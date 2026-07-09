package org.csu.creditbank.ai.tools;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import org.csu.creditbank.entity.*;
import org.csu.creditbank.service.*;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * AI 学习助手工具集 —— Function Calling 能力。
 * userId 由 LLM 从系统提示中获取并传入，不依赖 ThreadLocal。
 */
@Component
public class LearningTools {

    private final CourseService courseService;
    private final CreditAccountService accountService;
    private final CreditOrderService orderService;
    private final LearningRecordService learningRecordService;
    private final LearnerService learnerService;

    public LearningTools(CourseService courseService,
                         CreditAccountService accountService,
                         CreditOrderService orderService,
                         LearningRecordService learningRecordService,
                         LearnerService learnerService) {
        this.courseService = courseService;
        this.accountService = accountService;
        this.orderService = orderService;
        this.learningRecordService = learningRecordService;
        this.learnerService = learnerService;
    }

    @Tool(name = "search_course",
          value = "根据课程名称关键词搜索课程，返回匹配的课程列表（含课程编码、学分、积分等信息）")
    public String searchCourse(@P("课程名称关键词") String keyword) {
        List<Course> courses = courseService.lambdaQuery()
                .like(Course::getCourseName, keyword)
                .eq(Course::getStatus, "PUBLISHED")
                .list();
        if (courses.isEmpty()) {
            return "未找到与「" + keyword + "」相关的课程";
        }
        return courses.stream()
                .map(c -> String.format("【%s】编码:%s | 提供方:%s | 学分:%.1f | 积分:%d | 分类:%s",
                        c.getCourseName(), c.getCourseCode(), c.getProvider(),
                        c.getCreditValue(), c.getCreditPoint() != null ? c.getCreditPoint() : 0,
                        c.getCategory()))
                .collect(Collectors.joining("\n"));
    }

    @Tool(name = "get_my_credits",
          value = "查询用户的积分账户余额（总积分、可用积分、冻结积分）")
    public String getMyCredits(@P("用户ID，从系统提示中获取") Long userId) {
        CreditAccount account = accountService.lambdaQuery()
                .eq(CreditAccount::getLearnerId, userId).one();
        if (account == null) {
            return "尚未开通积分账户，请联系管理员开通";
        }
        return String.format("总积分:%d | 可用积分:%d | 冻结积分:%d | 状态:%s",
                account.getTotalCredits(), account.getAvailableCredits(),
                account.getFrozenCredits(), account.getAccountStatus());
    }

    @Tool(name = "get_my_progress",
          value = "查询用户已完成的学习记录，返回已学课程列表和进度概览")
    public String getMyProgress(@P("用户ID，从系统提示中获取") Long userId) {
        List<LearningRecord> records = learningRecordService.lambdaQuery()
                .eq(LearningRecord::getLearnerId, userId)
                .eq(LearningRecord::getResult, "PASSED")
                .list();
        if (records.isEmpty()) {
            return "暂无完成的学习记录，推荐先看看平台上的课程哦~";
        }
        StringBuilder sb = new StringBuilder("已完成 " + records.size() + " 门课程：\n");
        for (LearningRecord r : records) {
            Course course = courseService.getById(r.getCourseId());
            String name = course != null ? course.getCourseName() : "课程ID:" + r.getCourseId();
            sb.append("- ").append(name)
              .append("（成绩:").append(r.getScore()).append("分）\n");
        }
        return sb.toString();
    }

    @Tool(name = "recommend_courses",
          value = "根据用户已完成的课程和积分余额，推荐适合的学习路径")
    public String recommendCourses(@P("用户ID，从系统提示中获取") Long userId) {
        List<Long> learnedIds = learningRecordService.lambdaQuery()
                .eq(LearningRecord::getLearnerId, userId)
                .eq(LearningRecord::getResult, "PASSED")
                .list()
                .stream().map(LearningRecord::getCourseId).collect(Collectors.toList());

        CreditAccount account = accountService.lambdaQuery()
                .eq(CreditAccount::getLearnerId, userId).one();
        int balance = account != null ? account.getAvailableCredits() : 0;

        List<Course> candidates = courseService.lambdaQuery()
                .eq(Course::getStatus, "PUBLISHED")
                .list()
                .stream()
                .filter(c -> !learnedIds.contains(c.getId()))
                .limit(5)
                .collect(Collectors.toList());

        if (candidates.isEmpty()) {
            return "你已经学完了平台上所有课程，太厉害了！🎉";
        }

        StringBuilder sb = new StringBuilder("当前积分余额:" + balance + "\n推荐课程：\n");
        for (Course c : candidates) {
            int point = c.getCreditPoint() != null ? c.getCreditPoint() : 0;
            String affordable = balance >= point ? "✅可兑换" : "⚠️积分不足";
            sb.append("- ").append(c.getCourseName())
              .append(" | 需要").append(point).append("积分 ").append(affordable).append("\n");
        }
        if (learnedIds.isEmpty()) {
            sb.append("\n💡 提示：你还未开始学习，建议从基础课程开始哦~");
        }
        return sb.toString();
    }

    @Tool(name = "get_my_orders",
          value = "查询用户的积分商城订单")
    public String getMyOrders(@P("用户ID，从系统提示中获取") Long userId) {
        long count = orderService.lambdaQuery()
                .eq(CreditOrder::getLearnerId, userId).count();
        List<CreditOrder> recent = orderService.lambdaQuery()
                .eq(CreditOrder::getLearnerId, userId)
                .orderByDesc(CreditOrder::getCreatedAt)
                .last("limit 5")
                .list();
        if (recent.isEmpty()) {
            return "暂无订单记录";
        }
        StringBuilder sb = new StringBuilder("共 " + count + " 笔订单，最近5笔：\n");
        for (CreditOrder o : recent) {
            sb.append("- ").append(o.getProductName())
              .append(" | ").append(o.getCreditAmount()).append("积分")
              .append(" | ").append(o.getOrderStatus()).append("\n");
        }
        return sb.toString();
    }
}
