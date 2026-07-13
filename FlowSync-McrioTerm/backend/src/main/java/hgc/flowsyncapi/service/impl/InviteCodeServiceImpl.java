package hgc.flowsyncapi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import hgc.flowsyncapi.entity.InviteCode;
import hgc.flowsyncapi.mapper.InviteCodeMapper;
import hgc.flowsyncapi.service.InviteCodeService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class InviteCodeServiceImpl implements InviteCodeService {

    private final InviteCodeMapper mapper;

    public InviteCodeServiceImpl(InviteCodeMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public String generate(Long adminId) {
        // 清理过期码
        mapper.delete(new QueryWrapper<InviteCode>()
                .lt("create_time", LocalDateTime.now().minusMinutes(2)));

        String code = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        InviteCode ic = new InviteCode();
        ic.setCode(code);
        ic.setCreatedBy(adminId);
        ic.setUsed(false);
        mapper.insert(ic);
        return code;
    }

    @Override
    public boolean validateAndConsume(String code) {
        InviteCode ic = mapper.selectOne(new QueryWrapper<InviteCode>()
                .eq("code", code).eq("used", false)
                .gt("create_time", LocalDateTime.now().minusMinutes(2)));
        if (ic == null) return false;
        ic.setUsed(true);
        mapper.updateById(ic);
        return true;
    }
}
