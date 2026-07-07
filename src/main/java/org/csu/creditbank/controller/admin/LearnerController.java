package org.csu.creditbank.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.validation.Valid;
import org.csu.creditbank.common.ApiResult;
import org.csu.creditbank.entity.Learner;
import org.csu.creditbank.service.LearnerService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/learners")
public class LearnerController {

    private final LearnerService learnerService;

    public LearnerController(LearnerService learnerService) {
        this.learnerService = learnerService;
    }

    @GetMapping
    public ApiResult<Page<Learner>> page(@RequestParam(defaultValue = "1") long current,
                                         @RequestParam(defaultValue = "10") long size) {
        return ApiResult.ok(learnerService.page(Page.of(current, size)));
    }

    @GetMapping("/{id}")
    public ApiResult<Learner> detail(@PathVariable Long id) {
        return ApiResult.ok(learnerService.getById(id));
    }

    @PostMapping
    public ApiResult<Learner> create(@Valid @RequestBody Learner learner) {
        learnerService.save(learner);
        return ApiResult.ok(learner);
    }

    @PutMapping("/{id}")
    public ApiResult<Learner> update(@PathVariable Long id, @RequestBody Learner learner) {
        learner.setId(id);
        learnerService.updateById(learner);
        return ApiResult.ok(learner);
    }

    @DeleteMapping("/{id}")
    public ApiResult<Void> delete(@PathVariable Long id) {
        learnerService.removeById(id);
        return ApiResult.ok();
    }
}
