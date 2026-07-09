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
        int balanceBefore = account.getAvailableCredits();
        account.setTotalCredits(account.getTotalCredits() + request.getAmount());
        account.setAvailableCredits(account.getAvailableCredits() + request.getAmount());
        updateById(account);

        CreditTransaction t = new CreditTransaction();
        t.setLearnerId(request.getLearnerId());
        t.setAccountId(account.getId());
        t.setTransactionType("INCREASE");
        t.setAmount(request.getAmount());
        t.setBalanceBefore(balanceBefore);
        t.setBalanceAfter(account.getAvailableCredits());
        t.setSourceType(request.getSourceType());
        t.setSourceNo(request.getSourceNo());
        t.setRemark(request.getRemark());
        transactionService.save(t);

        return account;
    }

    @Override
    @Transactional
    public CreditAccount consumeCredits(CreditChangeRequest request) {
        CreditAccount account = openAccount(request.getLearnerId());
        if (account.getAvailableCredits() < request.getAmount()) {
            throw new BusinessException("可用积分不足");
        }
        int balanceBefore = account.getAvailableCredits();
        account.setAvailableCredits(account.getAvailableCredits() - request.getAmount());
        account.setTotalCredits(account.getTotalCredits() - request.getAmount());
        updateById(account);

        CreditTransaction t = new CreditTransaction();
        t.setLearnerId(request.getLearnerId());
        t.setAccountId(account.getId());
        t.setTransactionType("CONSUME");
        t.setAmount(request.getAmount());
        t.setBalanceBefore(balanceBefore);
        t.setBalanceAfter(account.getAvailableCredits());
        t.setSourceType(request.getSourceType());
        t.setSourceNo(request.getSourceNo());
        t.setRemark(request.getRemark());
        transactionService.save(t);

        return account;
    }

    @Override
    @Transactional
    public CreditAccount freezeCredits(Long learnerId, Integer amount) {
        CreditAccount account = openAccount(learnerId);
        if (account.getAvailableCredits() < amount) {
            throw new BusinessException("可用积分不足，无法冻结");
        }
        account.setAvailableCredits(account.getAvailableCredits() - amount);
        account.setFrozenCredits(account.getFrozenCredits() + amount);
        updateById(account);
        return account;
    }

    @Override
    @Transactional
    public CreditAccount unfreezeCredits(Long learnerId, Integer amount) {
        CreditAccount account = openAccount(learnerId);
        if (account.getFrozenCredits() < amount) {
            throw new BusinessException("冻结积分不足，无法解冻");
        }
        account.setFrozenCredits(account.getFrozenCredits() - amount);
        account.setAvailableCredits(account.getAvailableCredits() + amount);
        updateById(account);
        return account;
    }

    @Override
    @Transactional
    public CreditAccount confirmDeduct(Long learnerId, Integer amount) {
        CreditAccount account = openAccount(learnerId);
        if (account.getFrozenCredits() < amount) {
            throw new BusinessException("冻结积分不足，无法完成扣款");
        }
        account.setFrozenCredits(account.getFrozenCredits() - amount);
        account.setTotalCredits(account.getTotalCredits() - amount);
        updateById(account);
        return account;
    }

    @Override
    @Transactional
    public CreditAccount refundCredits(Long learnerId, Integer amount) {
        CreditAccount account = openAccount(learnerId);
        account.setAvailableCredits(account.getAvailableCredits() + amount);
        account.setTotalCredits(account.getTotalCredits() + amount);
        updateById(account);
        return account;
    }

    /**
     * @deprecated 保留兼容，新代码直接内联写入 balanceBefore
     */
    @Deprecated
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
