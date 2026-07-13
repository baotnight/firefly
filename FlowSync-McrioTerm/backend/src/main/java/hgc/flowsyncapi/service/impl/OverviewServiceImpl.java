package hgc.flowsyncapi.service.impl;

import hgc.flowsyncapi.mapper.*;
import hgc.flowsyncapi.service.OverviewService;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class OverviewServiceImpl implements OverviewService {

    private final UserMapper userMapper;
    private final ProjectInfoMapper projectInfoMapper;
    private final TaskInfoMapper taskInfoMapper;
    private final TaskSummaryMapper summaryMapper;

    public OverviewServiceImpl(UserMapper userMapper,
                               ProjectInfoMapper projectInfoMapper,
                               TaskInfoMapper taskInfoMapper,
                               TaskSummaryMapper summaryMapper) {
        this.userMapper = userMapper;
        this.projectInfoMapper = projectInfoMapper;
        this.taskInfoMapper = taskInfoMapper;
        this.summaryMapper = summaryMapper;
    }

    @Override
    public Map<String, Object> getOverview() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("userCount", userMapper.selectCount(null));
        result.put("projectCount", projectInfoMapper.selectCount(null));
        result.put("taskCount", taskInfoMapper.selectCount(null));
        result.put("summaryCount", summaryMapper.selectCount(null));
        return result;
    }
}
