package org.csu.creditbank.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.csu.creditbank.common.ApiResult;
import org.csu.creditbank.entity.BlockchainCredential;
import org.csu.creditbank.service.BlockchainCredentialService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/blockchain-credentials")
public class BlockchainCredentialController {

    private final BlockchainCredentialService blockchainCredentialService;

    public BlockchainCredentialController(BlockchainCredentialService blockchainCredentialService) {
        this.blockchainCredentialService = blockchainCredentialService;
    }

    @GetMapping
    public ApiResult<Page<BlockchainCredential>> page(@RequestParam(defaultValue = "1") long current,
                                                      @RequestParam(defaultValue = "10") long size) {
        return ApiResult.ok(blockchainCredentialService.page(Page.of(current, size)));
    }

    @PostMapping
    public ApiResult<BlockchainCredential> create(@RequestBody BlockchainCredential credential) {
        blockchainCredentialService.save(credential);
        return ApiResult.ok(credential);
    }
}
