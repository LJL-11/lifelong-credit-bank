package org.csu.creditbank.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.csu.creditbank.entity.Institution;
import org.csu.creditbank.mapper.InstitutionMapper;
import org.csu.creditbank.service.InstitutionService;
import org.springframework.stereotype.Service;

@Service
public class InstitutionServiceImpl extends ServiceImpl<InstitutionMapper, Institution> implements InstitutionService {
}
