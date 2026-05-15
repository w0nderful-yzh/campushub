package com.yzh.campushub.controller;

import com.yzh.campushub.dto.CreateActivityDTO;
import com.yzh.campushub.dto.Result;
import com.yzh.campushub.service.ActivityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/activities")
public class ActivityController {

    @Autowired
    private ActivityService activityService;

    @PostMapping
    public Result createActivity(@RequestBody CreateActivityDTO dto) {
        return activityService.createActivity(dto);
    }

    @GetMapping
    public Result listActivities(@RequestParam(required = false) Integer activityType,
                                 @RequestParam(required = false) String keyword,
                                 @RequestParam(defaultValue = "1") Integer pageNum,
                                 @RequestParam(defaultValue = "10") Integer pageSize) {
        return activityService.listActivities(activityType, keyword, pageNum, pageSize);
    }

    @GetMapping("/{id}")
    public Result getActivityDetail(@PathVariable Long id) {
        return activityService.getActivityDetail(id);
    }

    @PutMapping("/{id}")
    public Result updateActivity(@PathVariable Long id, @RequestBody CreateActivityDTO dto) {
        return activityService.updateActivity(id, dto);
    }

    @DeleteMapping("/{id}")
    public Result cancelActivity(@PathVariable Long id) {
        return activityService.cancelActivity(id);
    }

    @PostMapping("/{id}/signup")
    public Result signup(@PathVariable Long id) {
        return activityService.signup(id);
    }

    @DeleteMapping("/{id}/signup")
    public Result cancelSignup(@PathVariable Long id) {
        return activityService.cancelSignup(id);
    }

    @GetMapping("/{id}/signups")
    public Result listSignups(@PathVariable Long id,
                              @RequestParam(defaultValue = "1") Integer pageNum,
                              @RequestParam(defaultValue = "20") Integer pageSize) {
        return activityService.listSignups(id, pageNum, pageSize);
    }

    @GetMapping("/my")
    public Result listMyActivities(@RequestParam(defaultValue = "1") Integer pageNum,
                                   @RequestParam(defaultValue = "10") Integer pageSize) {
        return activityService.listMyActivities(pageNum, pageSize);
    }

    @GetMapping("/my-signups")
    public Result listMySignups(@RequestParam(defaultValue = "1") Integer pageNum,
                                @RequestParam(defaultValue = "10") Integer pageSize) {
        return activityService.listMySignups(pageNum, pageSize);
    }
}
