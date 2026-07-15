package org.csu.creditbank.dto;

import java.util.List;
import java.util.Map;

public class CourseRanking {
    /** Top 10 课程完成人数 [{courseName:"Java高级编程", learnerCount:45, institutionName:"..."}, ...] */
    private List<Map<String, Object>> topCourses;

    public CourseRanking() {}
    public CourseRanking(List<Map<String, Object>> topCourses) { this.topCourses = topCourses; }
    public List<Map<String, Object>> getTopCourses() { return topCourses; }
    public void setTopCourses(List<Map<String, Object>> topCourses) { this.topCourses = topCourses; }
}
