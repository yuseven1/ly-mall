package com.leyou.item.api;

import com.leyou.item.pojo.SpecGroup;
import com.leyou.item.pojo.SpecParam;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@RequestMapping("spec")
public interface SpecificationApi {

    /**
     * 根据分类 ID 查询参数组
     * @param cid
     * @return
     */
    @GetMapping("groups/{cid}")
    List<SpecGroup> querySpecGroupsByCid(@PathVariable("cid") Long cid);

    @GetMapping("params")
    List<SpecParam> queryParams(
            @RequestParam(value="gid", required = false)Long gid,
            @RequestParam(value="cid", required = false)Long cid,
            @RequestParam(value="generic", required = false)Boolean generic,
            @RequestParam(value="searching", required = false)Boolean searching
    );

    @GetMapping("group/param/{cid}")
    List<SpecGroup> queryGroupsWithParam (@PathVariable("cid")Long cid);
}
