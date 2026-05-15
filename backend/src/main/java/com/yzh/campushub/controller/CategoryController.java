package com.yzh.campushub.controller;


import com.yzh.campushub.dto.Result;
import com.yzh.campushub.service.CategoryService;
import com.yzh.campushub.utils.Constants;
import com.yzh.campushub.vo.CategoryVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/categories")
public class CategoryController {
    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public ResponseEntity<Result> listCategories() {
        log.info("获取分类列表");
        List<CategoryVO> categories = categoryService.listCategories();
        return ResponseEntity.ok(new Result(Constants.CODE_200, "操作成功", categories, null));
    }

}
