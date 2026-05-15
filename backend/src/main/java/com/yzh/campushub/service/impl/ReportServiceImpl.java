package com.yzh.campushub.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yzh.campushub.dto.CreateReportDTO;
import com.yzh.campushub.dto.Result;
import com.yzh.campushub.entity.*;
import com.yzh.campushub.mapper.*;
import com.yzh.campushub.service.ReportService;
import com.yzh.campushub.utils.UserContext;
import com.yzh.campushub.vo.ReportVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReportServiceImpl extends ServiceImpl<ReportMapper, Report> implements ReportService {

    @Autowired
    private PostMapper postMapper;

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private UserMapper userMapper;

    @Override
    public Result createReport(CreateReportDTO dto) {
        Long userId = UserContext.getUserId();
        if (userId == null) userId = 1L;

        if (dto.getTargetType() == null || dto.getTargetId() == null) {
            return Result.fail("举报目标不能为空");
        }
        if (dto.getReason() == null || dto.getReason().trim().isEmpty()) {
            return Result.fail("举报原因不能为空");
        }

        // Validate target exists
        if (dto.getTargetType() == 1) {
            Post post = postMapper.selectById(dto.getTargetId());
            if (post == null || post.getIsDeleted() == 1) {
                return Result.fail("帖子不存在");
            }
        } else if (dto.getTargetType() == 2) {
            Comment comment = commentMapper.selectById(dto.getTargetId());
            if (comment == null || comment.getIsDeleted() == 1) {
                return Result.fail("评论不存在");
            }
        } else if (dto.getTargetType() == 3) {
            User user = userMapper.selectById(dto.getTargetId());
            if (user == null) {
                return Result.fail("用户不存在");
            }
        } else {
            return Result.fail("无效的举报类型");
        }

        // Check duplicate
        LambdaQueryWrapper<Report> dupQuery = new LambdaQueryWrapper<>();
        dupQuery.eq(Report::getReportUserId, userId);
        dupQuery.eq(Report::getTargetType, dto.getTargetType());
        dupQuery.eq(Report::getTargetId, dto.getTargetId());
        dupQuery.eq(Report::getStatus, 0);
        if (count(dupQuery) > 0) {
            return Result.fail("你已经举报过该内容，请等待处理");
        }

        Report report = new Report();
        report.setReportUserId(userId);
        report.setTargetType(dto.getTargetType());
        report.setTargetId(dto.getTargetId());
        report.setReason(dto.getReason().trim());
        report.setStatus(0);
        report.setCreateTime(LocalDateTime.now());
        report.setUpdateTime(LocalDateTime.now());
        save(report);

        return Result.ok();
    }

    @Override
    public Result listReports(Integer status, Integer pageNum, Integer pageSize) {
        Page<Report> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Report> query = new LambdaQueryWrapper<>();
        if (status != null) {
            query.eq(Report::getStatus, status);
        }
        query.orderByDesc(Report::getCreateTime);
        Page<Report> resultPage = page(page, query);

        List<Report> reports = resultPage.getRecords();
        if (reports.isEmpty()) {
            return Result.ok(new ArrayList<>(), 0L);
        }

        Set<Long> userIds = new HashSet<>();
        reports.forEach(r -> {
            userIds.add(r.getReportUserId());
            if (r.getHandleUserId() != null) userIds.add(r.getHandleUserId());
        });

        List<User> users = userMapper.selectBatchIds(userIds);
        Map<Long, User> userMap = users.stream()
                .collect(Collectors.toMap(User::getId, u -> u));

        List<ReportVO> voList = reports.stream().map(r -> {
            ReportVO vo = new ReportVO();
            vo.setId(r.getId());
            vo.setReportUserId(r.getReportUserId());
            vo.setTargetType(r.getTargetType());
            vo.setTargetId(r.getTargetId());
            vo.setReason(r.getReason());
            vo.setStatus(r.getStatus());
            vo.setHandleUserId(r.getHandleUserId());
            vo.setHandleResult(r.getHandleResult());
            vo.setCreateTime(r.getCreateTime());
            vo.setUpdateTime(r.getUpdateTime());

            User reporter = userMap.get(r.getReportUserId());
            if (reporter != null) vo.setReportNickname(reporter.getNickname());

            if (r.getHandleUserId() != null) {
                User handler = userMap.get(r.getHandleUserId());
                if (handler != null) vo.setHandleNickname(handler.getNickname());
            }
            return vo;
        }).collect(Collectors.toList());

        return Result.ok(voList, resultPage.getTotal());
    }

    @Override
    public Result handleReport(Long reportId, String handleResult) {
        Long userId = UserContext.getUserId();
        if (userId == null) userId = 1L;

        Report report = getById(reportId);
        if (report == null) {
            return Result.fail("举报不存在");
        }
        if (report.getStatus() != 0) {
            return Result.fail("该举报已处理");
        }

        report.setStatus(1);
        report.setHandleUserId(userId);
        report.setHandleResult(handleResult);
        report.setUpdateTime(LocalDateTime.now());
        updateById(report);

        return Result.ok();
    }
}
