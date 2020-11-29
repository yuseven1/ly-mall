package com.leyou.item.servicr.impl;

import com.leyou.item.mapper.CategoryMapper;
import com.leyou.item.pojo.Category;
import com.leyou.item.servicr.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryServiceImpl implements CategoryService{

    @Autowired
    private CategoryMapper categoryMapper;

    /**
     * 根据父节点来查询子节点
     *
     * @param pid
     * @return
     */
    @Override
    public List<Category> queryCategoriesByPid(Long pid) {
        Category record = new Category();
        record.setParentId(pid);
        return this.categoryMapper.select(record);
    }

    public List<String> queryCategoryNameById(List<Long> ids) {
        List<Category> categories = this.categoryMapper.selectByIdList(ids);
        return categories.stream().map(category -> category.getName()).collect(Collectors.toList());
    }

}
