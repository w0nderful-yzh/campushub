package com.yzh.campushub.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yzh.campushub.entity.Category;
import com.yzh.campushub.mapper.CategoryMapper;
import com.yzh.campushub.service.CategoryService;
import org.springframework.stereotype.Service;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

}
