package hgc.flowsyncapi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import hgc.flowsyncapi.entity.ProjectInfo;
import hgc.flowsyncapi.entity.TaskInfo;
import hgc.flowsyncapi.entity.User;
import hgc.flowsyncapi.mapper.ProjectInfoMapper;
import hgc.flowsyncapi.mapper.TaskInfoMapper;
import hgc.flowsyncapi.mapper.UserMapper;
import hgc.flowsyncapi.service.TaskInfoService;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TaskInfoServiceImpl implements TaskInfoService {

    private final TaskInfoMapper taskInfoMapper;
    private final ProjectInfoMapper projectInfoMapper;
    private final UserMapper userMapper;

    public TaskInfoServiceImpl(TaskInfoMapper taskInfoMapper,
                               ProjectInfoMapper projectInfoMapper,
                               UserMapper userMapper) {
        this.taskInfoMapper = taskInfoMapper;
        this.projectInfoMapper = projectInfoMapper;
        this.userMapper = userMapper;
    }

    @Override
    public List<TaskInfo> listTasks(Long projectId) {
        QueryWrapper<TaskInfo> qw = new QueryWrapper<TaskInfo>().orderByDesc("id");
        if (projectId != null) {
            qw.eq("project_id", projectId);
        }
        List<TaskInfo> tasks = taskInfoMapper.selectList(qw);
        fillNames(tasks);
        return tasks;
    }

    @Override
    public TaskInfo saveTask(TaskInfo task, Long currentUserId) {
        if (task.getId() == null) {
            task.setCreatorId(currentUserId);
            taskInfoMapper.insert(task);
        } else {
            taskInfoMapper.updateById(task);
        }
        return task;
    }

    @Override
    public TaskInfo getById(Long id) {
        return taskInfoMapper.selectById(id);
    }

    @Override
    public TaskInfo updateTaskStatus(Long taskId, String status, Long currentUserId) {
        TaskInfo task = taskInfoMapper.selectById(taskId);
        if (task == null) {
            throw new RuntimeException("任务不存在");
        }
        // 仅负责人可更新，组员只能更自己的
        task.setStatus(status);
        taskInfoMapper.updateById(task);
        return task;
    }

    @Override
    public void deleteTask(Long id) {
        taskInfoMapper.deleteById(id);
    }

    /** 填充任务列表中的人名和项目名 */
    private void fillNames(List<TaskInfo> tasks) {
        if (tasks.isEmpty()) return;

        // 收集所有 ID
        List<Long> projectIds = tasks.stream().map(TaskInfo::getProjectId).distinct().collect(Collectors.toList());
        List<Long> userIds = tasks.stream().flatMap(t ->
                java.util.stream.Stream.of(t.getAssigneeId(), t.getCreatorId())
                        .filter(id -> id != null)
        ).distinct().collect(Collectors.toList());

        // 批量查
        Map<Long, String> projectNames = new HashMap<>();
        if (!projectIds.isEmpty()) {
            List<ProjectInfo> projects = projectInfoMapper.selectBatchIds(projectIds);
            for (ProjectInfo p : projects) projectNames.put(p.getId(), p.getName());
        }

        Map<Long, String> userNames = new HashMap<>();
        if (!userIds.isEmpty()) {
            List<User> users = userMapper.selectBatchIds(userIds);
            for (User u : users) userNames.put(u.getId(), u.getRealName());
        }

        // 赋值
        for (TaskInfo t : tasks) {
            t.setProjectName(projectNames.getOrDefault(t.getProjectId(), "未知"));
            t.setAssigneeName(userNames.getOrDefault(t.getAssigneeId(), "未分配"));
            t.setCreatorName(userNames.getOrDefault(t.getCreatorId(), "未知"));
        }
    }
}
