package org.csu.creditbank.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import java.util.List;
import java.util.Map;
import jakarta.servlet.http.HttpServletRequest;
import org.csu.creditbank.common.ApiResult;
import org.csu.creditbank.common.BusinessException;
import org.csu.creditbank.dto.CreditChangeRequest;
import org.csu.creditbank.entity.*;
import org.csu.creditbank.service.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/student")
public class StudentBusinessController {

    private final CourseService courseService;
    private final CourseEnrollmentService enrollmentService;
    private final SignInRecordService signInRecordService;
    private final CreditAccountService accountService;
    private final AchievementService achievementService;
    private final JobPostingService jobPostingService;
    private final JobApplicationService jobApplicationService;
    private final IntegrityRatingService integrityRatingService;

    public StudentBusinessController(CourseService courseService,
                                     CourseEnrollmentService enrollmentService,
                                     SignInRecordService signInRecordService,
                                     CreditAccountService accountService,
                                     AchievementService achievementService,
                                     JobPostingService jobPostingService,
                                     JobApplicationService jobApplicationService,
                                     IntegrityRatingService integrityRatingService) {
        this.courseService = courseService;
        this.enrollmentService = enrollmentService;
        this.signInRecordService = signInRecordService;
        this.accountService = accountService;
        this.achievementService = achievementService;
        this.jobPostingService = jobPostingService;
        this.jobApplicationService = jobApplicationService;
        this.integrityRatingService = integrityRatingService;
    }

    @PostMapping("/courses/{courseId}/enroll")
    public ApiResult<CourseEnrollment> enrollCourse(@PathVariable Long courseId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Course course = courseService.getById(courseId);
        if (course == null || !"PUBLISHED".equals(course.getStatus())) throw new BusinessException("课程不存在或未发布");
        CourseEnrollment existing = enrollmentService.lambdaQuery()
                .eq(CourseEnrollment::getLearnerId, userId)
                .eq(CourseEnrollment::getCourseId, courseId)
                .one();
        if (existing != null) return ApiResult.ok(existing);
        CourseEnrollment enrollment = new CourseEnrollment();
        enrollment.setLearnerId(userId);
        enrollment.setCourseId(courseId);
        enrollment.setEnrollStatus("PENDING");
        enrollment.setInstitutionId((Long) request.getAttribute("institutionId"));
        enrollmentService.save(enrollment);
        return ApiResult.ok(enrollment);
    }

    @GetMapping("/courses/my")
    public ApiResult<Page<Course>> myCourses(@RequestParam(defaultValue = "1") long current,
                                             @RequestParam(defaultValue = "10") long size,
                                             HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        List<CourseEnrollment> enrollments = enrollmentService.lambdaQuery()
                .eq(CourseEnrollment::getLearnerId, userId)
                .orderByDesc(CourseEnrollment::getCreatedAt)
                .list();
        if (enrollments.isEmpty()) {
            return ApiResult.ok(Page.of(current, size));
        }
        Map<Long, String> statusByCourse = enrollments.stream()
                .collect(java.util.stream.Collectors.toMap(CourseEnrollment::getCourseId, CourseEnrollment::getEnrollStatus, (a, b) -> a));
        List<Long> courseIds = enrollments.stream().map(CourseEnrollment::getCourseId).distinct().toList();
        Page<Course> page = courseService.lambdaQuery()
                .in(Course::getId, courseIds)
                .orderByDesc(Course::getCreatedAt)
                .page(Page.of(current, size));
        page.getRecords().forEach(course -> course.setEnrollStatus(statusByCourse.get(course.getId())));
        return ApiResult.ok(page);
    }

    @GetMapping("/enrollments")
    public ApiResult<Page<CourseEnrollment>> myEnrollments(@RequestParam(defaultValue = "1") long current,
                                                            @RequestParam(defaultValue = "10") long size,
                                                            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return ApiResult.ok(enrollmentService.lambdaQuery()
                .eq(CourseEnrollment::getLearnerId, userId)
                .orderByDesc(CourseEnrollment::getCreatedAt)
                .page(Page.of(current, size)));
    }

    @PostMapping("/sign-ins/today")
    @Transactional
    public ApiResult<SignInRecord> signToday(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        LocalDate today = LocalDate.now();
        long exists = signInRecordService.lambdaQuery()
                .eq(SignInRecord::getLearnerId, userId)
                .eq(SignInRecord::getSignDate, today)
                .count();
        if (exists > 0) throw new BusinessException("今天已经签到");
        SignInRecord record = new SignInRecord();
        record.setLearnerId(userId);
        record.setSignDate(today);
        record.setRewardCredits(2);
        record.setSignType("DAILY");
        record.setInstitutionId((Long) request.getAttribute("institutionId"));
        signInRecordService.save(record);

        CreditChangeRequest reward = new CreditChangeRequest();
        reward.setLearnerId(userId);
        reward.setAmount(2);
        reward.setSourceType("DAILY_SIGNIN");
        reward.setSourceNo("SIGN-" + today);
        reward.setRemark("每日签到奖励");
        accountService.increaseCredits(reward);
        integrityRatingService.recompute(userId);
        return ApiResult.ok(record);
    }

    @GetMapping("/sign-ins")
    public ApiResult<Page<SignInRecord>> mySignIns(@RequestParam(defaultValue = "1") long current,
                                                    @RequestParam(defaultValue = "10") long size,
                                                    HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return ApiResult.ok(signInRecordService.lambdaQuery()
                .eq(SignInRecord::getLearnerId, userId)
                .orderByDesc(SignInRecord::getSignDate)
                .page(Page.of(current, size)));
    }

    @PostMapping("/achievements")
    public ApiResult<Achievement> submitAchievement(@RequestBody Achievement achievement, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        achievement.setLearnerId(userId);
        achievement.setAuditStatus("PENDING");
        achievement.setAuditor(null);
        achievement.setAuditedAt(null);
        achievement.setInstitutionId((Long) request.getAttribute("institutionId"));
        achievementService.save(achievement);
        return ApiResult.ok(achievement);
    }

    @GetMapping("/achievements")
    public ApiResult<Page<Achievement>> myAchievements(@RequestParam(defaultValue = "1") long current,
                                                        @RequestParam(defaultValue = "10") long size,
                                                        HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return ApiResult.ok(achievementService.lambdaQuery()
                .eq(Achievement::getLearnerId, userId)
                .orderByDesc(Achievement::getCreatedAt)
                .page(Page.of(current, size)));
    }

    @GetMapping("/jobs")
    public ApiResult<Page<JobPosting>> jobs(@RequestParam(defaultValue = "1") long current,
                                             @RequestParam(defaultValue = "10") long size) {
        return ApiResult.ok(jobPostingService.lambdaQuery()
                .eq(JobPosting::getStatus, "OPEN")
                .orderByDesc(JobPosting::getCreatedAt)
                .page(Page.of(current, size)));
    }

    @PostMapping("/jobs/{jobId}/apply")
    public ApiResult<JobApplication> applyJob(@PathVariable Long jobId, @RequestBody Map<String, String> body, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        JobPosting job = jobPostingService.getById(jobId);
        if (job == null || !"OPEN".equals(job.getStatus())) throw new BusinessException("岗位不存在或已关闭");
        JobApplication existing = jobApplicationService.lambdaQuery()
                .eq(JobApplication::getJobId, jobId)
                .eq(JobApplication::getLearnerId, userId)
                .one();
        if (existing != null) return ApiResult.ok(existing);
        JobApplication application = new JobApplication();
        application.setJobId(jobId);
        application.setLearnerId(userId);
        application.setResumeSummary(body.get("resumeSummary"));
        application.setResumeUrl(body.get("resumeUrl"));
        application.setResumeFileName(body.get("resumeFileName"));
        application.setApplyStatus("SUBMITTED");
        application.setInstitutionId((Long) request.getAttribute("institutionId"));
        jobApplicationService.save(application);
        integrityRatingService.recompute(userId);
        return ApiResult.ok(application);
    }

    @GetMapping("/job-applications")
    public ApiResult<Page<JobApplication>> myApplications(@RequestParam(defaultValue = "1") long current,
                                                           @RequestParam(defaultValue = "10") long size,
                                                           HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return ApiResult.ok(jobApplicationService.lambdaQuery()
                .eq(JobApplication::getLearnerId, userId)
                .orderByDesc(JobApplication::getCreatedAt)
                .page(Page.of(current, size)));
    }

    @GetMapping("/integrity")
    public ApiResult<IntegrityRating> integrity(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return ApiResult.ok(integrityRatingService.recompute(userId));
    }
}
