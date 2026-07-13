package hgc.flowsyncapi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import hgc.flowsyncapi.entity.ProjectInfo;
import hgc.flowsyncapi.entity.TaskInfo;
import hgc.flowsyncapi.entity.TaskSummary;
import hgc.flowsyncapi.entity.User;
import hgc.flowsyncapi.mapper.ProjectInfoMapper;
import hgc.flowsyncapi.mapper.TaskInfoMapper;
import hgc.flowsyncapi.mapper.TaskSummaryMapper;
import hgc.flowsyncapi.mapper.UserMapper;
import hgc.flowsyncapi.service.TaskSummaryService;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TaskSummaryServiceImpl implements TaskSummaryService {

    private final TaskSummaryMapper summaryMapper;
    private final ProjectInfoMapper projectInfoMapper;
    private final TaskInfoMapper taskInfoMapper;
    private final UserMapper userMapper;

    public TaskSummaryServiceImpl(TaskSummaryMapper summaryMapper,
                                  ProjectInfoMapper projectInfoMapper,
                                  TaskInfoMapper taskInfoMapper,
                                  UserMapper userMapper) {
        this.summaryMapper = summaryMapper;
        this.projectInfoMapper = projectInfoMapper;
        this.taskInfoMapper = taskInfoMapper;
        this.userMapper = userMapper;
    }

    @Override
    public List<TaskSummary> listSummaries() {
        List<TaskSummary> summaries = summaryMapper.selectList(
                new QueryWrapper<TaskSummary>().orderByDesc("id"));
        fillNames(summaries);
        return summaries;
    }

    @Override
    public TaskSummary saveSummary(TaskSummary summary, Long currentUserId) {
        summary.setCreatedBy(currentUserId);
        summaryMapper.insert(summary);
        return summary;
    }

    private void fillNames(List<TaskSummary> summaries) {
        if (summaries.isEmpty()) return;

        List<Long> projectIds = summaries.stream().map(TaskSummary::getProjectId).distinct().collect(Collectors.toList());
        List<Long> taskIds = summaries.stream().map(TaskSummary::getTaskId)
                .filter(id -> id != null).distinct().collect(Collectors.toList());
        List<Long> userIds = summaries.stream().map(TaskSummary::getCreatedBy)
                .filter(id -> id != null).distinct().collect(Collectors.toList());

        Map<Long, String> projectNames = new HashMap<>();
        if (!projectIds.isEmpty()) {
            List<ProjectInfo> projects = projectInfoMapper.selectBatchIds(projectIds);
            for (ProjectInfo p : projects) projectNames.put(p.getId(), p.getName());
        }

        Map<Long, String> taskNames = new HashMap<>();
        if (!taskIds.isEmpty()) {
            List<TaskInfo> tasks = taskInfoMapper.selectBatchIds(taskIds);
            for (TaskInfo t : tasks) taskNames.put(t.getId(), t.getTitle());
        }

        Map<Long, String> userNames = new HashMap<>();
        if (!userIds.isEmpty()) {
            List<User> users = userMapper.selectBatchIds(userIds);
            for (User u : users) userNames.put(u.getId(), u.getRealName());
        }

        for (TaskSummary s : summaries) {
            s.setProjectName(projectNames.getOrDefault(s.getProjectId(), "未知"));
            s.setTaskTitle(taskNames.getOrDefault(s.getTaskId(), "—"));
            s.setCreatorName(userNames.getOrDefault(s.getCreatedBy(), "未知"));
        }
    }
}
