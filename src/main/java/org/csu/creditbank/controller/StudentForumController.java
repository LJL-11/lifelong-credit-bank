package org.csu.creditbank.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.csu.creditbank.common.ApiResult;
import org.csu.creditbank.common.BusinessException;
import org.csu.creditbank.entity.ForumPost;
import org.csu.creditbank.entity.Learner;
import org.csu.creditbank.service.ForumPostService;
import org.csu.creditbank.service.LearnerService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/student/forum")
public class StudentForumController {

    private final ForumPostService forumPostService;
    private final LearnerService learnerService;

    public StudentForumController(ForumPostService forumPostService, LearnerService learnerService) {
        this.forumPostService = forumPostService;
        this.learnerService = learnerService;
    }

    /** 帖子列表（只显示可见帖子） */
    @GetMapping
    public ApiResult<Page<ForumPost>> list(@RequestParam(defaultValue = "1") long current,
                                            @RequestParam(defaultValue = "10") long size,
                                            @RequestParam(required = false) String section) {
        LambdaQueryWrapper<ForumPost> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ForumPost::getStatus, "VISIBLE");
        if (section != null && !section.isEmpty()) {
            wrapper.eq(ForumPost::getSection, section);
        }
        wrapper.orderByDesc(ForumPost::getCreatedAt);
        Page<ForumPost> page = forumPostService.page(Page.of(current, size), wrapper);
        page.getRecords().forEach(p -> {
            Learner l = learnerService.getById(p.getLearnerId());
            if (l != null) p.setLearnerName(l.getRealName());
        });
        return ApiResult.ok(page);
    }

    /** 帖子详情 */
    @GetMapping("/{id}")
    public ApiResult<ForumPost> detail(@PathVariable Long id) {
        ForumPost post = forumPostService.getById(id);
        if (post == null) throw new BusinessException("帖子不存在");
        Learner l = learnerService.getById(post.getLearnerId());
        if (l != null) post.setLearnerName(l.getRealName());
        return ApiResult.ok(post);
    }

    /** 我的帖子 */
    @GetMapping("/mine")
    public ApiResult<Page<ForumPost>> myPosts(@RequestParam(defaultValue = "1") long current,
                                               @RequestParam(defaultValue = "10") long size,
                                               HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Page<ForumPost> page = forumPostService.lambdaQuery()
                .eq(ForumPost::getLearnerId, userId)
                .orderByDesc(ForumPost::getCreatedAt)
                .page(Page.of(current, size));
        page.getRecords().forEach(p -> {
            Learner l = learnerService.getById(userId);
            if (l != null) p.setLearnerName(l.getRealName());
        });
        return ApiResult.ok(page);
    }

    /** 发帖 */
    @PostMapping
    public ApiResult<ForumPost> create(@Valid @RequestBody ForumPost post,
                                        HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Learner learner = learnerService.getById(userId);
        post.setLearnerId(userId);
        post.setLearnerName(learner != null ? learner.getRealName() : "");
        post.setReplyCount(0);
        post.setStatus("VISIBLE");
        if (post.getSection() == null || post.getSection().isEmpty()) {
            post.setSection("学习交流");
        }
        forumPostService.save(post);
        return ApiResult.ok(post);
    }

    /** 板块列表 */
    @GetMapping("/sections")
    public ApiResult<String[]> sections() {
        return ApiResult.ok(new String[]{"学习交流", "积分商城", "课程讨论", "技术问答", "活动分享"});
    }
}
