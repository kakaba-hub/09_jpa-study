package com.kyungbae.jpa.dto;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@ToString
public class CategoryDto {
    private Integer categoryCode;
    private String categoryName;;
    private Integer refCategoryCode;;
}
