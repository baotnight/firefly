package hgc.flowsyncapi.service;

import hgc.flowsyncapi.entity.User;

public interface AuthService {
    User login(String username, String password);
    void updatePassword(Long userId, String oldPassword, String newPassword);
    User register(String username, String password, String realName, String role, String inviteCode);
}
