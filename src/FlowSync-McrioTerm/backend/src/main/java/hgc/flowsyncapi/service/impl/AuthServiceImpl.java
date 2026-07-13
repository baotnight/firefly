package hgc.flowsyncapi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import hgc.flowsyncapi.entity.User;
import hgc.flowsyncapi.mapper.UserMapper;
import hgc.flowsyncapi.service.AuthService;
import hgc.flowsyncapi.service.InviteCodeService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final InviteCodeService inviteCodeService;

    public AuthServiceImpl(UserMapper userMapper,
                           PasswordEncoder passwordEncoder,
                           InviteCodeService inviteCodeService) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.inviteCodeService = inviteCodeService;
    }

    @Override
    public User login(String username, String password) {
        User user = userMapper.selectOne(
                new QueryWrapper<User>().eq("username", username));
        if (user == null) {
            throw new RuntimeException("用户名或密码错误");
        }
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }
        user.setPassword(null);
        return user;
    }

    @Override
    public void updatePassword(Long userId, String oldPassword, String newPassword) {
        User user = userMapper.selectById(userId);
        if (user == null) throw new RuntimeException("用户不存在");
        if (!passwordEncoder.matches(oldPassword, user.getPassword()))
            throw new RuntimeException("原密码错误");
        user.setPassword(passwordEncoder.encode(newPassword));
        userMapper.updateById(user);
    }

    @Override
    public User register(String username, String password, String realName, String role, String inviteCode) {
        User existing = userMapper.selectOne(
                new QueryWrapper<User>().eq("username", username));
        if (existing != null) throw new RuntimeException("用户名已存在");

        String finalRole = (role != null && !role.isEmpty()) ? role : "组员";

        // 注册为负责人需要有效邀请码
        if ("负责人".equals(finalRole)) {
            if (inviteCode == null || inviteCode.isEmpty()) {
                throw new RuntimeException("注册为项目负责人需要邀请码");
            }
            if (!inviteCodeService.validateAndConsume(inviteCode)) {
                throw new RuntimeException("邀请码无效或已过期");
            }
        }

        // 不允许自行注册为管理员
        if ("管理员".equals(finalRole)) {
            throw new RuntimeException("不能注册为管理员");
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setRealName(realName);
        user.setRole(finalRole);
        userMapper.insert(user);
        user.setPassword(null);
        return user;
    }
}
