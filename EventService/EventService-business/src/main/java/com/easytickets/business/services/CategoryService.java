package com.easytickets.business.services;

import com.easytickets.business.dto.CategoryDto;

import java.util.List;

public interface CategoryService {
    List<CategoryDto> listCategories();
}
