package org.csu.creditbank.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.csu.creditbank.common.BusinessException;
import org.csu.creditbank.dto.CreditChangeRequest;
import org.csu.creditbank.entity.CreditAccount;
import org.csu.creditbank.entity.CreditTransaction;
import org.csu.creditbank.mapper.CreditAccountMapper;
import org.csu.creditbank.service.CreditAccountService;
import org.csu.creditbank.service.CreditTransactionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreditAccountServiceImpl extends ServiceImpl<CreditAccountMapper, CreditAccount> implements CreditAccountService {

    private final CreditTransactionService transactionService;

    public CreditAccountServiceImpl(CreditTransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Override
    @Transactional
    public CreditAccount openAccount(Long learnerId) {
        CreditAccount existing = getOne(new LambdaQueryWrapper<CreditAccount>()
                .eq(CreditAccount::getLearnerId, learnerId), false);
        if (existing != null) {
            return existing;
        }
        CreditAccount account = new CreditAccount();
        account.setLearnerId(learnerId);
        account.setTotalCredits(0);
        account.setAvailableCredits(0);
        account.setFrozenCredits(0);
        account.setAccountStatus("ACTIVE");
        save(account);
        return account;
    }

    @Override
    @Transactional
    public CreditAccount increaseCredits(CreditChangeRequest request) {
        CreditAccount account = openAccount(request.getLearnerId());
        account.setTotalCredits(account.getTotalCredits() + request.getAmount());
        account.setAvailableCredits(account.getAvailableCredits() + request.getAmount());
        updateById(account);
        saveTransaction(request, account, "INCREASE");
        return account;
    }

    @Override
    @Transactional
    public CreditAccount consumeCredits(CreditChangeRequest request) {
        CreditAccount account = openAccount(request.getLearnerId());
        if (account.getAvailableCredits() < request.getAmount()) {
            throw new BusinessException("可用积分不足");
        }
        account.setAvailableCredits(account.getAvailableCredits() - request.getAmount());
        updateById(account);
        saveTransaction(request, account, "CONSUME");
        return account;
    }

    private void saveTransaction(CreditChangeRequest request, CreditAccount account, String type) {
        CreditTransaction transaction = new CreditTransaction();
        transaction.setLearnerId(request.getLearnerId());
        transaction.setAccountId(account.getId());
        transaction.setTransactionType(type);
        transaction.setAmount(request.getAmount());
        transaction.setBalanceAfter(account.getAvailableCredits());
        transaction.setSourceType(request.getSourceType());
        transaction.setSourceNo(request.getSourceNo());
        transaction.setRemark(request.getRemark());
        transactionService.save(transaction);
    }
}
