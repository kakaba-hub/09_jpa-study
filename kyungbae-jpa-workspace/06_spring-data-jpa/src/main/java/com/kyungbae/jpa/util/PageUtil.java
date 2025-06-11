package com.kyungbae.jpa.util;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class PageUtil {

    public Map<String, Object> getPageInfo(Page page, int pagePerBlock){

        int currentPage = page.getNumber() + 1;
        int beginPage = (currentPage - 1) / pagePerBlock * pagePerBlock + 1;
        int endPage = Math.min( beginPage + pagePerBlock - 1, page.getTotalPages());

        Map<String, Object> map = new HashMap<>();
        map.put("totalCount", page.getTotalElements());
        map.put("page", currentPage);
        map.put("size", page.getSize());
        map.put("pagePerBlock", pagePerBlock);
        map.put("totalPage", page.getTotalPages());
        map.put("beginPage", beginPage);
        map.put("endPage", endPage);
        map.put("isFirst", page.isFirst());
        map.put("isLast", page.isLast());

        return map;
    }

}
