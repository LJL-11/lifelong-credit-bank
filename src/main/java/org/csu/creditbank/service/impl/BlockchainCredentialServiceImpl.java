package org.csu.creditbank.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.csu.creditbank.entity.BlockchainCredential;
import org.csu.creditbank.mapper.BlockchainCredentialMapper;
import org.csu.creditbank.service.BlockchainCredentialService;
import org.springframework.stereotype.Service;

@Service
public class BlockchainCredentialServiceImpl extends ServiceImpl<BlockchainCredentialMapper, BlockchainCredential> implements BlockchainCredentialService {
}
