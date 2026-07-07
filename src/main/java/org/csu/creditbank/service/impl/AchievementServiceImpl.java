package org.csu.creditbank.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.csu.creditbank.entity.Achievement;
import org.csu.creditbank.mapper.AchievementMapper;
import org.csu.creditbank.service.AchievementService;
import org.springframework.stereotype.Service;

@Service
public class AchievementServiceImpl extends ServiceImpl<AchievementMapper, Achievement> implements AchievementService {
}
