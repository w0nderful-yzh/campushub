package com.yzh.campushub.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yzh.campushub.entity.Category;
import com.yzh.campushub.vo.CategoryVO;

import java.util.List;

public interface CategoryService extends IService<Category> {
    List<CategoryVO> listCategories();
}
