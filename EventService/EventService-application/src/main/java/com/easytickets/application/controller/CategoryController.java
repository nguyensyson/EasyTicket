package com.easytickets.application.controller;

import com.easytickets.business.dto.CategoryDto;
import com.easytickets.business.services.CategoryService;
import com.easytickets.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("api/v1/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<CategoryDto>>> listCategories() {
        return ResponseEntity.ok(ApiResponse.ok(categoryService.listCategories()));
    }
}
