package com.leyou.goods.service;


import com.leyou.goods.util.ThreadUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.File;
import java.io.PrintWriter;

@Service
public class GoodsHtmlService {

    @Autowired
    private TemplateEngine engine;

    @Autowired
    private  GoodsService goodsService;

    private static final Logger LOGGER = LoggerFactory.getLogger(GoodsHtmlService.class);

    //创建html页面
    public void createHtml(Long spuId) {
        //初始化上下文对象
        Context context = new Context();
        //设置数据模型给上下文
        context.setVariables(this.goodsService.loadData(spuId));
        PrintWriter printWriter = null;
        try {
            printWriter = new PrintWriter(new File("E:\\springboot-project\\tools\\nginx-1.14.0\\html\\item\\" + spuId + ".html"));

            this.engine.process("item", context, printWriter);
        }catch (Exception e){
            LOGGER.error("页面静态化出错: {}," + e, spuId);
        }finally {
            if (printWriter != null){
                printWriter.close();
            }
        }
    }

    public void deleteHtml(Long spuId){
        File file = new File("E:\\springboot-project\\tools\\nginx-1.14.0\\html\\item\\" + spuId + ".html");
        file.deleteOnExit();
    }

    public void asyncExcute(Long spuId){
        ThreadUtils.execute(() ->createHtml(spuId));

    }

}
