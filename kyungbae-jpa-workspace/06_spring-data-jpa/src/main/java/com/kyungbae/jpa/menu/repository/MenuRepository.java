package com.kyungbae.jpa.menu.repository;

import com.kyungbae.jpa.menu.entity.Menu;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MenuRepository extends JpaRepository<Menu, Integer> {
    void removeMenuByMenuCode(Integer menuCode); // entity type, Id type 제시해야함



}
