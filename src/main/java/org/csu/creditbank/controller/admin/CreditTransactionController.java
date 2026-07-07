package org.csu.creditbank.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.csu.creditbank.common.ApiResult;
import org.csu.creditbank.entity.CreditTransaction;
import org.csu.creditbank.service.CreditTransactionService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/credit-transactions")
public class CreditTransactionController {

    private final CreditTransactionService creditTransactionService;

    public CreditTransactionController(CreditTransactionService creditTransactionService) {
        this.creditTransactionService = creditTransactionService;
    }

    @GetMapping
    public ApiResult<Page<CreditTransaction>> page(@RequestParam(defaultValue = "1") long current,
                                                   @RequestParam(defaultValue = "10") long size) {
        return ApiResult.ok(creditTransactionService.page(Page.of(current, size)));
    }
}
