package com.yzh.campushub.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yzh.campushub.dto.CreateReportDTO;
import com.yzh.campushub.dto.Result;
import com.yzh.campushub.entity.Report;

public interface ReportService extends IService<Report> {
    Result createReport(CreateReportDTO dto);
    Result listReports(Integer status, Integer pageNum, Integer pageSize);
    Result handleReport(Long reportId, String handleResult);
}
