package hgc.flowsyncapi.service;

import hgc.flowsyncapi.entity.TaskSummary;
import java.util.List;

public interface TaskSummaryService {
    List<TaskSummary> listSummaries();
    TaskSummary saveSummary(TaskSummary summary, Long currentUserId);
}
