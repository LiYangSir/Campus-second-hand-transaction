package com.quguai.campustransaction.product.web;

import com.quguai.campustransaction.product.entity.CategoryEntity;
import com.quguai.campustransaction.product.service.CategoryService;
import com.quguai.campustransaction.product.vo.Catelog2Vo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

@Controller
public class IndexController {

    @Autowired
    private CategoryService categoryService;

    @GetMapping({"/", "/index.html", "/index"})
    public String indexPage(Model model){
        List<CategoryEntity> categoryEntities = categoryService.getLevelFirstCategories();
        model.addAttribute("categories", categoryEntities);
        return "index";
    }

    @ResponseBody
    @GetMapping("/index/catalog.json")
    public Map<String, List<Catelog2Vo>> getCatelogJson() {
//        return categoryService.getCatelogJSONFromDb();
        return categoryService.getCatelogJsonFromCache();
    }

}
