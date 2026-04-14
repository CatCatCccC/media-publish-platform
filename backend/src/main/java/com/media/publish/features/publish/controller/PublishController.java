package com.media.publish.features.publish.controller;

import com.media.publish.features.publish.dto.PublishRecordDTO;
import com.media.publish.features.publish.dto.PublishRequest;
import com.media.publish.features.publish.service.PublishService;
import com.media.publish.infrastructure.response.ApiResponse;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/publish")
@RequiredArgsConstructor
public class PublishController {

    private final PublishService publishService;

    @GetMapping("/records")
    public ApiResponse<Page<PublishRecordDTO>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer status) {
        return ApiResponse.success(publishService.list(page, size, status));
    }

    @GetMapping("/records/article/{articleId}")
    public ApiResponse<List<PublishRecordDTO>> getByArticleId(@PathVariable Long articleId) {
        return ApiResponse.success(publishService.getByArticleId(articleId));
    }

    @PostMapping
    public ApiResponse<List<PublishRecordDTO>> publish(@RequestBody PublishRequest request) {
        return ApiResponse.success(publishService.publish(request));
    }
}
