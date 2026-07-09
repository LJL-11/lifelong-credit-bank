package org.csu.creditbank.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.csu.creditbank.entity.JobApplication;
import org.csu.creditbank.mapper.JobApplicationMapper;
import org.csu.creditbank.service.JobApplicationService;
import org.springframework.stereotype.Service;

@Service
public class JobApplicationServiceImpl extends ServiceImpl<JobApplicationMapper, JobApplication> implements JobApplicationService {
}
