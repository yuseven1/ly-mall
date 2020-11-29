package com.leyou.item.api;

import com.leyou.common.pojo.PageResult;
import com.leyou.item.bo.SpuBo;
import com.leyou.item.pojo.Sku;
import com.leyou.item.pojo.Spu;
import com.leyou.item.pojo.SpuDetail;
import org.springframework.web.bind.annotation.*;

import java.util.List;

public interface GoodsApi {

    @GetMapping("spu/page")
    PageResult<SpuBo> querySpuBypage(
            @RequestParam(value = "key", required = false)String key,
            @RequestParam(value = "saleable", required = false)Boolean saleable,
            @RequestParam(value = "page", defaultValue = "1")Integer page,
            @RequestParam(value = "rows", defaultValue = "5")Integer rows);


    @PostMapping("goods")
    void saveGoods(@RequestBody SpuBo spuBo);

    @GetMapping("spu/detail/{spuId}")
    SpuDetail querySpuDetailBySpuId(@PathVariable("spuId")Long spuId);

    @GetMapping("sku/list")
    List<Sku> querySkusBySpuId(@RequestParam("id")Long spuId);

    @PutMapping("goods")
    void updateGoods (@RequestBody SpuBo spuBo);

    @GetMapping("{id}")
    Spu querySpuById (@PathVariable("id")Long id);
}
