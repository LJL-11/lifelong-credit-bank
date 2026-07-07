package org.csu.creditbank.controller.admin;

import org.csu.creditbank.common.ApiResult;
import org.csu.creditbank.dto.DashboardStats;
import org.csu.creditbank.service.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/dashboard")
public class DashboardController {

    private final LearnerService learnerService;
    private final CourseService courseService;
    private final AchievementService achievementService;
    private final CreditTransactionService creditTransactionService;
    private final ForumPostService forumPostService;
    private final JobPostingService jobPostingService;

    public DashboardController(LearnerService learnerService,
                               CourseService courseService,
                               AchievementService achievementService,
                               CreditTransactionService creditTransactionService,
                               ForumPostService forumPostService,
                               JobPostingService jobPostingService) {
        this.learnerService = learnerService;
        this.courseService = courseService;
        this.achievementService = achievementService;
        this.creditTransactionService = creditTransactionService;
        this.forumPostService = forumPostService;
        this.jobPostingService = jobPostingService;
    }

    @GetMapping("/stats")
    public ApiResult<DashboardStats> stats() {
        return ApiResult.ok(new DashboardStats(
                learnerService.count(),
                courseService.count(),
                achievementService.count(),
                creditTransactionService.count(),
                forumPostService.count(),
                jobPostingService.count()
        ));
    }
}
