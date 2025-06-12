package com.kyungbae.jpa.menu.service;

import com.kyungbae.jpa.dto.CategoryDto;
import com.kyungbae.jpa.dto.MenuDto;
import com.kyungbae.jpa.menu.entity.Category;
import com.kyungbae.jpa.menu.entity.Menu;
import com.kyungbae.jpa.menu.repository.CategoryRepository;
import com.kyungbae.jpa.menu.repository.MenuRepository;
import com.kyungbae.jpa.util.PageUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class MenuService {

    private final PageUtil pageUtil;
    private final MenuRepository menuRepository;
    private final ModelMapper modelMapper;
    private final CategoryRepository categoryRepository;

    // 1. findById
    public MenuDto findMenuByCode(int menuCode) {

        // findById(식별자) : Optional<T>
        // * Optional : NPE 방지를 위한 다양한 기능 제공

        Menu menu = menuRepository.findById(menuCode)
                .orElseThrow(/*발생시킬예외객체*/
                        () -> new IllegalArgumentException("잘못된 메뉴 코드입니다.")
                );

        // Menu 엔티티 -> Menu DTO로 변환해서 반환
        // Entity <-> DTO 상호 변환 라이브러리 사용 : ModelMapper
        return modelMapper.map(menu, MenuDto.class);
    }

    // 2. findAll
    public List<MenuDto> findMenuList() {
        // 1) findAll() : List<T>
//        List<Menu> menuList = menuRepository.findAll();

        // 2) findAll(Sort) : List<T> - 정렬기준을 전달해서 실행
//        List<Menu> menuList = menuRepository.findAll(Sort.by("menuCode").descending()); // 정렬기준이 한개일때
        List<Menu> menuList = menuRepository.findAll(Sort.by( // 정렬기준이 여러개일 경우
                Sort.Order.asc("categoryCode"), // 첫번째 기준
                Sort.Order.desc("menuPrice")    // 두번째 기준
        ));

        return menuList.stream()
                .map(menu -> modelMapper.map(menu, MenuDto.class))
                .toList();
//                .collect(Collectors.toList());
    }

    public Map<String, Object> findMenuList(Pageable pageable) {

        // 3) findAll(Pageable) : Page<T> - 페이지 정보와 해당 요청 페이지에 필요한 엔티티목록조회결과(List<T>)가 담긴 Page 객체 반환
        Page<Menu> menuPage = menuRepository.findAll(pageable);

        /*
        log.info("total count: {}", menuPage.getTotalElements());
        log.info("display: {}", menuPage.getSize());
        log.info("total page: {}", menuPage.getTotalPages());
        log.info("current page: {}", menuPage.getNumber() + 1);
        log.info("is first page: {}", menuPage.isFirst());
        log.info("is last page: {}", menuPage.isLast());
        log.info("sort: {}", menuPage.getSort());
        log.info("current page data num: {}", menuPage.getNumberOfElements());
        log.info("content: {}", menuPage.getContent());
         */

        Map<String, Object> map = pageUtil.getPageInfo(menuPage, 5);
        map.put("menuList",
                menuPage.getContent()
                        .stream()
                        .map(menu -> modelMapper.map(menu, MenuDto.class))
                        .toList()
        );

        return map;
    }

    // 3. Native Query 사용
    public List<CategoryDto> findCategoryList() {
//        List<Category> categories = categoryRepository.findAll(); // 상위카테고리 포함 조회
//        List<Category> categories = categoryRepository.findAllSubCategory();
        List<Category> categories = categoryRepository.findByRefCategoryCodeIsNotNull();

//        log.info("categories: {}", categories);
        // List<Category> -> List<CategoryDto>
        return categories.stream()
                .map((element) -> modelMapper.map(element, CategoryDto.class))
                .toList();
    }

    @Transactional
    public void registMenu(MenuDto newMenu) {
        menuRepository.save(modelMapper.map(newMenu, Menu.class));
    }

    @Transactional
    public void modifyMenu(MenuDto modifyMenu) {
        // 조회 => setter 이용해서 필드 반영 => commit

        // 조회된 엔티티 -> 영속 컨텍스트에 저장 (@Id-@Entity, 스냅샷(복사본))
        Menu menu = menuRepository.findById(modifyMenu.getMenuCode())
                .orElseThrow(() -> new IllegalArgumentException("잘못된 메뉴 번호입니다."));

        // setter 이용해서 엔티티 필드 변경
        menu.setMenuName(modifyMenu.getMenuName());
        menu.setMenuPrice(modifyMenu.getMenuPrice());
        menu.setCategoryCode(modifyMenu.getCategoryCode());
        menu.setOrderableStatus(modifyMenu.getOrderableStatus());
        // setter에 의해서 변경된 값을 스냅샷과 비교해서
        // 변경감지 되면 update쿼리가 쓰기 지연 저장소에 저장
        // commit 시점에서 db에 반영
    }

    public void removeMenu(int code) {
//        menuRepository.deleteById(code);

        Menu menu = menuRepository.findById(code)
                .orElseThrow(() -> new IllegalArgumentException("잘못된 메뉴 번호입니다."));
        menuRepository.delete(menu);
    }

    public List<MenuDto> findMenuByMenuPrice(int price) {

        // Native Query 파라미터 바인딩
//        List<Menu> menuList = menuRepository.findByMenuPrice(price);

        // 쿼리메소드
        List<Menu> menuList = menuRepository.findByMenuPriceEquals(price);

        return menuList.stream()
                .map((element) -> modelMapper.map(element, MenuDto.class))
                .toList();

    }

    public List<MenuDto> findMenuByName(String name){
        List<Menu> menuList = menuRepository.findByMenuNameContaining(name);

        return menuList.stream()
                .map((element) -> modelMapper.map(element, MenuDto.class))
                .toList();
    }

    public List<MenuDto> findMenuByPriceAndName(String[] queryArr) {
        // 전달된 가격 이상 그리고 메뉴명이 포함된
        log.info(Arrays.toString(queryArr));
        return menuRepository.findByMenuPriceGreaterThanEqualAndMenuNameContaining(Integer.parseInt(queryArr[0]), queryArr[1])
                .stream()
                .map((element) -> modelMapper.map(element, MenuDto.class))
                .collect(Collectors.toList());
    }
}
