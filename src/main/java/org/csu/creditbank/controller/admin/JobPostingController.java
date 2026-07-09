package org.csu.creditbank.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.csu.creditbank.common.ApiResult;
import org.csu.creditbank.entity.JobPosting;
import org.csu.creditbank.service.JobPostingService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/jobs")
public class JobPostingController {

    private final JobPostingService jobPostingService;

    public JobPostingController(JobPostingService jobPostingService) {
        this.jobPostingService = jobPostingService;
    }

    @GetMapping
    public ApiResult<Page<JobPosting>> page(@RequestParam(defaultValue = "1") long current,
                                            @RequestParam(defaultValue = "10") long size) {
        return ApiResult.ok(jobPostingService.page(Page.of(current, size)));
    }

    @PostMapping
    public ApiResult<JobPosting> create(@RequestBody JobPosting jobPosting) {
        jobPostingService.save(jobPosting);
        return ApiResult.ok(jobPosting);
    }

    @PutMapping("/{id}")
    public ApiResult<JobPosting> update(@PathVariable Long id, @RequestBody JobPosting jobPosting) {
        jobPosting.setId(id);
        jobPostingService.updateById(jobPosting);
        return ApiResult.ok(jobPostingService.getById(id));
    }

    @DeleteMapping("/{id}")
    public ApiResult<Void> delete(@PathVariable Long id) {
        jobPostingService.removeById(id);
        return ApiResult.ok();
    }
}
