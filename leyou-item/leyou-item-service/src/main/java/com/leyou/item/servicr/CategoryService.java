package com.leyou.item.servicr;

import com.leyou.item.pojo.Category;

import java.util.List;

public interface CategoryService {
    List<Category> queryCategoriesByPid(Long pid);

    List<String> queryCategoryNameById(List<Long> ids);

}
