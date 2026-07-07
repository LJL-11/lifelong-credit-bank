package org.csu.creditbank.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.validation.Valid;
import org.csu.creditbank.common.ApiResult;
import org.csu.creditbank.dto.CreditChangeRequest;
import org.csu.creditbank.entity.CreditAccount;
import org.csu.creditbank.service.CreditAccountService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/credit-accounts")
public class CreditAccountController {

    private final CreditAccountService creditAccountService;

    public CreditAccountController(CreditAccountService creditAccountService) {
        this.creditAccountService = creditAccountService;
    }

    @GetMapping
    public ApiResult<Page<CreditAccount>> page(@RequestParam(defaultValue = "1") long current,
                                               @RequestParam(defaultValue = "10") long size) {
        return ApiResult.ok(creditAccountService.page(Page.of(current, size)));
    }

    @PostMapping("/open/{learnerId}")
    public ApiResult<CreditAccount> open(@PathVariable Long learnerId) {
        return ApiResult.ok(creditAccountService.openAccount(learnerId));
    }

    @PostMapping("/increase")
    public ApiResult<CreditAccount> increase(@Valid @RequestBody CreditChangeRequest request) {
        return ApiResult.ok(creditAccountService.increaseCredits(request));
    }

    @PostMapping("/consume")
    public ApiResult<CreditAccount> consume(@Valid @RequestBody CreditChangeRequest request) {
        return ApiResult.ok(creditAccountService.consumeCredits(request));
    }
}
