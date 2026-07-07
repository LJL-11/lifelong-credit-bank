package org.csu.creditbank.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.csu.creditbank.common.ApiResult;
import org.csu.creditbank.entity.ForumPost;
import org.csu.creditbank.service.ForumPostService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/forum-posts")
public class ForumPostController {

    private final ForumPostService forumPostService;

    public ForumPostController(ForumPostService forumPostService) {
        this.forumPostService = forumPostService;
    }

    @GetMapping
    public ApiResult<Page<ForumPost>> page(@RequestParam(defaultValue = "1") long current,
                                           @RequestParam(defaultValue = "10") long size) {
        return ApiResult.ok(forumPostService.page(Page.of(current, size)));
    }

    @PostMapping
    public ApiResult<ForumPost> create(@RequestBody ForumPost post) {
        forumPostService.save(post);
        return ApiResult.ok(post);
    }
}
