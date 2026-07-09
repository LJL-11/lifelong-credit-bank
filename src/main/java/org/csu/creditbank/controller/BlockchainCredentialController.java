package org.csu.creditbank.controller;

import org.csu.creditbank.common.ApiResult;
import org.csu.creditbank.entity.BlockchainCredential;
import org.csu.creditbank.service.BlockchainCredentialService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/blockchain")
public class BlockchainCredentialController {

    private final BlockchainCredentialService credentialService;

    public BlockchainCredentialController(BlockchainCredentialService credentialService) {
        this.credentialService = credentialService;
    }

    @GetMapping("/credentials/{businessNo}")
    public ApiResult<BlockchainCredential> credential(@PathVariable String businessNo) {
        return ApiResult.ok(credentialService.lambdaQuery()
                .eq(BlockchainCredential::getBusinessNo, businessNo)
                .one());
    }

    @PostMapping("/verify/{businessNo}")
    public ApiResult<Map<String, Object>> verify(@PathVariable String businessNo, @RequestBody Map<String, String> body) {
        boolean valid = credentialService.verify(businessNo, body.get("payload"));
        return ApiResult.ok(Map.of("businessNo", businessNo, "valid", valid));
    }
}
