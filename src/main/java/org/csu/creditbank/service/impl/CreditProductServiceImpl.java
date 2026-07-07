package org.csu.creditbank.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.csu.creditbank.entity.CreditProduct;
import org.csu.creditbank.mapper.CreditProductMapper;
import org.csu.creditbank.service.CreditProductService;
import org.springframework.stereotype.Service;

@Service
public class CreditProductServiceImpl extends ServiceImpl<CreditProductMapper, CreditProduct> implements CreditProductService {
}
