package org.csu.creditbank.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.csu.creditbank.common.ApiResult;
import org.csu.creditbank.entity.SignInRecord;
import org.csu.creditbank.service.SignInRecordService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/sign-ins")
public class SignInRecordController {

    private final SignInRecordService signInRecordService;

    public SignInRecordController(SignInRecordService signInRecordService) {
        this.signInRecordService = signInRecordService;
    }

    @GetMapping
    public ApiResult<Page<SignInRecord>> page(@RequestParam(defaultValue = "1") long current,
                                              @RequestParam(defaultValue = "10") long size) {
        return ApiResult.ok(signInRecordService.page(Page.of(current, size)));
    }

    @PostMapping
    public ApiResult<SignInRecord> create(@RequestBody SignInRecord record) {
        signInRecordService.save(record);
        return ApiResult.ok(record);
    }
}
