package com.leyou.item.servicr.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.leyou.common.pojo.PageResult;
import com.leyou.item.mapper.BrandMapper;
import com.leyou.item.pojo.Brand;
import com.leyou.item.servicr.BrandService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

@Service
public class BrandServiceImpl implements BrandService {

    @Autowired
    private BrandMapper brandMapper;

    /**
     * 根据条件分页并排序查询品牌信息
     * @param key
     * @param page
     * @param rows
     * @param sortBy
     * @param desc
     * @return
     */
    @Override
    public PageResult<Brand> queryBrandsByPage(String key, Integer page, Integer rows, String sortBy, Boolean desc) {
        Example example = new Example(Brand.class);
        Example.Criteria criteria = example.createCriteria();
        if (StringUtils.isNotBlank(key)) {
            criteria.andLike("name", "%"+key+"%").orEqualTo("letter",key);
        }
        PageHelper.startPage(page,rows);
        if (StringUtils.isNotBlank(sortBy)) {
            example.setOrderByClause(sortBy + " " + (desc?"desc":"asc"));
        }
        List<Brand> brands = this.brandMapper.selectByExample(example);
        PageInfo<Brand> pageInfo = new PageInfo<>(brands);
        return new PageResult<>(pageInfo.getTotal(),pageInfo.getList());
    }

    /**
     * 新增品牌
     * @param brand
     * @param cids
     */
    @Override
    @Transactional
    public void saveBrand(Brand brand, List<Long> cids) {
        this.brandMapper.insertSelective(brand);
        cids.forEach(cid -> {
            this.brandMapper.insertCategoryAndBrand(cid, brand.getId());
        });
    }

    /**
     * 根据分类来查询品牌
     * @param cid
     * @return
     */
    @Override
    public List<Brand> queryBrandByCid(Long cid) {
        return this.brandMapper.selectBrandsByCid(cid);
    }

    @Override
    public Brand queryBrandById(Long id) {
        return this.brandMapper.selectByPrimaryKey(id);
    }
}
