package org.csu.creditbank.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.csu.creditbank.dto.CreditChangeRequest;
import org.csu.creditbank.entity.CreditAccount;

public interface CreditAccountService extends IService<CreditAccount> {

    CreditAccount openAccount(Long learnerId);

    CreditAccount increaseCredits(CreditChangeRequest request);

    CreditAccount consumeCredits(CreditChangeRequest request);

    /** 冻结积分：available → frozen */
    CreditAccount freezeCredits(Long learnerId, Integer amount);

    /** 解冻积分：frozen → available */
    CreditAccount unfreezeCredits(Long learnerId, Integer amount);

    /** 确认扣减：从 frozen 中扣减，减少 total */
    CreditAccount confirmDeduct(Long learnerId, Integer amount);

    /** 退还积分：增加 available + total */
    CreditAccount refundCredits(Long learnerId, Integer amount);
}
