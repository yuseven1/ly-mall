package com.leyou.search.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.leyou.common.pojo.PageResult;
import com.leyou.item.pojo.*;
import com.leyou.search.client.BrandClient;
import com.leyou.search.client.CategoryClient;
import com.leyou.search.client.GoodsClient;
import com.leyou.search.client.SpecificationClient;
import com.leyou.search.pojo.Goods;
import com.leyou.search.pojo.SearchRequest;
import com.leyou.search.repository.GoodsRepository;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.query.FetchSourceFilter;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

@Service
public class SearchService {
    @Autowired
    private CategoryClient categoryClient;
    @Autowired
    private BrandClient brandClient;
    @Autowired
    private GoodsClient goodsClient;
    @Autowired
    private SpecificationClient specificationClient;
    @Autowired
    private GoodsRepository goodsRepository;

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public Goods buildGoods (Spu spu) throws IOException {
        Goods goods = new Goods();
        // 根据分类id，查询分类名称
        List<String> names = this.categoryClient.queryNamesByIds(Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3()));
        // 根据品牌id，查询品牌数据
        Brand brand = this.brandClient.queryBrandById(spu.getBrandId());
        // 根据spu id 查询 sku信息
        List<Sku> skus = this.goodsClient.querySkusBySpuId(spu.getId());
        List<Long> prices = new ArrayList<Long>();
        // 手机sku必要信息
        List<Map<String,Object>> skuMapList = new ArrayList<>();
        skus.forEach(sku -> {
            prices.add(sku.getPrice());
            Map<String,Object> skuMap = new HashMap<>();
            skuMap.put("id",sku.getId());
            skuMap.put("title",sku.getTitle());
            skuMap.put("price",sku.getPrice());
            // 获取sku中的图片，是数据库中的图片，可能多张，以逗号分割，取第一张
            skuMap.put("image",StringUtils.isBlank(sku.getImages()) ? "" : StringUtils.split(sku.getImages(),",")[0]);
            skuMapList.add(skuMap);
        });
        // 所有的搜索规格参数
        List<SpecParam> specParams = this.specificationClient.queryParams(null, spu.getCid3(), null, true);
        SpuDetail spuDetail = this.goodsClient.querySpuDetailBySpuId(spu.getId());
        Map<String, Object> genericSpecMap = MAPPER.readValue(spuDetail.getGenericSpec(), new TypeReference<Map<String, Object>>() {});
        Map<String, List<Object>> specialSpecMap = MAPPER.readValue(spuDetail.getSpecialSpec(), new TypeReference<Map<String, List<Object>>>() {});
        Map<String, Object> spec = new HashMap<>();
        specParams.forEach(param ->{
            if (param.getGeneric()) {
                String value = genericSpecMap.get(param.getId().toString()).toString();
                if (param.getNumeric()) {
                    value = chooseSegment(value, param);
                }
                spec.put(param.getName(), value);
            } else {
                spec.put(param.getName(), specialSpecMap.get(param.getId()));
            }
        });
        goods.setId(spu.getId());
        goods.setCid1(spu.getCid1());
        goods.setCid2(spu.getCid2());
        goods.setCid3(spu.getCid3());
        goods.setBrandId(spu.getBrandId());
        goods.setCreateTime(spu.getCreateTime());
        goods.setSubTitle(spu.getSubTitle());
        // 拼接all字段，需要分类和品牌名称
        goods.setAll(spu.getTitle() + " " + StringUtils.join(names," ") + " " + brand.getName());
        // 获取spu下的所有sku的价格
        goods.setPrice(prices);
        // 获取spu下的所有sku，并转化成json字符串
        goods.setSkus(MAPPER.writeValueAsString(skuMapList));
        // 获取所有查询的规格参数
        goods.setSpecs(spec);
        return goods;
    }

    private String chooseSegment(String value, SpecParam p) {
        double val = NumberUtils.toDouble(value);
        String result = "其它";
        // 保存数值段
        for (String segment : p.getSegments().split(",")) {
            String[] segs = segment.split("-");
            // 获取数值范围
            double begin = NumberUtils.toDouble(segs[0]);
            double end = Double.MAX_VALUE;
            if(segs.length == 2){
                end = NumberUtils.toDouble(segs[1]);
            }
            // 判断是否在范围内
            if(val >= begin && val < end){
                if(segs.length == 1){
                    result = segs[0] + p.getUnit() + "以上";
                }else if(begin == 0){
                    result = segs[1] + p.getUnit() + "以下";
                }else{
                    result = segment + p.getUnit();
                }
                break;
            }
        }
        return result;
    }

    public PageResult<Goods> search(SearchRequest searchRequest) {
        if (StringUtils.isBlank(searchRequest.getKey())) {
            return null;
        }
        // 自定义查询构建器
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        queryBuilder.withQuery(QueryBuilders.matchQuery("all", searchRequest.getKey()).operator(Operator.AND));
        queryBuilder.withPageable(PageRequest.of(searchRequest.getPage()-1, searchRequest.getSize()));
        // 添加结果集过滤
        queryBuilder.withSourceFilter(new FetchSourceFilter(new String[]{"id","skus","subTitle"}, null));

        // 执行查询获取结果集
        Page<Goods> goods = this.goodsRepository.search(queryBuilder.build());

        return new PageResult<>(goods.getTotalElements(),goods.getTotalPages(),goods.getContent());
    }

    public void save(Long id) throws IOException {
        Spu spu = this.goodsClient.querySpuById(id);
        Goods goods = this.buildGoods(spu);
        this.goodsRepository.save(goods);
    }
}
