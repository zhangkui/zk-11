package com.charging.config;

import com.charging.common.Constants;
import com.charging.common.Result;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

@Component
public class TokenInterceptor implements HandlerInterceptor {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private ObjectMapper objectMapper;

    private static final Set<String> PUBLIC_PATHS = new HashSet<>();

    static {
        PUBLIC_PATHS.add("/user/login");
        PUBLIC_PATHS.add("/user/register");
        PUBLIC_PATHS.add("/doc.html");
        PUBLIC_PATHS.add("/v3/api-docs");
        PUBLIC_PATHS.add("/swagger-resources");
        PUBLIC_PATHS.add("/webjars");
        PUBLIC_PATHS.add("/favicon.ico");
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String requestUri = request.getRequestURI();
        String contextPath = request.getContextPath();
        String path = requestUri.substring(contextPath.length());

        if (isPublicPath(path)) {
            return true;
        }

        String token = request.getHeader(Constants.REQUEST_HEADER_TOKEN);
        if (token == null || token.isEmpty()) {
            writeErrorResponse(response, 401, "未登录，请先登录");
            return false;
        }

        String tokenValue = stringRedisTemplate.opsForValue().get(Constants.REDIS_USER_TOKEN_KEY + token);
        if (tokenValue == null) {
            writeErrorResponse(response, 401, "登录已过期，请重新登录");
            return false;
        }

        String[] parts = tokenValue.split(":");
        if (parts.length != 2) {
            writeErrorResponse(response, 401, "登录凭证无效");
            return false;
        }

        Long userId = Long.parseLong(parts[0]);
        Integer userRole = Integer.parseInt(parts[1]);

        request.setAttribute(Constants.REQUEST_ATTR_USER_ID, userId);
        request.setAttribute(Constants.REQUEST_ATTR_USER_ROLE, userRole);

        if (!hasPermission(request, path, userRole)) {
            writeErrorResponse(response, 403, "没有权限访问该资源");
            return false;
        }

        return true;
    }

    private boolean isPublicPath(String path) {
        for (String publicPath : PUBLIC_PATHS) {
            if (path.startsWith(publicPath)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasPermission(HttpServletRequest request, String path, Integer userRole) {
        if (userRole == Constants.UserRole.ADMIN) {
            if (path.matches("/reservation/create") ||
                path.matches("/reservation/cancel") ||
                path.matches("/reservation/\\d+/arrive") ||
                path.matches("/queue/join") ||
                path.matches("/queue/\\d+/cancel") ||
                path.matches("/record/start") ||
                path.matches("/record/end") ||
                path.matches("/fee/pay")) {
                return false;
            }
            return true;
        }

        if (userRole == Constants.UserRole.USER) {
            if (path.matches("/station/save") ||
                path.matches("/station/\\d+/status") ||
                (path.matches("/station/\\d+") && request.getMethod().equals("DELETE")) ||
                path.matches("/user/list") ||
                path.matches("/user/\\d+") ||
                path.matches("/dashboard/.*") ||
                path.matches("/queue/call/.*")) {
                return false;
            }
            return true;
        }

        return false;
    }

    private void writeErrorResponse(HttpServletResponse response, int code, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json;charset=UTF-8");
        Result<?> result = Result.error(code, message);
        response.getWriter().write(objectMapper.writeValueAsString(result));
    }
}
