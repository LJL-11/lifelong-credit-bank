package org.csu.creditbank.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.csu.creditbank.entity.JobPosting;
import org.csu.creditbank.mapper.JobPostingMapper;
import org.csu.creditbank.service.JobPostingService;
import org.springframework.stereotype.Service;

@Service
public class JobPostingServiceImpl extends ServiceImpl<JobPostingMapper, JobPosting> implements JobPostingService {
}
