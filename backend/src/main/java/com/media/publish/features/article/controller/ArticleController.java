package com.media.publish.features.article.controller;

import com.media.publish.common.utils.FileUtil;
import com.media.publish.features.article.dto.ArticleCreateRequest;
import com.media.publish.features.article.dto.ArticleDTO;
import com.media.publish.features.article.dto.ArticleUpdateRequest;
import com.media.publish.features.article.service.ArticleService;
import com.media.publish.infrastructure.response.ApiResponse;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/articles")
@RequiredArgsConstructor
public class ArticleController {

    private final ArticleService articleService;

    @GetMapping
    public ApiResponse<Page<ArticleDTO>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.success(articleService.list(page, size));
    }

    @GetMapping("/{id}")
    public ApiResponse<ArticleDTO> getById(@PathVariable Long id) {
        return ApiResponse.success(articleService.getById(id));
    }

    @PostMapping
    public ApiResponse<ArticleDTO> create(@RequestBody ArticleCreateRequest request) {
        return ApiResponse.success(articleService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<ArticleDTO> update(@PathVariable Long id, 
                                          @RequestBody ArticleUpdateRequest request) {
        request.setId(id);
        return ApiResponse.success(articleService.update(request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        articleService.delete(id);
        return ApiResponse.success();
    }

    @PostMapping("/upload")
    public ApiResponse<Map<String, String>> upload(@RequestParam("file") MultipartFile file) {
        try {
            String url = FileUtil.upload(file);
            return ApiResponse.success(Map.of("url", url));
        } catch (IOException e) {
            return ApiResponse.error("上传失败: " + e.getMessage());
        }
    }
}
