package com.yzh.campushub.controller;

import com.yzh.campushub.dto.CreateReportDTO;
import com.yzh.campushub.dto.Result;
import com.yzh.campushub.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    @Autowired
    private ReportService reportService;

    @PostMapping
    public Result createReport(@RequestBody CreateReportDTO dto) {
        return reportService.createReport(dto);
    }

    @GetMapping
    public Result listReports(@RequestParam(required = false) Integer status,
                              @RequestParam(defaultValue = "1") Integer pageNum,
                              @RequestParam(defaultValue = "10") Integer pageSize) {
        return reportService.listReports(status, pageNum, pageSize);
    }

    @PutMapping("/{reportId}/handle")
    public Result handleReport(@PathVariable Long reportId, @RequestBody Map<String, String> body) {
        return reportService.handleReport(reportId, body.get("handleResult"));
    }
}
