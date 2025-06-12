package com.kyungbae.jpa.menu.controller;

import com.kyungbae.jpa.dto.CategoryDto;
import com.kyungbae.jpa.dto.MenuDto;
import com.kyungbae.jpa.menu.service.MenuService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@RequestMapping("/menu")
@Controller
public class MenuController {

    private final MenuService menuService;

    @GetMapping("/{menuCode}")
    public String menuDetail(@PathVariable int menuCode, Model model){
        MenuDto menu = menuService.findMenuByCode(menuCode);
        model.addAttribute("menu", menu);

        return "menu/detail";
    }

    /* 페이징 전
    @GetMapping("/list")
    public String menuList(Model model){
        List<MenuDto> menuList = menuService.findMenuList();
        model.addAttribute("menuList", menuList);
        return "menu/list";
    }
     */

    /*
        ## Pageable
        1. 페이징 처리에 필요한 정보 (page, size, sort)를 처리하는 인터페이스
        2. Pageable 객체를 통해서 페이징 처리와 정렬을 동시에 처리할 수 있음
        3. 사용방법
            1) 페이징 처리에 필요한 정보를 따로 파라미터를 전달받아 직접 생성하는 방법
                PageRequest.of(요청페이지번호, 조회할데이터건수, Sort객체)
            2) 정해진 파라미터(page, size, sort)로 전달받아 생성된 객체에 바로 주입하는 방법
                @PageableDefault Pageable pageable
                => 전달된 파리미터가 없는 경우 기본값이 초기화됨
                    (page=0, size=10, 정렬기준없음)
        4. 주의사항
            Pageable 인터페이스는 조회할 페이지번호를 0부터 인식
            => 넘어오는 페이지번호 파라미터를 -1 처리 해야됨
     */
    // /menu/list?page=xx&size=xx&sort=xxx,asc|desc
    // 페이징 후
    @GetMapping("/list")
    public String menuList(@PageableDefault Pageable pageable, Model model){
        log.info("pageable: {}", pageable);

        // withPage() : 현재 Pageable의 기존설정(size, sort)은 그대로 두고, 페이지 번호만 바꾼 새로운 Pageable 객체를 반환
        pageable = pageable.withPage(pageable.getPageNumber() <= 0 ? 0 : pageable.getPageNumber() - 1);

        if (pageable.getSort().isEmpty()) { // 정렬 파라미터가 존재하지 않을 경우
            pageable = PageRequest.of(
                    pageable.getPageNumber(),
                    pageable.getPageSize(),
                    Sort.by("menuCode").descending() // 기본 정렬 기준
            );
        }

        log.info("변경후 pageable: {}", pageable);

        Map<String, Object> map = menuService.findMenuList(pageable);
        model.addAttribute("menuList", map.get("menuList"));
        model.addAttribute("page", map.get("page"));
        model.addAttribute("beginPage", map.get("beginPage"));
        model.addAttribute("endPage", map.get("endPage"));
        model.addAttribute("isFirst", map.get("isFirst"));
        model.addAttribute("isLast", map.get("isLast"));


        return "menu/list";
    }

    @GetMapping("/regist")
    public void registPage(){}

    @ResponseBody
    @GetMapping(value = "/categories", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<CategoryDto> categoryList(){

        List<CategoryDto> list = menuService.findCategoryList();
//        log.info(list.toString());

        return list;
    }

    @PostMapping("/regist")
    public String registMenu(MenuDto newMenu){
        menuService.registMenu(newMenu);
        return "redirect:/menu/list";
    }

    @GetMapping("/modify")
    public void modifyPage(int code, Model model){
        model.addAttribute("menu", menuService.findMenuByCode(code));
    }

    @PostMapping("/modify")
    public String modifyMenu(MenuDto modifyMenu){
        menuService.modifyMenu(modifyMenu);
        return "redirect:/menu/" + modifyMenu.getMenuCode();
    }

    @GetMapping("/remove")
    public String removeMenu(int code){

        menuService.removeMenu(code);

        return "redirect:/";
    }

    @GetMapping("/search")
    public String searchMenu(String type, String query){

        List<MenuDto> menuList = new ArrayList<>();
        if ("price".equals(type)) {
            menuList = menuService.findMenuByMenuPrice(Integer.parseInt(query));
        } else if ("name".equals(type)) {
            menuList = menuService.findMenuByName(query);
        } else if ("both".equals(type)) { // String query = 10000, 마늘
            log.info(query);
            menuList = menuService.findMenuByPriceAndName(query.split(","));
        }

        menuList.forEach(System.out::println);

        return "redirect:/";
    }

}
