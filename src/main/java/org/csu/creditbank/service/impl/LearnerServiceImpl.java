package org.csu.creditbank.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.csu.creditbank.common.BusinessException;
import org.csu.creditbank.entity.Learner;
import org.csu.creditbank.mapper.LearnerMapper;
import org.csu.creditbank.service.LearnerService;
import org.csu.creditbank.util.PasswordUtil;
import org.springframework.stereotype.Service;

@Service
public class LearnerServiceImpl extends ServiceImpl<LearnerMapper, Learner> implements LearnerService {

    @Override
    public Learner login(String username, String password) {
        Learner learner = getOne(new LambdaQueryWrapper<Learner>()
                .eq(Learner::getUsername, username), false);
        if (learner == null) {
            throw new BusinessException("用户名或密码错误");
        }
        if (!PasswordUtil.verify(password, learner.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }
        if (!"ACTIVE".equals(learner.getStatus())) {
            throw new BusinessException("账号已被禁用");
        }
        return learner;
    }

    @Override
    public Learner register(Learner learner, String rawPassword) {
        // 检查用户名唯一
        Learner existing = getOne(new LambdaQueryWrapper<Learner>()
                .eq(Learner::getUsername, learner.getUsername()), false);
        if (existing != null) {
            throw new BusinessException("用户名已存在");
        }
        learner.setPassword(PasswordUtil.hash(rawPassword));
        learner.setRole("STUDENT");
        learner.setStatus("ACTIVE");
        save(learner);
        return learner;
    }
}
