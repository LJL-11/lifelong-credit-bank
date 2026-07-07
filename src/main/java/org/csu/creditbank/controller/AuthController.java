package org.csu.creditbank.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.csu.creditbank.common.ApiResult;
import org.csu.creditbank.dto.LoginRequest;
import org.csu.creditbank.dto.LoginResult;
import org.csu.creditbank.entity.Learner;
import org.csu.creditbank.service.LearnerService;
import org.csu.creditbank.util.TokenUtil;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final LearnerService learnerService;
    private final TokenUtil tokenUtil;

    public AuthController(LearnerService learnerService, TokenUtil tokenUtil) {
        this.learnerService = learnerService;
        this.tokenUtil = tokenUtil;
    }

    @PostMapping("/login")
    public ApiResult<LoginResult> login(@Valid @RequestBody LoginRequest request) {
        Learner learner = learnerService.login(request.getUsername(), request.getPassword());
        String token = tokenUtil.generateToken();
        tokenUtil.storeToken(token, learner.getId(), learner.getRole());
        LoginResult result = new LoginResult(
                token, learner.getId(), learner.getUsername(),
                learner.getRealName(), learner.getRole());
        return ApiResult.ok(result);
    }

    @PostMapping("/logout")
    public ApiResult<Void> logout(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            tokenUtil.removeToken(authHeader.substring(7));
        }
        return ApiResult.ok();
    }

    @GetMapping("/me")
    public ApiResult<LoginResult> me(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        String role = (String) request.getAttribute("role");
        Learner learner = learnerService.getById(userId);
        if (learner == null) {
            return ApiResult.fail("用户不存在");
        }
        LoginResult result = new LoginResult(
                null, learner.getId(), learner.getUsername(),
                learner.getRealName(), role);
        return ApiResult.ok(result);
    }
}
