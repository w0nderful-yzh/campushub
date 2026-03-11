package com.yzh.campushub.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yzh.campushub.entity.Favorite;
import com.yzh.campushub.mapper.FavoriteMapper;
import com.yzh.campushub.service.FavoriteService;
import org.springframework.stereotype.Service;

@Service
public class FavoriteServiceImpl extends ServiceImpl<FavoriteMapper, Favorite> implements FavoriteService {

}
