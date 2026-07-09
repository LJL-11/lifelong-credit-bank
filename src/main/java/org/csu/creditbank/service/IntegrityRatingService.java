package org.csu.creditbank.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.csu.creditbank.entity.IntegrityRating;

public interface IntegrityRatingService extends IService<IntegrityRating> {
    IntegrityRating recompute(Long learnerId);
}
