package org.cong.backend.security;

import org.cong.backend.user.entity.User;
import org.cong.backend.user.repository.UserRepository;
import org.springframework.stereotype.Service;

/**
 * 权限检查服务
 * 用于在 @PreAuthorize 注解中进行权限校验
 */
@Service
public class PermissionService {

    private final UserRepository userRepository;

    public PermissionService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * 检查是否是当前用户（用于权限校验）
     * 在 @PreAuthorize 注解中使用：@PreAuthorize("hasRole('admin') or @permissionService.isCurrentUser(#id)")
     */
    public boolean isCurrentUser(Long userId) {
        String currentUsername = SecurityUtils.getCurrentUsername();
        if (currentUsername == null) {
            return false;
        }
        return userRepository.findById(userId)
                .map(user -> user.getUsername().equals(currentUsername))
                .orElse(false);
    }
}

