package com.yzh.campushub.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yzh.campushub.entity.Comment;
import com.yzh.campushub.mapper.CommentMapper;
import com.yzh.campushub.service.CommentService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements CommentService {

}
