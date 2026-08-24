package com.itheima.config;

import cn.hutool.json.JSONObject;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Token 校验 Filter
 * 校验 POST 请求体中 token 字段，不匹配则返回错误
 */
@WebFilter(urlPatterns = "/*")
public class TokenFilter implements Filter {

    private static final String TOKEN = "e6338a4acxw502kmf5dwr316ss8u0ymb";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // 只校验 POST 请求
        if (!"POST".equalsIgnoreCase(httpRequest.getMethod())) {
            chain.doFilter(request, response);
            return;
        }

        // 登录认证接口跳过 token 校验
        String path = httpRequest.getRequestURI();
        if (path != null && path.endsWith("/auth")) {
            chain.doFilter(request, response);
            return;
        }

        // 缓存 body 以便多次读取
        CachedBodyHttpServletRequest cachedRequest = new CachedBodyHttpServletRequest(httpRequest);
        String body = cachedRequest.getBodyAsString();

        // 校验 token：优先从 URL 参数取，其次从请求体 JSON 对象/数组中取
        String token = httpRequest.getParameter("token");

        if (token == null && body != null && !body.trim().isEmpty()) {
            try {
                String trimmed = body.trim();
                if (trimmed.startsWith("[")) {
                    // body 是 JSON 数组，取第一个元素的 token
                    cn.hutool.json.JSONArray arr = new cn.hutool.json.JSONArray(body);
                    if (!arr.isEmpty()) {
                        token = arr.getJSONObject(0).getStr("token");
                    }
                } else if (trimmed.startsWith("{")) {
                    JSONObject json = new JSONObject(body);
                    token = json.getStr("token");
                }
            } catch (Exception ignored) {
                // 解析失败忽略
            }
        }

        if (!TOKEN.equals(token)) {
            httpResponse.setContentType("application/json;charset=UTF-8");
            httpResponse.setStatus(200);
            JSONObject err = new JSONObject();
            err.set("success", false);
            err.set("error", "token error");
            httpResponse.getWriter().write(err.toString());
            return;
        }

        // token 正确，继续（传入缓存后的 request）
        try {
            chain.doFilter(cachedRequest, response);
        } catch (Exception e) {
            throw new IOException(e);
        }
    }
}
