package com.quguai.campustransaction.search.controller;

import com.quguai.campustransaction.search.service.MallSearchService;
import com.quguai.campustransaction.search.vo.SearchParam;
import com.quguai.campustransaction.search.vo.SearchResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.IOException;

@Controller
public class SearchController {

    @Autowired
    private MallSearchService mallSearchService;

    @GetMapping("list.html")
    public String list(SearchParam searchParam, Model model) throws IOException {
        SearchResult result = mallSearchService.search(searchParam);
        model.addAttribute("result", result);
        return "list";
    }
}
