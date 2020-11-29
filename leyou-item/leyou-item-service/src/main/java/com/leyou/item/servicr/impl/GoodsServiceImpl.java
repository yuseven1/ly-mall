package com.leyou.item.servicr.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.leyou.common.pojo.PageResult;
import com.leyou.item.bo.SpuBo;
import com.leyou.item.mapper.*;
import com.leyou.item.pojo.*;
import com.leyou.item.servicr.CategoryService;
import com.leyou.item.servicr.GoodsService;
import org.apache.commons.lang.StringUtils;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GoodsServiceImpl implements GoodsService{
    @Autowired
    private SpuMapper spuMapper;
    @Autowired
    private SpuDetailMapper spuDetailMapper;
    @Autowired
    private BrandMapper brandMapper;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private SkuMapper skuMapper;
    @Autowired
    private StockMapper stockMapper;
    @Autowired
    private AmqpTemplate amqpTemplate;

    /**
     * 根据条件分页查询 spu
     * @param key
     * @param saleable
     * @param page
     * @param rows
     * @return
     */
    @Override
    public PageResult<SpuBo> querySpuByPage(String key, Boolean saleable, Integer page, Integer rows) {
        Example example = new Example(Spu.class);
        Example.Criteria criteria = example.createCriteria();
        // 添加查询条件
        if (StringUtils.isNotBlank(key)) {
            criteria.andLike("title", "%" + key + "%");
        }
        // 添加上下架的过滤条件
        if (saleable != null) {
            criteria.andEqualTo("saleable", saleable);
        }
        // 添加分页
        PageHelper.startPage(page, rows);
        // 执行查询，获取spu集合
        List<Spu> spus = this.spuMapper.selectByExample(example);
        PageInfo<Spu> spuPageInfo = new PageInfo<>(spus);
        // spu集合转化成spuBo集合
        List<SpuBo> spuBos = spus.stream().map(spu -> {
            SpuBo spuBo = new SpuBo();
            BeanUtils.copyProperties(spu,spuBo);
            // 查询品牌名 bname
            Brand brand = this.brandMapper.selectByPrimaryKey(spu.getBrandId());
            spuBo.setBname(brand.getName());
            // 查询分类名称
            List<String> names = this.categoryService.queryCategoryNameById(Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3()));
            spuBo.setCname(StringUtils.join(names," - "));
            return spuBo;
        }).collect(Collectors.toList());
        // 返回分页结果集
        return new PageResult<SpuBo>(spuPageInfo.getTotal(),spuBos);
    }

    @Transactional
    @Override
    public void saveGoods(SpuBo spuBo) {
        // 新增SPU
        spuBo.setId(null);
        spuBo.setSaleable(true);
        spuBo.setValid(true);
        spuBo.setCreateTime(new Date());
        spuBo.setLastUpdateTime(new Date());
        this.spuMapper.insertSelective(spuBo);
        // 新增spu detail
        SpuDetail spuDetail = spuBo.getSpuDetail();
        spuDetail.setSpuId(spuBo.getId());
        this.spuDetailMapper.insertSelective(spuDetail);
        saveSkuAndStock(spuBo);

        sendMsg("insert", spuBo.getId());

    }

    private void sendMsg(String type, Long id) {
        try {
            this.amqpTemplate.convertAndSend("item." + type, id);
        } catch (AmqpException e) {
            e.printStackTrace();
        }
    }

    /**
     *
     * @param spuBo
     */
    private void saveSkuAndStock(SpuBo spuBo) {
        // 新增sku / stock
        spuBo.getSkus().forEach(sku -> {
            // 新增sku
            sku.setId(null);
            sku.setSpuId(spuBo.getId());
            sku.setCreateTime(new Date());
            sku.setLastUpdateTime(new Date());
            this.skuMapper.insertSelective(sku);
            // 新增stock
            Stock stock = new Stock();
            stock.setSkuId(sku.getId());
            stock.setStock(sku.getStock());
            this.stockMapper.insert(stock);
        });
    }

    /**
     * spuid 查询 spu_detail
     * @param spuId
     * @return
     */
    @Override
    public SpuDetail querySpuDetailBySpuId(Long spuId) {
        return this.spuDetailMapper.selectByPrimaryKey(spuId);
    }

    /**
     * 根据spu id 查询 sku
     * @param spuId
     * @return
     */
    @Override
    public List<Sku> querySkusBySpuId(Long spuId) {
        Sku record = new Sku();
        record.setSpuId(spuId);
        List<Sku> skus = this.skuMapper.select(record);
        skus.forEach(sku -> {
            Stock stock = this.stockMapper.selectByPrimaryKey(sku.getId());
            sku.setStock(stock.getStock());
        });
        return skus;
    }

    /**
     * 更新商品
     * @param spuBo
     */
    @Transactional
    @Override
    public void updateGoods(SpuBo spuBo) {
        Sku record = new Sku();
        record.setSpuId(spuBo.getId());
        // 根据spuId查询要删除的sku
        List<Sku> skus = this.skuMapper.select(record);
        skus.forEach(sku -> {
            // 删除库存
            this.stockMapper.deleteByPrimaryKey(sku.getId());
        });
        // 删除sku
        Sku sku = new Sku();
        sku.setSpuId(spuBo.getId());
        this.skuMapper.delete(sku);
        // 新增SKU和Stock
        saveSkuAndStock(spuBo);
        // 更新spu和spu detail
        spuBo.setCreateTime(null);
        spuBo.setLastUpdateTime(new Date());
        spuBo.setSaleable(null);
        spuBo.setValid(null);
        this.spuMapper.updateByPrimaryKeySelective(spuBo);
        this.spuDetailMapper.updateByPrimaryKeySelective(spuBo.getSpuDetail());

        sendMsg("update", spuBo.getId());
    }

    @Override
    public Spu querySpuById(Long id) {
        return this.spuMapper.selectByPrimaryKey(id);
    }
}
