package org.csu.creditbank.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.csu.creditbank.common.ApiResult;
import org.csu.creditbank.entity.Course;
import org.csu.creditbank.service.CourseService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/courses")
public class CourseController {

    private final CourseService courseService;

    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    @GetMapping
    public ApiResult<Page<Course>> page(@RequestParam(defaultValue = "1") long current,
                                        @RequestParam(defaultValue = "10") long size) {
        return ApiResult.ok(courseService.page(Page.of(current, size)));
    }

    @PostMapping
    public ApiResult<Course> create(@RequestBody Course course) {
        courseService.save(course);
        return ApiResult.ok(course);
    }

    @PutMapping("/{id}")
    public ApiResult<Course> update(@PathVariable Long id, @RequestBody Course course) {
        course.setId(id);
        courseService.updateById(course);
        return ApiResult.ok(course);
    }

    @DeleteMapping("/{id}")
    public ApiResult<Void> delete(@PathVariable Long id) {
        courseService.removeById(id);
        return ApiResult.ok();
    }
}
