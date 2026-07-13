package hgc.flowsyncapi.service;

import hgc.flowsyncapi.entity.ProjectInfo;
import java.util.List;

public interface ProjectInfoService {
    /** 获取当前用户可见的项目列表（负责人：自己的项目；组员：自己被分配任务的项目） */
    List<ProjectInfo> listProjects(Long currentUserId);

    /** 新建或编辑项目（编辑时校验是否为负责人） */
    ProjectInfo saveProject(ProjectInfo project, Long currentUserId);

    /** 删除项目（校验是否为负责人） */
    void deleteProject(Long id, Long currentUserId);

    /** 检查用户是否为项目负责人 */
    boolean isProjectOwner(Long projectId, Long userId);

    /** 获取用户可见的项目 ID 列表 */
    List<Long> listVisibleProjectIds(Long userId);
    /** 获取用户拥有的项目列表 */
    List<ProjectInfo> listOwnedProjects(Long userId);
    /** 转让项目所有权 */
    int transferOwnership(Long fromUserId, Long toUserId);
}
