package hgc.flowsyncapi.service;

import hgc.flowsyncapi.entity.TaskLog;
import java.util.List;

public interface TaskLogService {
    List<TaskLog> listTaskLogs(Long taskId);
    TaskLog saveTaskLog(TaskLog log, Long currentUserId);
}
