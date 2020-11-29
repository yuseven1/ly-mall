package com.leyou.item.api;

import com.leyou.item.pojo.Brand;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("brand")
public interface BrandApi {

    /**
     * 新增品牌
     * @param brand
     * @param cids
     * @return
     */
    @PostMapping
    void saveBrand (Brand brand, @RequestParam("cids") List<Long> cids);

    @GetMapping("cid/{cid}")
    List<Brand> queryBrandByCid(@PathVariable("cid")Long cid);

    @GetMapping("{id}")
    Brand queryBrandById(@PathVariable("id")Long id);
}
