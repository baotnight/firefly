package hgc.flowsyncapi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import hgc.flowsyncapi.entity.User;
import hgc.flowsyncapi.mapper.UserMapper;
import hgc.flowsyncapi.service.UserService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;

    public UserServiceImpl(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Override
    public List<User> listUsers() {
        List<User> users = userMapper.selectList(
                new QueryWrapper<User>().orderByAsc("id"));
        for (User u : users) {
            u.setPassword(null);
        }
        return users;
    }

    @Override
    public User updateProfile(Long userId, String phone, String email) {
        User user = userMapper.selectById(userId);
        if (user == null) throw new RuntimeException("用户不存在");
        user.setPhone(phone);
        user.setEmail(email);
        userMapper.updateById(user);
        user.setPassword(null);
        return user;
    }

    @Override
    public User changeRole(Long userId, String newRole) {
        User user = userMapper.selectById(userId);
        if (user == null) throw new RuntimeException("用户不存在");
        if ("管理员".equals(user.getRole())) throw new RuntimeException("不能修改管理员角色");
        user.setRole(newRole);
        userMapper.updateById(user);
        user.setPassword(null);
        return user;
    }
}
