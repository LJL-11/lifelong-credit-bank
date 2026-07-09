package org.csu.creditbank.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.csu.creditbank.entity.ForumReply;
import org.csu.creditbank.mapper.ForumReplyMapper;
import org.csu.creditbank.service.ForumReplyService;
import org.springframework.stereotype.Service;

@Service
public class ForumReplyServiceImpl extends ServiceImpl<ForumReplyMapper, ForumReply> implements ForumReplyService {
}
