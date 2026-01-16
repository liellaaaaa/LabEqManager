package org.cong.backend.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Spring Security 工具类
 * 用于获取当前登录用户信息
 */
public class SecurityUtils {

    /**
     * 获取当前登录用户名
     */
    public static String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            return authentication.getName();
        }
        return null;
    }

    /**
     * 获取当前用户的角色代码
     */
    public static String getCurrentUserRole() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getAuthorities() != null) {
            for (GrantedAuthority authority : authentication.getAuthorities()) {
                String authorityStr = authority.getAuthority();
                if (authorityStr.startsWith("ROLE_")) {
                    return authorityStr.substring(5); // 移除 "ROLE_" 前缀
                }
            }
        }
        return null;
    }

    /**
     * 检查当前用户是否是管理员
     */
    public static boolean isAdmin() {
        return "admin".equals(getCurrentUserRole());
    }

    /**
     * 检查当前用户是否是教师
     */
    public static boolean isTeacher() {
        return "teacher".equals(getCurrentUserRole());
    }

    /**
     * 检查当前用户是否是学生
     */
    public static boolean isStudent() {
        return "student".equals(getCurrentUserRole());
    }

    /**
     * 检查当前用户是否有指定角色
     */
    public static boolean hasRole(String roleCode) {
        return roleCode != null && roleCode.equals(getCurrentUserRole());
    }
}

