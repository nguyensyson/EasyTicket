package com.easytickets.infratructures.mapper;

import com.easytickets.business.dto.CategoryDto;
import com.easytickets.infratructures.model.Category;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CategoryMapper {
    CategoryDto toDto(Category entity);

    Category toEntity(CategoryDto dto);
}
