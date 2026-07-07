package org.csu.creditbank.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.csu.creditbank.entity.Learner;

public interface LearnerService extends IService<Learner> {

    /** 登录：校验用户名密码，成功返回 Learner */
    Learner login(String username, String password);

    /** 注册学员，默认角色 STUDENT，密码自动哈希 */
    Learner register(Learner learner, String rawPassword);
}
