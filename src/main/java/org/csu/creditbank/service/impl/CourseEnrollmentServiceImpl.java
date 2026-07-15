package org.csu.creditbank.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.csu.creditbank.entity.Course;
import org.csu.creditbank.entity.CourseEnrollment;
import org.csu.creditbank.entity.CreditProduct;
import org.csu.creditbank.entity.Learner;
import org.csu.creditbank.mapper.CourseEnrollmentMapper;
import org.csu.creditbank.service.CourseEnrollmentService;
import org.csu.creditbank.service.CourseService;
import org.csu.creditbank.service.CreditProductService;
import org.csu.creditbank.service.LearnerService;
import org.csu.creditbank.util.InstitutionContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class CourseEnrollmentServiceImpl extends ServiceImpl<CourseEnrollmentMapper, CourseEnrollment> implements CourseEnrollmentService {

    private final CreditProductService productService;
    private final CourseService courseService;
    private final LearnerService learnerService;

    public CourseEnrollmentServiceImpl(CreditProductService productService,
                                       CourseService courseService,
                                       LearnerService learnerService) {
        this.productService = productService;
        this.courseService = courseService;
        this.learnerService = learnerService;
    }

    @Override
    @Transactional
    public CourseEnrollment grantPurchasedCourse(Long learnerId, Long productId, String remark) {
        CreditProduct product = productService.getById(productId);
        Course course = findPurchasedCourse(product);
        if (course == null) return null;

        CourseEnrollment existing = lambdaQuery()
                .eq(CourseEnrollment::getLearnerId, learnerId)
                .eq(CourseEnrollment::getCourseId, course.getId())
                .one();
        if (existing != null) {
            if (!"APPROVED".equals(existing.getEnrollStatus())) {
                existing.setEnrollStatus("APPROVED");
                existing.setReviewedAt(LocalDateTime.now());
                existing.setReviewer("SYSTEM");
                existing.setRemark(remark);
                fillInstitution(existing, learnerId, course);
                updateById(existing);
            }
            return existing;
        }

        CourseEnrollment enrollment = new CourseEnrollment();
        enrollment.setLearnerId(learnerId);
        enrollment.setCourseId(course.getId());
        enrollment.setEnrollStatus("APPROVED");
        enrollment.setReviewer("SYSTEM");
        enrollment.setReviewedAt(LocalDateTime.now());
        enrollment.setRemark(remark);
        fillInstitution(enrollment, learnerId, course);
        save(enrollment);
        return enrollment;
    }

    private void fillInstitution(CourseEnrollment enrollment, Long learnerId, Course course) {
        Learner learner = learnerService.getById(learnerId);
        if (learner != null && learner.getInstitutionId() != null) {
            enrollment.setInstitutionId(learner.getInstitutionId());
        } else {
            enrollment.setInstitutionId(course.getInstitutionId());
        }
    }

    private Course findPurchasedCourse(CreditProduct product) {
        Long tenantId = InstitutionContext.get();
        try {
            InstitutionContext.clear();
            return findPurchasedCourseWithoutTenant(product);
        } finally {
            if (tenantId != null) InstitutionContext.set(tenantId);
        }
    }

    private Course findPurchasedCourseWithoutTenant(CreditProduct product) {
        if (product == null || !"COURSE".equals(product.getProductType())) return null;
        if (product.getCourseId() != null) {
            Course course = courseService.getById(product.getCourseId());
            if (course != null && "PUBLISHED".equals(course.getStatus())) return course;
        }
        String courseCode = switch (product.getProductCode() == null ? "" : product.getProductCode()) {
            default -> product.getProductCode() != null && product.getProductCode().startsWith("COURSE-")
                    ? product.getProductCode().substring("COURSE-".length()) : null;
        };
        if (courseCode != null) {
            Course course = courseService.lambdaQuery()
                    .eq(Course::getCourseCode, courseCode)
                    .eq(Course::getStatus, "PUBLISHED")
                    .one();
            if (course != null) return course;
        }
        return courseService.lambdaQuery()
                .like(Course::getCourseName, product.getProductName())
                .eq(Course::getStatus, "PUBLISHED")
                .last("LIMIT 1")
                .one();
    }
}
