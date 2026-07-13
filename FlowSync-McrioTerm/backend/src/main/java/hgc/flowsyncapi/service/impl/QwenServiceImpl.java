package hgc.flowsyncapi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import hgc.flowsyncapi.dto.*;
import hgc.flowsyncapi.entity.User;
import hgc.flowsyncapi.mapper.UserMapper;
import hgc.flowsyncapi.service.QwenService;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class QwenServiceImpl implements QwenService {

    private static final Logger log = LoggerFactory.getLogger(QwenServiceImpl.class);
    private static final String DEEPSEEK_URL = "https://api.deepseek.com/chat/completions";
    private static final String MODEL = "deepseek-chat";

    private final String apiKey;
    private final UserMapper userMapper;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;

    public QwenServiceImpl(@Value("${deepseek.api-key:}") String apiKey,
                           UserMapper userMapper,
                           ObjectMapper objectMapper) {
        this.apiKey = apiKey;
        this.userMapper = userMapper;
        this.objectMapper = objectMapper;
        this.restTemplate = new RestTemplate();
    }

    @PostConstruct
    public void init() {
        if (apiKey != null && !apiKey.isEmpty()) {
            log.info("DeepSeek API Key configured (length={}), AI features enabled", apiKey.length());
        } else {
            log.warn("DeepSeek API Key NOT configured - AI will use fallback template. Set DEEPSEEK_API_KEY env var.");
        }
    }

    @Override
    public String getTaskSuggestion(AiTaskSuggestionRequest request) {
        if (!isConfigured()) {
            return "AI 功能未配置 API Key。请设置环境变量 DEEPSEEK_API_KEY 或在 application.yml 中配置 deepseek.api-key。";
        }

        String systemPrompt = "你是一个直接的项目管理助手。请针对用户提供的任务给出建议，包括：1. 拆分的子任务建议 2. 执行顺序 3. 注意事项。回答控制在 300 字以内。";

        String userPrompt = String.format(
                "项目名称：%s\n任务标题：%s\n任务说明：%s\n请给出任务执行建议。",
                request.getProjectName(), request.getTaskTitle(), request.getTaskDescription());

        return callDeepSeek(systemPrompt, userPrompt);
    }

    @Override
    public AiTaskPlanResponse generateTaskPlan(AiTaskPlanRequest request) {
        if (!isConfigured()) {
            return buildFallbackPlan(request);
        }

        try {
            // 查询成员列表（排除管理员）
            List<User> members = userMapper.selectList(
                    new QueryWrapper<User>().ne("role", "管理员"));
            String memberList = members.stream()
                    .map(u -> u.getId() + " - " + u.getRealName() + "(" + u.getRole() + ")")
                    .collect(Collectors.joining("\n"));

            String systemPrompt = "你是一个项目管理专家。请将项目目标拆解为可直接执行的小任务，并为每个任务推荐最合适的负责人（从成员列表中选择）。"
                    + "你必须返回严格 JSON 格式，不要 markdown 代码块，不要额外解释。\n\n"
                    + "JSON 格式要求：\n"
                    + "{\"summary\":\"整体概述\",\"items\":[{\"title\":\"任务标题\",\"description\":\"任务说明\",\"priority\":\"高/中/低\",\"suggestedDays\":3,\"assigneeId\":1}]}\n\n"
                    + "要求：每个任务必须有 assigneeId（从成员列表中选数字 id），同一个成员可负责多个任务。";

            String userPrompt = String.format(
                    "项目名称：%s\n项目目标：%s\n补充说明：%s\n\n可选成员列表（id - 姓名 - 角色）：\n%s\n\n请拆解任务并推荐负责人。",
                    request.getProjectName(), request.getGoal(),
                    request.getDescription() != null ? request.getDescription() : "",
                    memberList);

            String rawJson = callDeepSeek(systemPrompt, userPrompt);
            return parseTaskPlan(rawJson, members, request);
        } catch (Exception e) {
            return buildFallbackPlan(request);
        }
    }

    /** 调用 DeepSeek API（OpenAI 兼容格式） */
    private String callDeepSeek(String systemPrompt, String userPrompt) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        Map<String, Object> sysMsg = new LinkedHashMap<>();
        sysMsg.put("role", "system");
        sysMsg.put("content", systemPrompt);

        Map<String, Object> usrMsg = new LinkedHashMap<>();
        usrMsg.put("role", "user");
        usrMsg.put("content", userPrompt);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", MODEL);
        body.put("messages", Arrays.asList(sysMsg, usrMsg));
        body.put("temperature", 0.7);
        body.put("max_tokens", 2000);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(DEEPSEEK_URL, entity, Map.class);
            Map<String, Object> respBody = response.getBody();
            List<Map<String, Object>> choices = (List<Map<String, Object>>) respBody.get("choices");
            Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
            return (String) message.get("content");
        } catch (Exception e) {
            return "ERROR: " + e.getMessage();
        }
    }

    private boolean isConfigured() {
        return apiKey != null && !apiKey.isEmpty();
    }

    /** 解析 AI 返回的 JSON 为任务计划 */
    private AiTaskPlanResponse parseTaskPlan(String rawJson, List<User> members, AiTaskPlanRequest request) {
        try {
            String json = rawJson.trim();
            if (json.startsWith("```json")) json = json.substring(7);
            if (json.startsWith("```")) json = json.substring(3);
            if (json.endsWith("```")) json = json.substring(0, json.length() - 3);
            json = json.trim();

            AiTaskPlanResponse response = objectMapper.readValue(json, AiTaskPlanResponse.class);

            // 校验 assigneeId 有效性
            List<Long> validIds = members.stream().map(User::getId).collect(Collectors.toList());
            if (response.getItems() != null) {
                for (AiTaskPlanItem item : response.getItems()) {
                    if (!validIds.contains(item.getAssigneeId())) {
                        item.setAssigneeId(validIds.isEmpty() ? null : validIds.get(0));
                    }
                }
            }
            return response;
        } catch (JsonProcessingException e) {
            return buildFallbackPlan(request);
        }
    }

    /** 降级方案：当 AI 不可用时生成默认任务计划 */
    private AiTaskPlanResponse buildFallbackPlan(AiTaskPlanRequest request) {
        AiTaskPlanResponse fallback = new AiTaskPlanResponse();
        fallback.setSummary("（降级方案）针对「" + request.getProjectName() + "」的基础任务拆解，请手动调整。");

        List<AiTaskPlanItem> items = new ArrayList<>();
        String[][] defaults = {
                {"需求分析", "梳理项目需求，明确功能范围", "高", "2"},
                {"技术方案设计", "确定技术选型和架构设计", "高", "3"},
                {"开发实现", "按模块编码实现核心功能", "中", "5"},
                {"测试验证", "功能测试与问题修复", "中", "3"},
                {"项目总结", "撰写项目总结文档", "低", "2"}
        };
        for (String[] d : defaults) {
            AiTaskPlanItem item = new AiTaskPlanItem();
            item.setTitle(d[0]);
            item.setDescription(d[1]);
            item.setPriority(d[2]);
            item.setSuggestedDays(Integer.parseInt(d[3]));
            item.setAssigneeId(null);
            items.add(item);
        }
        fallback.setItems(items);
        return fallback;
    }
}
