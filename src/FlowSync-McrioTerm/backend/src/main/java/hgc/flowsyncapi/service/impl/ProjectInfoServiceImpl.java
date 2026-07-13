package hgc.flowsyncapi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import hgc.flowsyncapi.entity.ProjectInfo;
import hgc.flowsyncapi.entity.TaskInfo;
import hgc.flowsyncapi.entity.TaskLog;
import hgc.flowsyncapi.entity.TaskSummary;
import hgc.flowsyncapi.entity.User;
import hgc.flowsyncapi.mapper.ProjectInfoMapper;
import hgc.flowsyncapi.mapper.TaskInfoMapper;
import hgc.flowsyncapi.mapper.TaskLogMapper;
import hgc.flowsyncapi.mapper.TaskSummaryMapper;
import hgc.flowsyncapi.mapper.UserMapper;
import hgc.flowsyncapi.service.ProjectInfoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProjectInfoServiceImpl implements ProjectInfoService {

    private final ProjectInfoMapper projectInfoMapper;
    private final UserMapper userMapper;
    private final TaskInfoMapper taskInfoMapper;
    private final TaskLogMapper taskLogMapper;
    private final TaskSummaryMapper taskSummaryMapper;

    public ProjectInfoServiceImpl(ProjectInfoMapper projectInfoMapper,
                                  UserMapper userMapper,
                                  TaskInfoMapper taskInfoMapper,
                                  TaskLogMapper taskLogMapper,
                                  TaskSummaryMapper taskSummaryMapper) {
        this.projectInfoMapper = projectInfoMapper;
        this.userMapper = userMapper;
        this.taskInfoMapper = taskInfoMapper;
        this.taskLogMapper = taskLogMapper;
        this.taskSummaryMapper = taskSummaryMapper;
    }

    @Override
    public List<ProjectInfo> listProjects(Long currentUserId) {
        List<Long> visibleIds = listVisibleProjectIds(currentUserId);
        if (visibleIds.isEmpty()) {
            return Collections.emptyList();
        }
        List<ProjectInfo> projects = projectInfoMapper.selectList(
                new QueryWrapper<ProjectInfo>().in("id", visibleIds).orderByDesc("id"));
        fillOwnerNames(projects);
        return projects;
    }

    @Override
    public ProjectInfo saveProject(ProjectInfo project, Long currentUserId) {
        if (project.getId() != null) {
            if (!isProjectOwner(project.getId(), currentUserId)) {
                throw new RuntimeException("无权操作：只有项目负责人可以编辑项目");
            }
            projectInfoMapper.updateById(project);
        } else {
            project.setOwnerId(currentUserId);
            projectInfoMapper.insert(project);
        }
        return project;
    }

    @Override
    @Transactional
    public void deleteProject(Long id, Long currentUserId) {
        if (!isProjectOwner(id, currentUserId)) {
            throw new RuntimeException("无权操作：只有项目负责人可以删除项目");
        }

        // 1. 查出该项目下所有任务 ID
        List<TaskInfo> tasks = taskInfoMapper.selectList(
                new QueryWrapper<TaskInfo>().eq("project_id", id));
        List<Long> taskIds = tasks.stream().map(TaskInfo::getId).collect(Collectors.toList());

        // 2. 按 FK 依赖顺序逆序删除：先删子表，再删父表
        if (!taskIds.isEmpty()) {
            // 删除所有任务的进度记录
            taskLogMapper.delete(new QueryWrapper<TaskLog>().in("task_id", taskIds));
            // 删除所有关联任务的总结
            taskSummaryMapper.delete(new QueryWrapper<TaskSummary>().in("task_id", taskIds));
        }
        // 删除项目级别的总结
        taskSummaryMapper.delete(new QueryWrapper<TaskSummary>().eq("project_id", id));
        // 删除所有任务
        taskInfoMapper.delete(new QueryWrapper<TaskInfo>().eq("project_id", id));
        // 最后删除项目本身
        projectInfoMapper.deleteById(id);
    }

    @Override
    public boolean isProjectOwner(Long projectId, Long userId) {
        User user = userMapper.selectById(userId);
        if (user != null && "管理员".equals(user.getRole())) return true;
        ProjectInfo project = projectInfoMapper.selectById(projectId);
        return project != null && project.getOwnerId().equals(userId);
    }

    @Override
    public List<Long> listVisibleProjectIds(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            return Collections.emptyList();
        }

        if ("管理员".equals(user.getRole())) {
            // 管理员：看到所有项目
            List<ProjectInfo> all = projectInfoMapper.selectList(null);
            return all.stream().map(ProjectInfo::getId).collect(Collectors.toList());
        }

        if ("负责人".equals(user.getRole())) {
            List<ProjectInfo> owned = projectInfoMapper.selectList(
                    new QueryWrapper<ProjectInfo>().eq("owner_id", userId));
            return owned.stream().map(ProjectInfo::getId).collect(Collectors.toList());
        } else {
            List<TaskInfo> tasks = taskInfoMapper.selectList(
                    new QueryWrapper<TaskInfo>().eq("assignee_id", userId));
            return tasks.stream()
                    .map(TaskInfo::getProjectId)
                    .distinct()
                    .collect(Collectors.toList());
        }
    }

    @Override
    public List<ProjectInfo> listOwnedProjects(Long userId) {
        return projectInfoMapper.selectList(
                new QueryWrapper<ProjectInfo>().eq("owner_id", userId));
    }

    @Override
    public int transferOwnership(Long fromUserId, Long toUserId) {
        List<ProjectInfo> projects = listOwnedProjects(fromUserId);
        int count = 0;
        for (ProjectInfo p : projects) {
            p.setOwnerId(toUserId);
            projectInfoMapper.updateById(p);
            count++;
        }
        return count;
    }

    private void fillOwnerNames(List<ProjectInfo> projects) {
        if (projects.isEmpty()) return;
        List<Long> ownerIds = projects.stream()
                .map(ProjectInfo::getOwnerId).distinct().collect(Collectors.toList());
        List<User> users = userMapper.selectBatchIds(ownerIds);
        Map<Long, String> nameMap = new HashMap<>();
        for (User u : users) {
            nameMap.put(u.getId(), u.getRealName());
        }
        for (ProjectInfo p : projects) {
            p.setOwnerName(nameMap.getOrDefault(p.getOwnerId(), "未知"));
        }
    }
}
