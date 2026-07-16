package org.csu.creditbank.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.servlet.http.HttpServletRequest;
import org.csu.creditbank.common.ApiResult;
import org.csu.creditbank.common.BusinessException;
import org.csu.creditbank.dto.CreditChangeRequest;
import org.csu.creditbank.entity.*;
import org.csu.creditbank.service.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/core")
public class CoreBusinessController {

    private final CourseEnrollmentService enrollmentService;
    private final AchievementService achievementService;
    private final CreditAccountService accountService;
    private final BlockchainCredentialService credentialService;
    private final JobApplicationService jobApplicationService;
    private final IntegrityRatingService integrityRatingService;
    private final LearnerService learnerService;

    public CoreBusinessController(CourseEnrollmentService enrollmentService,
                                  AchievementService achievementService,
                                  CreditAccountService accountService,
                                  BlockchainCredentialService credentialService,
                                  JobApplicationService jobApplicationService,
                                  IntegrityRatingService integrityRatingService,
                                  LearnerService learnerService) {
        this.enrollmentService = enrollmentService;
        this.achievementService = achievementService;
        this.accountService = accountService;
        this.credentialService = credentialService;
        this.jobApplicationService = jobApplicationService;
        this.integrityRatingService = integrityRatingService;
        this.learnerService = learnerService;
    }

    @GetMapping("/enrollments")
    public ApiResult<Page<CourseEnrollment>> enrollments(@RequestParam(defaultValue = "1") long current,
                                                          @RequestParam(defaultValue = "10") long size) {
        Page<CourseEnrollment> page = enrollmentService.lambdaQuery()
                .orderByDesc(CourseEnrollment::getCreatedAt)
                .page(Page.of(current, size));
        AdminLearnerNames.fill(page.getRecords(), CourseEnrollment::getLearnerId, CourseEnrollment::setLearnerName, learnerService);
        return ApiResult.ok(page);
    }

    @PutMapping("/enrollments/{id}/review")
    public ApiResult<CourseEnrollment> reviewEnrollment(@PathVariable Long id, @RequestBody Map<String, String> body, HttpServletRequest request) {
        String status = body.get("status");
        if (!"APPROVED".equals(status) && !"REJECTED".equals(status)) throw new BusinessException("状态只允许 APPROVED / REJECTED");
        CourseEnrollment enrollment = enrollmentService.getById(id);
        if (enrollment == null) throw new BusinessException("报名记录不存在");
        enrollment.setEnrollStatus(status);
        enrollment.setReviewer(String.valueOf(request.getAttribute("userId")));
        enrollment.setReviewedAt(LocalDateTime.now());
        enrollment.setRemark(body.get("remark"));
        enrollmentService.updateById(enrollment);
        return ApiResult.ok(enrollment);
    }

    @PutMapping("/achievements/{id}/review")
    @Transactional
    public ApiResult<Achievement> reviewAchievement(@PathVariable Long id, @RequestBody Map<String, String> body, HttpServletRequest request) {
        String status = body.get("status");
        if (!"APPROVED".equals(status) && !"REJECTED".equals(status)) throw new BusinessException("状态只允许 APPROVED / REJECTED");
        Achievement achievement = achievementService.getById(id);
        if (achievement == null) throw new BusinessException("成果不存在");
        achievement.setAuditStatus(status);
        achievement.setAuditor(String.valueOf(request.getAttribute("userId")));
        achievement.setAuditedAt(LocalDateTime.now());
        achievement.setRejectReason(body.get("rejectReason"));
        achievementService.updateById(achievement);

        if ("APPROVED".equals(status) && achievement.getSuggestedCredits() != null && achievement.getSuggestedCredits() > 0) {
            CreditChangeRequest reward = new CreditChangeRequest();
            reward.setLearnerId(achievement.getLearnerId());
            reward.setAmount(achievement.getSuggestedCredits());
            reward.setSourceType("ACHIEVEMENT_APPROVED");
            reward.setSourceNo("ACH-" + achievement.getId());
            reward.setRemark("成果认定通过：《" + achievement.getAchievementName() + "》");
            accountService.increaseCredits(reward);
            String payload = achievement.getLearnerId() + "|" + achievement.getAchievementName() + "|" + achievement.getAchievementType() + "|" + achievement.getSuggestedCredits();
            credentialService.createCredential(achievement.getLearnerId(), "ACHIEVEMENT", "ACH-" + achievement.getId(), payload, achievement.getInstitutionId());
        }
        integrityRatingService.recompute(achievement.getLearnerId());
        return ApiResult.ok(achievement);
    }

    @GetMapping("/job-applications")
    public ApiResult<Page<JobApplication>> jobApplications(@RequestParam(defaultValue = "1") long current,
                                                            @RequestParam(defaultValue = "10") long size) {
        Page<JobApplication> page = jobApplicationService.lambdaQuery()
                .orderByDesc(JobApplication::getCreatedAt)
                .page(Page.of(current, size));
        AdminLearnerNames.fill(page.getRecords(), JobApplication::getLearnerId, JobApplication::setLearnerName, learnerService);
        return ApiResult.ok(page);
    }

    @PutMapping("/job-applications/{id}/review")
    public ApiResult<JobApplication> reviewJobApplication(@PathVariable Long id, @RequestBody Map<String, String> body, HttpServletRequest request) {
        String status = body.get("status");
        if (!"VIEWED".equals(status) && !"ACCEPTED".equals(status) && !"REJECTED".equals(status)) {
            throw new BusinessException("状态只允许 VIEWED / ACCEPTED / REJECTED");
        }
        JobApplication application = jobApplicationService.getById(id);
        if (application == null) throw new BusinessException("投递记录不存在");
        application.setApplyStatus(status);
        application.setReviewer(String.valueOf(request.getAttribute("userId")));
        application.setReviewedAt(LocalDateTime.now());
        application.setRemark(body.get("remark"));
        jobApplicationService.updateById(application);
        integrityRatingService.recompute(application.getLearnerId());
        return ApiResult.ok(application);
    }

    @GetMapping("/integrity-ratings")
    public ApiResult<Page<IntegrityRating>> integrityRatings(@RequestParam(defaultValue = "1") long current,
                                                              @RequestParam(defaultValue = "10") long size) {
        Page<IntegrityRating> page = integrityRatingService.page(Page.of(current, size));
        AdminLearnerNames.fill(page.getRecords(), IntegrityRating::getLearnerId, IntegrityRating::setLearnerName, learnerService);
        return ApiResult.ok(page);
    }

    @PostMapping("/integrity-ratings/{learnerId}/recompute")
    public ApiResult<IntegrityRating> recomputeIntegrity(@PathVariable Long learnerId) {
        IntegrityRating rating = integrityRatingService.recompute(learnerId);
        AdminLearnerNames.fill(java.util.List.of(rating), IntegrityRating::getLearnerId, IntegrityRating::setLearnerName, learnerService);
        return ApiResult.ok(rating);
    }
}
