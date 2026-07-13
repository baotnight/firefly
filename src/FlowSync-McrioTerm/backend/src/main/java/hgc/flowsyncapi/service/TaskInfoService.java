package hgc.flowsyncapi.service;

import hgc.flowsyncapi.entity.TaskInfo;
import java.util.List;

public interface TaskInfoService {
    List<TaskInfo> listTasks(Long projectId);
    TaskInfo saveTask(TaskInfo task, Long currentUserId);
    TaskInfo getById(Long id);
    /** 组员只能更新自己任务的状态 */
    TaskInfo updateTaskStatus(Long taskId, String status, Long currentUserId);
    void deleteTask(Long id);
}
