package org.csu.creditbank.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.csu.creditbank.entity.Course;
import org.csu.creditbank.entity.CreditProduct;
import org.csu.creditbank.mapper.CourseMapper;
import org.csu.creditbank.service.CourseService;
import org.csu.creditbank.service.CreditProductService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CourseServiceImpl extends ServiceImpl<CourseMapper, Course> implements CourseService {

    private final CreditProductService productService;

    public CourseServiceImpl(CreditProductService productService) {
        this.productService = productService;
    }

    @Override
    @Transactional
    public boolean save(Course course) {
        boolean ok = super.save(course);
        syncProduct(course);
        return ok;
    }

    @Override
    @Transactional
    public boolean updateById(Course course) {
        boolean ok = super.updateById(course);
        Course fresh = getById(course.getId());
        if (fresh != null) syncProduct(fresh);
        return ok;
    }

    @Override
    @Transactional
    public boolean removeById(java.io.Serializable id) {
        Course course = getById(id);
        if (course != null) {
            String productCode = "COURSE-" + course.getCourseCode();
            CreditProduct product = productService.lambdaQuery()
                    .eq(CreditProduct::getProductCode, productCode).one();
            if (product != null) {
                productService.removeById(product.getId());
            }
        }
        return super.removeById(id);
    }

    private void syncProduct(Course course) {
        String productCode = "COURSE-" + course.getCourseCode();
        CreditProduct product = productService.lambdaQuery()
                .eq(CreditProduct::getProductCode, productCode).one();

        if (product == null) {
            return;
        }

        product.setProductName(course.getCourseName());
        product.setProductType("COURSE");
        product.setCourseId(course.getId());
        product.setCreditPrice(course.getCreditPoint() != null ? course.getCreditPoint() : 0);
        product.setDescription(course.getProvider() + " / " + course.getCategory());
        product.setStock(-1);
        product.setIsActive("PUBLISHED".equals(course.getStatus()) ? 1 : 0);

        productService.updateById(product);
    }
}
