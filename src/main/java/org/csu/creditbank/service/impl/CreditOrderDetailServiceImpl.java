package org.csu.creditbank.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.csu.creditbank.entity.CreditOrderDetail;
import org.csu.creditbank.mapper.CreditOrderDetailMapper;
import org.csu.creditbank.service.CreditOrderDetailService;
import org.springframework.stereotype.Service;

@Service
public class CreditOrderDetailServiceImpl extends ServiceImpl<CreditOrderDetailMapper, CreditOrderDetail> implements CreditOrderDetailService {
}
