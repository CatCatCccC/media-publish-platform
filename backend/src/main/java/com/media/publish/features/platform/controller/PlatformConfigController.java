package com.media.publish.features.platform.controller;

import com.media.publish.features.platform.dto.PlatformConfigDTO;
import com.media.publish.features.platform.dto.PlatformConfigRequest;
import com.media.publish.features.platform.dto.TestConnectionRequest;
import com.media.publish.features.platform.service.PlatformConfigService;
import com.media.publish.infrastructure.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/platforms")
@RequiredArgsConstructor
public class PlatformConfigController {

    private final PlatformConfigService platformConfigService;

    @GetMapping
    public ApiResponse<List<PlatformConfigDTO>> list() {
        return ApiResponse.success(platformConfigService.list());
    }

    @GetMapping("/{platform}")
    public ApiResponse<PlatformConfigDTO> getByPlatform(@PathVariable String platform) {
        return ApiResponse.success(platformConfigService.getByPlatform(platform));
    }

    @PostMapping
    public ApiResponse<PlatformConfigDTO> save(@RequestBody PlatformConfigRequest request) {
        return ApiResponse.success(platformConfigService.save(request));
    }

    @DeleteMapping("/{platform}")
    public ApiResponse<Void> delete(@PathVariable String platform) {
        platformConfigService.delete(platform);
        return ApiResponse.success();
    }

    @PostMapping("/test")
    public ApiResponse<Map<String, Object>> testConnection(@RequestBody TestConnectionRequest request) {
        Map<String, Object> result = platformConfigService.testConnection(request);
        return ApiResponse.success(result);
    }
}
