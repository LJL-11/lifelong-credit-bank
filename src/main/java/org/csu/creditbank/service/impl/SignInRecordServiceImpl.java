package org.csu.creditbank.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.csu.creditbank.entity.SignInRecord;
import org.csu.creditbank.mapper.SignInRecordMapper;
import org.csu.creditbank.service.SignInRecordService;
import org.springframework.stereotype.Service;

@Service
public class SignInRecordServiceImpl extends ServiceImpl<SignInRecordMapper, SignInRecord> implements SignInRecordService {
}
