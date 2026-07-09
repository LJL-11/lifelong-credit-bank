package org.csu.creditbank.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.csu.creditbank.common.ApiResult;
import org.csu.creditbank.common.BusinessException;
import org.csu.creditbank.entity.ForumPost;
import org.csu.creditbank.entity.ForumReply;
import org.csu.creditbank.entity.Learner;
import org.csu.creditbank.service.ForumPostLikeService;
import org.csu.creditbank.service.ForumReplyService;
import org.csu.creditbank.service.ForumPostService;
import org.csu.creditbank.service.LearnerService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/student/forum")
public class StudentForumController {

    private final ForumPostService forumPostService;
    private final ForumPostLikeService forumPostLikeService;
    private final ForumReplyService forumReplyService;
    private final LearnerService learnerService;

    public StudentForumController(ForumPostService forumPostService,
                                  ForumPostLikeService forumPostLikeService,
                                  ForumReplyService forumReplyService,
                                  LearnerService learnerService) {
        this.forumPostService = forumPostService;
        this.forumPostLikeService = forumPostLikeService;
        this.forumReplyService = forumReplyService;
        this.learnerService = learnerService;
    }

    /** 帖子列表（只显示可见帖子） */
    @GetMapping
    public ApiResult<Page<ForumPost>> list(@RequestParam(defaultValue = "1") long current,
                                            @RequestParam(defaultValue = "10") long size,
                                            @RequestParam(required = false) String section,
                                            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
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
        fillLikeInfo(page.getRecords(), userId);
        return ApiResult.ok(page);
    }

    /** 帖子详情 */
    @GetMapping("/{id}")
    public ApiResult<ForumPost> detail(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        ForumPost post = forumPostService.getById(id);
        if (post == null) throw new BusinessException("帖子不存在");
        Learner l = learnerService.getById(post.getLearnerId());
        if (l != null) post.setLearnerName(l.getRealName());
        fillLikeInfo(List.of(post), userId);
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
        fillLikeInfo(page.getRecords(), userId);
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



    /** 帖子回复列表 */
    @GetMapping("/{id}/replies")
    public ApiResult<Page<ForumReply>> replies(@PathVariable Long id,
                                                @RequestParam(defaultValue = "1") long current,
                                                @RequestParam(defaultValue = "20") long size) {
        Page<ForumReply> page = forumReplyService.lambdaQuery()
                .eq(ForumReply::getPostId, id)
                .eq(ForumReply::getStatus, "VISIBLE")
                .orderByAsc(ForumReply::getCreatedAt)
                .page(Page.of(current, size));
        page.getRecords().forEach(r -> {
            Learner l = learnerService.getById(r.getLearnerId());
            if (l != null) r.setLearnerName(l.getRealName());
        });
        return ApiResult.ok(page);
    }

    /** 回复帖子 */
    @PostMapping("/{id}/replies")
    public ApiResult<ForumReply> reply(@PathVariable Long id, @RequestBody ForumReply reply, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        ForumPost post = forumPostService.getById(id);
        if (post == null || !"VISIBLE".equals(post.getStatus())) throw new BusinessException("帖子不存在或不可回复");
        reply.setPostId(id);
        reply.setLearnerId(userId);
        reply.setStatus("VISIBLE");
        reply.setInstitutionId((Long) request.getAttribute("institutionId"));
        forumReplyService.save(reply);
        post.setReplyCount((post.getReplyCount() == null ? 0 : post.getReplyCount()) + 1);
        forumPostService.updateById(post);
        return ApiResult.ok(reply);
    }

    /** 点赞（Redisson 分布式锁防并发） */
    @PostMapping("/{id}/like")
    public ApiResult<Void> like(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        ForumPost post = forumPostService.getById(id);
        if (post == null) throw new BusinessException("帖子不存在");
        forumPostLikeService.like(id, userId);
        return ApiResult.ok();
    }

    /** 取消点赞 */
    @DeleteMapping("/{id}/like")
    public ApiResult<Void> unlike(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        forumPostLikeService.unlike(id, userId);
        return ApiResult.ok();
    }

    /** 板块列表 */
    @GetMapping("/sections")
    public ApiResult<String[]> sections() {
        return ApiResult.ok(new String[]{"学习交流", "积分商城", "课程讨论", "技术问答", "活动分享"});
    }

    /** 批量填充点赞数和当前用户是否已点赞 */
    private void fillLikeInfo(List<ForumPost> posts, Long userId) {
        if (posts.isEmpty()) return;
        List<Long> postIds = posts.stream().map(ForumPost::getId).toList();

        Map<Long, Integer> likeCounts = forumPostLikeService.getLikeCounts(postIds);
        Set<Long> likedPostIds = forumPostLikeService.getLikedPostIds(postIds, userId);

        for (ForumPost post : posts) {
            post.setLikeCount(likeCounts.getOrDefault(post.getId(), 0));
            post.setLiked(likedPostIds.contains(post.getId()));
        }
    }
}
