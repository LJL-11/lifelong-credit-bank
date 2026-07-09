package org.csu.creditbank.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.csu.creditbank.common.BusinessException;
import org.csu.creditbank.entity.BlockchainCredential;
import org.csu.creditbank.mapper.BlockchainCredentialMapper;
import org.csu.creditbank.service.BlockchainCredentialService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;

@Service
public class BlockchainCredentialServiceImpl extends ServiceImpl<BlockchainCredentialMapper, BlockchainCredential> implements BlockchainCredentialService {

    @Override
    @Transactional
    public BlockchainCredential createCredential(Long learnerId, String credentialType, String businessNo, String sourcePayload, Long institutionId) {
        BlockchainCredential existing = lambdaQuery().eq(BlockchainCredential::getBusinessNo, businessNo).one();
        if (existing != null) return existing;
        BlockchainCredential credential = new BlockchainCredential();
        credential.setLearnerId(learnerId);
        credential.setCredentialType(credentialType);
        credential.setBusinessNo(businessNo);
        credential.setSourcePayload(sourcePayload);
        credential.setHashValue(sha256(sourcePayload));
        credential.setChainStatus("CONFIRMED");
        credential.setVerifiedAt(LocalDateTime.now());
        credential.setInstitutionId(institutionId);
        save(credential);
        return credential;
    }

    @Override
    public boolean verify(String businessNo, String sourcePayload) {
        BlockchainCredential credential = lambdaQuery().eq(BlockchainCredential::getBusinessNo, businessNo).one();
        if (credential == null) throw new BusinessException("存证不存在");
        return credential.getHashValue().equals(sha256(sourcePayload));
    }

    private String sha256(String payload) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest((payload == null ? "" : payload).getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new BusinessException("生成存证哈希失败");
        }
    }
}
