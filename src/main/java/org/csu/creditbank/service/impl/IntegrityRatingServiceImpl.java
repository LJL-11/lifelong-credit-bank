package org.csu.creditbank.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.csu.creditbank.entity.*;
import org.csu.creditbank.mapper.IntegrityRatingMapper;
import org.csu.creditbank.service.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class IntegrityRatingServiceImpl extends ServiceImpl<IntegrityRatingMapper, IntegrityRating> implements IntegrityRatingService {

    private final LearningRecordService learningRecordService;
    private final ForumPostService forumPostService;
    private final SignInRecordService signInRecordService;
    private final AchievementService achievementService;
    private final JobApplicationService jobApplicationService;

    public IntegrityRatingServiceImpl(LearningRecordService learningRecordService,
                                      ForumPostService forumPostService,
                                      SignInRecordService signInRecordService,
                                      AchievementService achievementService,
                                      JobApplicationService jobApplicationService) {
        this.learningRecordService = learningRecordService;
        this.forumPostService = forumPostService;
        this.signInRecordService = signInRecordService;
        this.achievementService = achievementService;
        this.jobApplicationService = jobApplicationService;
    }

    @Override
    @Transactional
    public IntegrityRating recompute(Long learnerId) {
        long completed = learningRecordService.lambdaQuery()
                .eq(LearningRecord::getLearnerId, learnerId)
                .eq(LearningRecord::getResult, "PASSED")
                .count();
        long hiddenPosts = forumPostService.lambdaQuery()
                .eq(ForumPost::getLearnerId, learnerId)
                .eq(ForumPost::getStatus, "HIDDEN")
                .count();
        long signIns = signInRecordService.lambdaQuery()
                .eq(SignInRecord::getLearnerId, learnerId)
                .count();
        long approvedAchievements = achievementService.lambdaQuery()
                .eq(Achievement::getLearnerId, learnerId)
                .eq(Achievement::getAuditStatus, "APPROVED")
                .count();
        long jobApplies = jobApplicationService.lambdaQuery()
                .eq(JobApplication::getLearnerId, learnerId)
                .count();

        int learningScore = (int) Math.min(25, completed * 5);
        int signinScore = (int) Math.min(20, signIns * 2);
        int achievementScore = (int) Math.min(25, approvedAchievements * 8);
        int jobScore = (int) Math.min(10, jobApplies * 2);
        int forumScore = Math.max(0, 20 - (int) hiddenPosts * 5);
        int score = Math.min(100, learningScore + signinScore + achievementScore + jobScore + forumScore);

        IntegrityRating rating = lambdaQuery().eq(IntegrityRating::getLearnerId, learnerId).one();
        if (rating == null) {
            rating = new IntegrityRating();
            rating.setLearnerId(learnerId);
        }
        rating.setLearningScore(learningScore);
        rating.setSigninScore(signinScore);
        rating.setAchievementScore(achievementScore);
        rating.setJobScore(jobScore);
        rating.setForumScore(forumScore);
        rating.setScore(score);
        rating.setLevelName(score >= 90 ? "A" : score >= 75 ? "B" : score >= 60 ? "C" : "D");
        rating.setRemark("系统按学习、签到、成果、论坛和求职行为自动评定");
        saveOrUpdate(rating);
        return rating;
    }
}
