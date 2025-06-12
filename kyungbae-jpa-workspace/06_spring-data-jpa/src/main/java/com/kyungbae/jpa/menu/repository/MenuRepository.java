package com.kyungbae.jpa.menu.repository;

import com.kyungbae.jpa.menu.entity.Menu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MenuRepository extends JpaRepository<Menu, Integer> {
    void removeMenuByMenuCode(Integer menuCode); // entity type, Id type 제시해야함

//    @Query(value="SELECT menu_code, menu_name, menu_price, category_code, orderable_status FROM tbl_menu WHERE menu_price=?1", nativeQuery = true)
//    List<Menu> findByMenuPrice(int price);
    List<Menu> findByMenuPriceEquals(int price);

    List<Menu> findByMenuNameContaining(String name);

    List<Menu> findByMenuPriceGreaterThanEqualAndMenuNameContaining(int price, String name);

}
