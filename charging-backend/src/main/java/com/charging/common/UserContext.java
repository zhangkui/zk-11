package com.charging.common;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public class UserContext {

    public static Long getCurrentUserId() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return null;
        }
        HttpServletRequest request = attributes.getRequest();
        return (Long) request.getAttribute(Constants.REQUEST_ATTR_USER_ID);
    }

    public static Integer getCurrentUserRole() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return null;
        }
        HttpServletRequest request = attributes.getRequest();
        return (Integer) request.getAttribute(Constants.REQUEST_ATTR_USER_ROLE);
    }

    public static boolean isAdmin() {
        Integer role = getCurrentUserRole();
        return role != null && role == Constants.UserRole.ADMIN;
    }

    public static boolean isUser() {
        Integer role = getCurrentUserRole();
        return role != null && role == Constants.UserRole.USER;
    }

    public static void validateUserRole(int expectedRole) {
        Integer currentRole = getCurrentUserRole();
        if (currentRole == null || currentRole != expectedRole) {
            throw new com.charging.exception.BusinessException("没有权限执行该操作");
        }
    }

    public static void validateUserId(Long userId) {
        Long currentUserId = getCurrentUserId();
        if (currentUserId == null) {
            throw new com.charging.exception.BusinessException("用户未登录");
        }
        if (!currentUserId.equals(userId)) {
            throw new com.charging.exception.BusinessException("没有权限访问该数据");
        }
    }
}
