package org.csu.creditbank.controller.admin.credit;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.servlet.http.HttpServletRequest;
import org.csu.creditbank.common.ApiResult;
import org.csu.creditbank.entity.CreditTransaction;
import org.csu.creditbank.service.CreditTransactionService;
import org.csu.creditbank.service.LearnerService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/credit-transactions")
public class CreditTransactionController {

    private final CreditTransactionService creditTransactionService;
    private final LearnerService learnerService;

    public CreditTransactionController(CreditTransactionService creditTransactionService,
                                       LearnerService learnerService) {
        this.creditTransactionService = creditTransactionService;
        this.learnerService = learnerService;
    }

    @GetMapping
    public ApiResult<Page<CreditTransaction>> page(@RequestParam(defaultValue = "1") long current,
                                                   @RequestParam(defaultValue = "10") long size,
                                                   HttpServletRequest request) {
        Long institutionId = (Long) request.getAttribute("institutionId");
        LambdaQueryWrapper<CreditTransaction> wrapper = new LambdaQueryWrapper<>();
        if (institutionId != null && institutionId > 0) {
            List<Long> learnerIds = learnerService.lambdaQuery()
                    .eq(org.csu.creditbank.entity.Learner::getInstitutionId, institutionId)
                    .list()
                    .stream().map(org.csu.creditbank.entity.Learner::getId).collect(Collectors.toList());
            if (learnerIds.isEmpty()) {
                return ApiResult.ok(Page.of(current, size));
            }
            wrapper.in(CreditTransaction::getLearnerId, learnerIds);
        }
        Page<CreditTransaction> page = creditTransactionService.page(Page.of(current, size), wrapper);
        fillLearnerNames(page.getRecords());
        return ApiResult.ok(page);
    }

    private void fillLearnerNames(List<CreditTransaction> transactions) {
        org.csu.creditbank.controller.admin.AdminLearnerNames.fill(transactions, CreditTransaction::getLearnerId, CreditTransaction::setLearnerName, learnerService);
    }
}
