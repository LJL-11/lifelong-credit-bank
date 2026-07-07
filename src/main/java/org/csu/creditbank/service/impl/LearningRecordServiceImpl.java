package org.csu.creditbank.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.csu.creditbank.entity.LearningRecord;
import org.csu.creditbank.mapper.LearningRecordMapper;
import org.csu.creditbank.service.LearningRecordService;
import org.springframework.stereotype.Service;

@Service
public class LearningRecordServiceImpl extends ServiceImpl<LearningRecordMapper, LearningRecord> implements LearningRecordService {
}
