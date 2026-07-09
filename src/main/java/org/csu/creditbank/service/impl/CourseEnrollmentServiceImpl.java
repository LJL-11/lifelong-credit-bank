package org.csu.creditbank.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.csu.creditbank.entity.CourseEnrollment;
import org.csu.creditbank.mapper.CourseEnrollmentMapper;
import org.csu.creditbank.service.CourseEnrollmentService;
import org.springframework.stereotype.Service;

@Service
public class CourseEnrollmentServiceImpl extends ServiceImpl<CourseEnrollmentMapper, CourseEnrollment> implements CourseEnrollmentService {
}
