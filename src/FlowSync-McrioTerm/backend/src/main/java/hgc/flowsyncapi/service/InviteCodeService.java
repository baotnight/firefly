package hgc.flowsyncapi.service;

public interface InviteCodeService {
    /** 生成邀请码（2分钟有效） */
    String generate(Long adminId);
    /** 验证并消费邀请码，返回是否有效 */
    boolean validateAndConsume(String code);
}
