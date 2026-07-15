package org.csu.creditbank.controller.admin;

import org.csu.creditbank.common.ApiResult;
import org.csu.creditbank.dto.CourseRanking;
import org.csu.creditbank.dto.DashboardStats;
import org.csu.creditbank.dto.IntegrityDistribution;
import org.csu.creditbank.dto.RevenueTrend;
import org.csu.creditbank.entity.*;
import org.csu.creditbank.service.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/dashboard")
public class DashboardController {

    private final LearnerService learnerService;
    private final CourseService courseService;
    private final AchievementService achievementService;
    private final CreditTransactionService creditTransactionService;
    private final ForumPostService forumPostService;
    private final JobPostingService jobPostingService;
    private final CreditOrderService creditOrderService;
    private final IntegrityRatingService integrityRatingService;
    private final InstitutionService institutionService;
    private final LearningRecordService learningRecordService;

    public DashboardController(LearnerService learnerService,
                               CourseService courseService,
                               AchievementService achievementService,
                               CreditTransactionService creditTransactionService,
                               ForumPostService forumPostService,
                               JobPostingService jobPostingService,
                               CreditOrderService creditOrderService,
                               IntegrityRatingService integrityRatingService,
                               InstitutionService institutionService,
                               LearningRecordService learningRecordService) {
        this.learnerService = learnerService;
        this.courseService = courseService;
        this.achievementService = achievementService;
        this.creditTransactionService = creditTransactionService;
        this.forumPostService = forumPostService;
        this.jobPostingService = jobPostingService;
        this.creditOrderService = creditOrderService;
        this.integrityRatingService = integrityRatingService;
        this.institutionService = institutionService;
        this.learningRecordService = learningRecordService;
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

    /** 订单营收趋势：近7日每日营收 + 订单状态分布 */
    @GetMapping("/revenue")
    public ApiResult<RevenueTrend> revenue() {
        // --- 1. 近7日每日营收（已支付订单） ---
        LocalDate today = LocalDate.now();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MM-dd");
        List<Map<String, Object>> daily = new ArrayList<>();

        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            long amount = creditOrderService.lambdaQuery()
                    .eq(CreditOrder::getOrderStatus, "PAID")
                    .ge(CreditOrder::getPaidAt, date.atStartOfDay())
                    .lt(CreditOrder::getPaidAt, date.plusDays(1).atStartOfDay())
                    .list()
                    .stream()
                    .mapToLong(o -> o.getCreditAmount() != null ? o.getCreditAmount() : 0)
                    .sum();
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("date", date.format(fmt));
            item.put("amount", amount);
            daily.add(item);
        }

        // --- 2. 订单状态分布（饼图） ---
        List<Map<String, Object>> statusDist = new ArrayList<>();
        String[] statuses = {"PAID", "PENDING", "COMPLETED", "CANCELLED", "DELIVERED"};
        String[] labels   = {"已支付", "待支付", "已完成", "已取消", "已发货"};

        for (int i = 0; i < statuses.length; i++) {
            long cnt = creditOrderService.lambdaQuery()
                    .eq(CreditOrder::getOrderStatus, statuses[i])
                    .count();
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("status", labels[i]);
            item.put("count", cnt);
            statusDist.add(item);
        }

        return ApiResult.ok(new RevenueTrend(daily, statusDist));
    }

    /** 诚信评分分布：等级分布 + 各机构平均分 */
    @GetMapping("/integrity")
    public ApiResult<IntegrityDistribution> integrity() {
        // --- 1. A/B/C/D 等级分布 ---
        List<Map<String, Object>> levelDist = new ArrayList<>();
        String[] levels = {"A", "B", "C", "D"};
        for (String level : levels) {
            List<IntegrityRating> list = integrityRatingService.lambdaQuery()
                    .eq(IntegrityRating::getLevelName, level).list();
            long count = list.size();
            double avgScore = list.stream()
                    .mapToInt(r -> r.getScore() != null ? r.getScore() : 0)
                    .average().orElse(0);
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("level", level);
            item.put("count", count);
            item.put("avgScore", Math.round(avgScore * 10) / 10.0);
            levelDist.add(item);
        }

        // --- 2. 各机构平均诚信分 ---
        List<Map<String, Object>> byInst = new ArrayList<>();
        List<Institution> institutions = institutionService.list();
        for (Institution inst : institutions) {
            List<IntegrityRating> ratings = integrityRatingService.lambdaQuery()
                    .eq(IntegrityRating::getInstitutionId, inst.getId()).list();
            if (ratings.isEmpty()) continue;
            double avgScore = ratings.stream()
                    .mapToInt(r -> r.getScore() != null ? r.getScore() : 0)
                    .average().orElse(0);
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("name", inst.getName());
            item.put("avgScore", Math.round(avgScore * 10) / 10.0);
            byInst.add(item);
        }

        return ApiResult.ok(new IntegrityDistribution(levelDist, byInst));
    }

    /** 课程学习排行榜：按完成人数 Top 10 */
    @GetMapping("/course-ranking")
    public ApiResult<CourseRanking> courseRanking() {
        // 按 courseId 聚合学习记录中已通过的条数，取 Top 10
        List<Map<String, Object>> topCourses = learningRecordService.lambdaQuery()
                .eq(LearningRecord::getResult, "PASSED")
                .list()
                .stream()
                .collect(Collectors.groupingBy(LearningRecord::getCourseId, Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.<Long, Long>comparingByValue().reversed())
                .limit(10)
                .map(e -> {
                    Course course = courseService.getById(e.getKey());
                    String courseName = course != null ? course.getCourseName() : "未知课程 " + e.getKey();
                    String instName = "";
                    if (course != null && course.getInstitutionId() != null) {
                        Institution inst = institutionService.getById(course.getInstitutionId());
                        if (inst != null) instName = inst.getName();
                    }
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("courseName", courseName);
                    item.put("learnerCount", e.getValue());
                    item.put("institutionName", instName);
                    return item;
                })
                .collect(Collectors.toList());

        return ApiResult.ok(new CourseRanking(topCourses));
    }
}
