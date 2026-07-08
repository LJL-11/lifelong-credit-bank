package org.csu.creditbank.controller.admin.edu;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.csu.creditbank.common.ApiResult;
import org.csu.creditbank.common.BusinessException;
import org.csu.creditbank.dto.CreditChangeRequest;
import org.csu.creditbank.entity.Achievement;
import org.csu.creditbank.entity.Course;
import org.csu.creditbank.entity.LearningRecord;
import org.csu.creditbank.entity.SignInRecord;
import org.csu.creditbank.service.*;
import org.springframework.web.bind.annotation.*;

@RestController
public class EducationController {

    // ==================== 成果认定 ====================

    @RestController
    @RequestMapping("/api/admin/achievements")
    public static class AchievementRoutes {
        private final AchievementService achievementService;
        public AchievementRoutes(AchievementService s) { this.achievementService = s; }

        @GetMapping
        public ApiResult<Page<Achievement>> page(@RequestParam(defaultValue = "1") long current,
                                                  @RequestParam(defaultValue = "10") long size) {
            return ApiResult.ok(achievementService.page(Page.of(current, size)));
        }
        @PostMapping
        public ApiResult<Achievement> create(@RequestBody Achievement a) { achievementService.save(a); return ApiResult.ok(a); }
        @PutMapping("/{id}")
        public ApiResult<Achievement> update(@PathVariable Long id, @RequestBody Achievement a) { a.setId(id); achievementService.updateById(a); return ApiResult.ok(a); }
    }

    // ==================== 学习记录（完成课程自动获取得分） ====================

    @RestController
    @RequestMapping("/api/admin/learning-records")
    public static class LearningRecordRoutes {
        private final LearningRecordService learningRecordService;
        private final CreditAccountService creditAccountService;
        private final CourseService courseService;

        public LearningRecordRoutes(LearningRecordService learningRecordService,
                                     CreditAccountService creditAccountService,
                                     CourseService courseService) {
            this.learningRecordService = learningRecordService;
            this.creditAccountService = creditAccountService;
            this.courseService = courseService;
        }

        @GetMapping
        public ApiResult<Page<LearningRecord>> page(@RequestParam(defaultValue = "1") long current,
                                                     @RequestParam(defaultValue = "10") long size) {
            return ApiResult.ok(learningRecordService.page(Page.of(current, size)));
        }

        @PostMapping
        public ApiResult<LearningRecord> create(@RequestBody LearningRecord r) {
            learningRecordService.save(r);
            awardCreditsIfPassed(r);
            return ApiResult.ok(r);
        }

        @PutMapping("/{id}")
        public ApiResult<LearningRecord> update(@PathVariable Long id, @RequestBody LearningRecord r) {
            LearningRecord old = learningRecordService.getById(id);
            r.setId(id);
            learningRecordService.updateById(r);

            boolean justPassed = "PASSED".equals(r.getResult()) && (old == null || !"PASSED".equals(old.getResult()));
            if (justPassed) {
                awardCreditsIfPassed(r);
            }
            return ApiResult.ok(r);
        }

        /** 完成课程 → 获取得分（与积分商城独立，积分来自机构学习） */
        private void awardCreditsIfPassed(LearningRecord r) {
            if (!"PASSED".equals(r.getResult())) return;
            if (r.getLearnerId() == null) return;

            int reward = 10; // 默认奖励
            if (r.getCourseId() != null) {
                Course course = courseService.getById(r.getCourseId());
                if (course != null && course.getCreditPoint() != null && course.getCreditPoint() > 0) {
                    reward = course.getCreditPoint();
                }
            }

            CreditChangeRequest req = new CreditChangeRequest();
            req.setLearnerId(r.getLearnerId());
            req.setAmount(reward);
            req.setSourceType("COURSE_COMPLETE");
            req.setSourceNo("COURSE-" + r.getCourseId());
            req.setRemark("完成课程获奖得分");
            creditAccountService.increaseCredits(req);
        }
    }

    // ==================== 签到记录 ====================

    @RestController
    @RequestMapping("/api/admin/sign-ins")
    public static class SignInRecordRoutes {
        private final SignInRecordService signInRecordService;
        public SignInRecordRoutes(SignInRecordService s) { this.signInRecordService = s; }

        @GetMapping
        public ApiResult<Page<SignInRecord>> page(@RequestParam(defaultValue = "1") long current,
                                                   @RequestParam(defaultValue = "10") long size) {
            return ApiResult.ok(signInRecordService.page(Page.of(current, size)));
        }
        @PostMapping
        public ApiResult<SignInRecord> create(@RequestBody SignInRecord r) { signInRecordService.save(r); return ApiResult.ok(r); }
    }
}
