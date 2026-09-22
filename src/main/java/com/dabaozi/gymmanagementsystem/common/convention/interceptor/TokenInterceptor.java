package com.dabaozi.gymmanagementsystem.common.convention.interceptor;

import com.dabaozi.gymmanagementsystem.common.convention.utils.JwtUtils;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@Component
public class TokenInterceptor implements HandlerInterceptor {

    public boolean preHandle( HttpServletRequest httpServletRequest,HttpServletResponse httpServletResponse,Object  handler){
//        String path= httpServletRequest.getRequestURI().toString();//获取请求路径
////        2.判断请求路径是否为登录请求
//        if(path.contains("login")){
//            log.info("登录请求");
//
//            return true;//放行
//        }
        //3.获取请求头的令牌
        String token=httpServletRequest.getHeader("token");

//        4.判断令牌是否为空
        if(token==null||token.isEmpty()){
            log.info("请求头的令牌为空，返回错误信息");
            httpServletResponse.setStatus(401);
            return false;
        }
        //5.解析令牌解析失败错误结果
        Claims claims;
        try {
            claims = JwtUtils.parseJWT(token);
        }
        catch (Exception e){
            log.info("令牌解析失败，返回错误信息");
            httpServletResponse.setStatus(401);
            return false ;
        }
//        6.角色校验:管理员模块接口(路径含 /admin/)要求 role=1,否则 403
        String uri = httpServletRequest.getRequestURI();
        if (uri.contains("/admin/")) {
            String role = String.valueOf(claims.get("role"));
            if (!"1".equals(role)) {
                log.info("非管理员访问管理端接口，返回 403");
                httpServletResponse.setStatus(403);
                return false;
            }
        }
        //把登录信息放进 request,后续模块可直接使用
        httpServletRequest.setAttribute("loginId", claims.get("loginId"));
        httpServletRequest.setAttribute("role", claims.get("role"));
//        7.放行
        log.info("令牌解析成功，放行");
        return true;

    }
}
