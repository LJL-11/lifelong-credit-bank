package org.csu.creditbank.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.csu.creditbank.common.ApiResult;
import org.csu.creditbank.entity.LearningRecord;
import org.csu.creditbank.service.LearningRecordService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/learning-records")
public class LearningRecordController {

    private final LearningRecordService learningRecordService;

    public LearningRecordController(LearningRecordService learningRecordService) {
        this.learningRecordService = learningRecordService;
    }

    @GetMapping
    public ApiResult<Page<LearningRecord>> page(@RequestParam(defaultValue = "1") long current,
                                                @RequestParam(defaultValue = "10") long size) {
        return ApiResult.ok(learningRecordService.page(Page.of(current, size)));
    }

    @PostMapping
    public ApiResult<LearningRecord> create(@RequestBody LearningRecord record) {
        learningRecordService.save(record);
        return ApiResult.ok(record);
    }

    @PutMapping("/{id}")
    public ApiResult<LearningRecord> update(@PathVariable Long id, @RequestBody LearningRecord record) {
        record.setId(id);
        learningRecordService.updateById(record);
        return ApiResult.ok(record);
    }
}
