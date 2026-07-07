package org.csu.creditbank.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.csu.creditbank.entity.Learner;
import org.csu.creditbank.mapper.LearnerMapper;
import org.csu.creditbank.service.LearnerService;
import org.springframework.stereotype.Service;

@Service
public class LearnerServiceImpl extends ServiceImpl<LearnerMapper, Learner> implements LearnerService {
}
