package hgc.flowsyncapi.service;

import hgc.flowsyncapi.entity.User;
import java.util.List;

public interface UserService {
    List<User> listUsers();
    User updateProfile(Long userId, String phone, String email);
    User changeRole(Long userId, String newRole);
}
