package org.csu.creditbank.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.csu.creditbank.dto.CreditChangeRequest;
import org.csu.creditbank.entity.CreditAccount;

public interface CreditAccountService extends IService<CreditAccount> {

    CreditAccount openAccount(Long learnerId);

    CreditAccount increaseCredits(CreditChangeRequest request);

    CreditAccount consumeCredits(CreditChangeRequest request);
}
