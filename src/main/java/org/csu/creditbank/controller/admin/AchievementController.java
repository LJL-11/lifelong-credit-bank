package org.csu.creditbank.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.csu.creditbank.common.ApiResult;
import org.csu.creditbank.entity.Achievement;
import org.csu.creditbank.service.AchievementService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/achievements")
public class AchievementController {

    private final AchievementService achievementService;

    public AchievementController(AchievementService achievementService) {
        this.achievementService = achievementService;
    }

    @GetMapping
    public ApiResult<Page<Achievement>> page(@RequestParam(defaultValue = "1") long current,
                                             @RequestParam(defaultValue = "10") long size) {
        return ApiResult.ok(achievementService.page(Page.of(current, size)));
    }

    @PostMapping
    public ApiResult<Achievement> create(@RequestBody Achievement achievement) {
        achievementService.save(achievement);
        return ApiResult.ok(achievement);
    }

    @PutMapping("/{id}")
    public ApiResult<Achievement> update(@PathVariable Long id, @RequestBody Achievement achievement) {
        achievement.setId(id);
        achievementService.updateById(achievement);
        return ApiResult.ok(achievement);
    }
}
