package com.leyou.goods.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

@Service
public class HtmlService {

    @Autowired
    private TemplateEngine templateEngine;

    @Autowired
    private GoodsService goodsService;

    public  void createHtml(Long spuId){
        // 初始化运行上下文
        Context context = new Context();
        // 设置数据模型
        context.setVariables(this.goodsService.loadData(spuId));
        // 静态文件生成到服务器本地
        PrintWriter writer = null;
        try {
            File file = new File("D:\\Softwares\\nginx-1.14.0\\html\\item\\" + spuId + ".html");
            writer = new PrintWriter(file);
            this.templateEngine.process("item", context, writer);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (writer!=null) {
                writer.close();
            }
        }
    }

    public void deleteHtml(Long id) {
        new File("D:\\Softwares\\nginx-1.14.0\\html\\item\\" + id + ".html").deleteOnExit();
    }
}
