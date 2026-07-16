package org.csu.creditbank.controller.admin.edu;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.csu.creditbank.common.ApiResult;
import org.csu.creditbank.entity.Achievement;
import org.csu.creditbank.entity.LearningRecord;
import org.csu.creditbank.entity.SignInRecord;
import org.csu.creditbank.service.AchievementService;
import org.csu.creditbank.service.LearningRecordService;
import org.csu.creditbank.service.LearnerService;
import org.csu.creditbank.service.SignInRecordService;
import org.springframework.web.bind.annotation.*;

@RestController
public class EducationController {

    // ==================== 课程 ====================

    /* 课程 CRUD 在 CourseController，不在此列 */

    // ==================== 成果认定 ====================

    @RestController
    @RequestMapping("/api/admin/achievements")
    public static class AchievementRoutes {
        private final AchievementService achievementService;
        private final LearnerService learnerService;
        public AchievementRoutes(AchievementService s, LearnerService learnerService) {
            this.achievementService = s;
            this.learnerService = learnerService;
        }

        @GetMapping
        public ApiResult<Page<Achievement>> page(@RequestParam(defaultValue = "1") long current,
                                                  @RequestParam(defaultValue = "10") long size) {
            Page<Achievement> page = achievementService.page(Page.of(current, size));
            org.csu.creditbank.controller.admin.AdminLearnerNames.fill(page.getRecords(), Achievement::getLearnerId, Achievement::setLearnerName, learnerService);
            return ApiResult.ok(page);
        }
        @PostMapping
        public ApiResult<Achievement> create(@RequestBody Achievement a) { achievementService.save(a); return ApiResult.ok(a); }
        @PutMapping("/{id}")
        public ApiResult<Achievement> update(@PathVariable Long id, @RequestBody Achievement a) { a.setId(id); achievementService.updateById(a); return ApiResult.ok(a); }
    }

    // ==================== 学习记录 ====================

    @RestController
    @RequestMapping("/api/admin/learning-records")
    public static class LearningRecordRoutes {
        private final LearningRecordService learningRecordService;
        private final LearnerService learnerService;
        public LearningRecordRoutes(LearningRecordService s, LearnerService learnerService) {
            this.learningRecordService = s;
            this.learnerService = learnerService;
        }

        @GetMapping
        public ApiResult<Page<LearningRecord>> page(@RequestParam(defaultValue = "1") long current,
                                                     @RequestParam(defaultValue = "10") long size) {
            Page<LearningRecord> page = learningRecordService.page(Page.of(current, size));
            org.csu.creditbank.controller.admin.AdminLearnerNames.fill(page.getRecords(), LearningRecord::getLearnerId, LearningRecord::setLearnerName, learnerService);
            return ApiResult.ok(page);
        }
        @PostMapping
        public ApiResult<LearningRecord> create(@RequestBody LearningRecord r) { learningRecordService.save(r); return ApiResult.ok(r); }
        @PutMapping("/{id}")
        public ApiResult<LearningRecord> update(@PathVariable Long id, @RequestBody LearningRecord r) { r.setId(id); learningRecordService.updateById(r); return ApiResult.ok(r); }
    }

    // ==================== 签到记录 ====================

    @RestController
    @RequestMapping("/api/admin/sign-ins")
    public static class SignInRecordRoutes {
        private final SignInRecordService signInRecordService;
        private final LearnerService learnerService;
        public SignInRecordRoutes(SignInRecordService s, LearnerService learnerService) {
            this.signInRecordService = s;
            this.learnerService = learnerService;
        }

        @GetMapping
        public ApiResult<Page<SignInRecord>> page(@RequestParam(defaultValue = "1") long current,
                                                   @RequestParam(defaultValue = "10") long size) {
            Page<SignInRecord> page = signInRecordService.page(Page.of(current, size));
            org.csu.creditbank.controller.admin.AdminLearnerNames.fill(page.getRecords(), SignInRecord::getLearnerId, SignInRecord::setLearnerName, learnerService);
            return ApiResult.ok(page);
        }
        @PostMapping
        public ApiResult<SignInRecord> create(@RequestBody SignInRecord r) { signInRecordService.save(r); return ApiResult.ok(r); }
    }
}
