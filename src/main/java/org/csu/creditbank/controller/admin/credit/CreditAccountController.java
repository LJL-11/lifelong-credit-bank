package org.csu.creditbank.controller.admin.credit;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.csu.creditbank.common.ApiResult;
import org.csu.creditbank.dto.CreditChangeRequest;
import org.csu.creditbank.entity.CreditAccount;
import org.csu.creditbank.service.CreditAccountService;
import org.csu.creditbank.service.LearnerService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/credit-accounts")
public class CreditAccountController {

    private final CreditAccountService creditAccountService;
    private final LearnerService learnerService;

    public CreditAccountController(CreditAccountService creditAccountService,
                                   LearnerService learnerService) {
        this.creditAccountService = creditAccountService;
        this.learnerService = learnerService;
    }

    @GetMapping
    public ApiResult<Page<CreditAccount>> page(@RequestParam(defaultValue = "1") long current,
                                                @RequestParam(defaultValue = "10") long size,
                                                HttpServletRequest request) {
        Long institutionId = (Long) request.getAttribute("institutionId");
        LambdaQueryWrapper<CreditAccount> wrapper = new LambdaQueryWrapper<>();
        if (institutionId != null && institutionId > 0) {
            List<Long> learnerIds = learnerService.lambdaQuery()
                    .eq(org.csu.creditbank.entity.Learner::getInstitutionId, institutionId)
                    .list()
                    .stream().map(org.csu.creditbank.entity.Learner::getId).collect(Collectors.toList());
            if (learnerIds.isEmpty()) {
                return ApiResult.ok(Page.of(current, size));
            }
            wrapper.in(CreditAccount::getLearnerId, learnerIds);
        }
        return ApiResult.ok(creditAccountService.page(Page.of(current, size), wrapper));
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

    /** 管理员冻结积分 */
    @PostMapping("/freeze")
    public ApiResult<CreditAccount> freeze(@RequestBody Map<String, Object> body) {
        Long learnerId = Long.valueOf(body.get("learnerId").toString());
        Integer amount = Integer.valueOf(body.get("amount").toString());
        return ApiResult.ok(creditAccountService.freezeCredits(learnerId, amount));
    }

    /** 管理员解冻积分 */
    @PostMapping("/unfreeze")
    public ApiResult<CreditAccount> unfreeze(@RequestBody Map<String, Object> body) {
        Long learnerId = Long.valueOf(body.get("learnerId").toString());
        Integer amount = Integer.valueOf(body.get("amount").toString());
        return ApiResult.ok(creditAccountService.unfreezeCredits(learnerId, amount));
    }
}
