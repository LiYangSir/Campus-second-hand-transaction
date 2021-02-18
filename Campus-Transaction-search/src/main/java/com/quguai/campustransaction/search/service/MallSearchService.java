package com.quguai.campustransaction.search.service;

import com.quguai.campustransaction.search.vo.SearchParam;
import com.quguai.campustransaction.search.vo.SearchResult;

import java.io.IOException;

public interface MallSearchService {
    SearchResult search(SearchParam searchParam) throws IOException;
}
