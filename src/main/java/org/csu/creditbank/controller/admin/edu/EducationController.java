package org.csu.creditbank.controller.admin.edu;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.csu.creditbank.common.ApiResult;
import org.csu.creditbank.entity.Achievement;
import org.csu.creditbank.entity.LearningRecord;
import org.csu.creditbank.entity.SignInRecord;
import org.csu.creditbank.service.AchievementService;
import org.csu.creditbank.service.LearningRecordService;
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

    // ==================== 学习记录 ====================

    @RestController
    @RequestMapping("/api/admin/learning-records")
    public static class LearningRecordRoutes {
        private final LearningRecordService learningRecordService;
        public LearningRecordRoutes(LearningRecordService s) { this.learningRecordService = s; }

        @GetMapping
        public ApiResult<Page<LearningRecord>> page(@RequestParam(defaultValue = "1") long current,
                                                     @RequestParam(defaultValue = "10") long size) {
            return ApiResult.ok(learningRecordService.page(Page.of(current, size)));
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
