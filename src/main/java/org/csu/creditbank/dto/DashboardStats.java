package org.csu.creditbank.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DashboardStats {

    private long learnerCount;
    private long courseCount;
    private long achievementCount;
    private long creditTransactionCount;
    private long forumPostCount;
    private long jobPostingCount;
}
