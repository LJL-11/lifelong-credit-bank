package org.csu.creditbank.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.csu.creditbank.entity.CreditTransaction;
import org.csu.creditbank.mapper.CreditTransactionMapper;
import org.csu.creditbank.service.CreditTransactionService;
import org.springframework.stereotype.Service;

@Service
public class CreditTransactionServiceImpl extends ServiceImpl<CreditTransactionMapper, CreditTransaction> implements CreditTransactionService {
}
