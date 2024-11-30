package net.hwyz.iov.cloud.mpt.auth.service.controller;

import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import jakarta.servlet.http.HttpServletRequest;
import net.hwyz.iov.cloud.framework.common.bean.Response;
import net.hwyz.iov.cloud.framework.common.util.JwtUtil;
import net.hwyz.iov.cloud.framework.security.auth.AuthUtil;
import net.hwyz.iov.cloud.framework.security.service.TokenService;
import net.hwyz.iov.cloud.framework.security.util.SecurityUtils;
import net.hwyz.iov.cloud.mpt.auth.service.form.LoginBody;
import net.hwyz.iov.cloud.mpt.auth.service.form.RegisterBody;
import net.hwyz.iov.cloud.mpt.auth.service.service.SysLoginService;
import net.hwyz.iov.cloud.mpt.system.api.model.LoginUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * token 控制
 *
 * @author hwyz_leo
 */
@RestController
public class TokenController {
    @Autowired
    private TokenService tokenService;

    @Autowired
    private SysLoginService sysLoginService;

    @PostMapping("login")
    public Response<?> login(@RequestBody LoginBody form) {
        // 用户登录
        LoginUser userInfo = sysLoginService.login(form.getUsername(), form.getPassword());
        // 获取登录token
        return Response.ok(tokenService.createToken(userInfo));
    }

    @DeleteMapping("logout")
    public Response<?> logout(HttpServletRequest request) {
        String token = SecurityUtils.getToken(request);
        if (StrUtil.isNotEmpty(token)) {
            String username = JwtUtil.getUserName(token);
            // 删除用户缓存记录
            AuthUtil.logoutByToken(token);
            // 记录用户退出日志
            sysLoginService.logout(username);
        }
        return Response.ok();
    }

    @PostMapping("refresh")
    public Response<?> refresh(HttpServletRequest request) {
        LoginUser loginUser = tokenService.getLoginUser(request);
        if (ObjUtil.isNotNull(loginUser)) {
            // 刷新令牌有效期
            tokenService.refreshToken(loginUser);
            return Response.ok();
        }
        return Response.ok();
    }

    @PostMapping("register")
    public Response<?> register(@RequestBody RegisterBody registerBody) {
        // 用户注册
        sysLoginService.register(registerBody.getUsername(), registerBody.getPassword());
        return Response.ok();
    }
}
