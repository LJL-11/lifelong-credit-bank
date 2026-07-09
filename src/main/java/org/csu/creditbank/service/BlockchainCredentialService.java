package org.csu.creditbank.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.csu.creditbank.entity.BlockchainCredential;

public interface BlockchainCredentialService extends IService<BlockchainCredential> {
    BlockchainCredential createCredential(Long learnerId, String credentialType, String businessNo, String sourcePayload, Long institutionId);
    boolean verify(String businessNo, String sourcePayload);
}
