package com.easytickets.business.repo;

import com.easytickets.business.dto.CategoryDto;

import java.util.List;
import java.util.Optional;

public interface CategoryRepo {
    List<CategoryDto> findAll();

    Optional<CategoryDto> findById(String id);
}
