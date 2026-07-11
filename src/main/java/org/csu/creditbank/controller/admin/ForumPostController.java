package org.csu.creditbank.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.csu.creditbank.common.ApiResult;
import org.csu.creditbank.common.BusinessException;
import org.csu.creditbank.entity.ForumPost;
import org.csu.creditbank.entity.ForumPostLike;
import org.csu.creditbank.entity.ForumReply;
import org.csu.creditbank.entity.Learner;
import org.csu.creditbank.service.ForumPostLikeService;
import org.csu.creditbank.service.ForumPostService;
import org.csu.creditbank.service.ForumReplyService;
import org.csu.creditbank.service.LearnerService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/forum-posts")
public class ForumPostController {

    private final ForumPostService forumPostService;
    private final ForumPostLikeService forumPostLikeService;
    private final ForumReplyService forumReplyService;
    private final LearnerService learnerService;

    public ForumPostController(ForumPostService forumPostService,
                               ForumPostLikeService forumPostLikeService,
                               ForumReplyService forumReplyService,
                               LearnerService learnerService) {
        this.forumPostService = forumPostService;
        this.forumPostLikeService = forumPostLikeService;
        this.forumReplyService = forumReplyService;
        this.learnerService = learnerService;
    }

    @GetMapping
    public ApiResult<Page<ForumPost>> page(@RequestParam(defaultValue = "1") long current,
                                            @RequestParam(defaultValue = "10") long size,
                                            @RequestParam(required = false) String section,
                                            @RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<ForumPost> wrapper = new LambdaQueryWrapper<>();
        if (section != null && !section.isEmpty()) {
            wrapper.eq(ForumPost::getSection, section);
        }
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(ForumPost::getTitle, keyword).or().like(ForumPost::getContent, keyword));
        }
        wrapper.orderByDesc(ForumPost::getCreatedAt);
        Page<ForumPost> page = forumPostService.page(Page.of(current, size), wrapper);
        page.getRecords().forEach(this::fillLearnerName);
        fillLikeCount(page.getRecords());
        return ApiResult.ok(page);
    }

    @GetMapping("/{id}")
    public ApiResult<ForumPost> detail(@PathVariable Long id) {
        ForumPost post = forumPostService.getById(id);
        if (post == null) throw new BusinessException("帖子不存在");
        fillLearnerName(post);
        fillLikeCount(List.of(post));
        return ApiResult.ok(post);
    }

    @PutMapping("/{id}/status")
    public ApiResult<ForumPost> updateStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String status = body.get("status");
        if (status == null || (!"VISIBLE".equals(status) && !"HIDDEN".equals(status))) {
            throw new BusinessException("状态值无效，允许: VISIBLE / HIDDEN");
        }
        ForumPost post = forumPostService.getById(id);
        if (post == null) throw new BusinessException("帖子不存在");
        post.setStatus(status);
        forumPostService.updateById(post);
        return ApiResult.ok(post);
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ApiResult<Void> delete(@PathVariable Long id) {
        ForumPost post = forumPostService.getById(id);
        if (post == null) throw new BusinessException("帖子不存在");
        forumReplyService.lambdaUpdate().eq(ForumReply::getPostId, id).remove();
        forumPostLikeService.lambdaUpdate().eq(ForumPostLike::getPostId, id).remove();
        forumPostService.removeById(id);
        return ApiResult.ok();
    }

    private void fillLearnerName(ForumPost post) {
        if (post.getLearnerId() != null) {
            Learner learner = learnerService.getById(post.getLearnerId());
            if (learner != null) post.setLearnerName(learner.getRealName());
        }
    }

    private void fillLikeCount(List<ForumPost> posts) {
        if (posts.isEmpty()) return;
        List<Long> postIds = posts.stream().map(ForumPost::getId).toList();
        Map<Long, Integer> likeCounts = forumPostLikeService.getLikeCounts(postIds);
        for (ForumPost post : posts) {
            post.setLikeCount(likeCounts.getOrDefault(post.getId(), 0));
        }
    }
}
