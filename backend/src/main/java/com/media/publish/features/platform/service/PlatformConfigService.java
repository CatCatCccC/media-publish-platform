package com.media.publish.features.platform.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.media.publish.common.utils.EncryptionUtil;
import com.media.publish.features.platform.dto.PlatformConfigDTO;
import com.media.publish.features.platform.dto.PlatformConfigRequest;
import com.media.publish.features.platform.dto.TestConnectionRequest;
import com.media.publish.features.platform.entity.PlatformConfig;
import com.media.publish.features.platform.mapper.PlatformConfigMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PlatformConfigService {

    private final PlatformConfigMapper platformConfigMapper;

    @Value("${app.encryption.secret-key}")
    private String secretKey;

    private static final List<String> VALID_PLATFORMS = List.of("CSDN", "ZHIHU", "WECHAT");

    public List<PlatformConfigDTO> list() {
        LambdaQueryWrapper<PlatformConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PlatformConfig::getDeleted, 0);
        wrapper.orderByAsc(PlatformConfig::getPlatform);
        
        List<PlatformConfig> configs = platformConfigMapper.selectList(wrapper);
        
        return configs.stream().map(this::toDTO).collect(Collectors.toList());
    }

    public PlatformConfigDTO getByPlatform(String platform) {
        PlatformConfig config = findByPlatform(platform);
        if (config == null) {
            throw new IllegalArgumentException("平台配置不存在");
        }
        return toDTO(config);
    }

    @Transactional
    public PlatformConfigDTO save(PlatformConfigRequest request) {
        validatePlatform(request.getPlatform());
        
        if (request.getCredentials() == null || request.getCredentials().trim().isEmpty()) {
            throw new IllegalArgumentException("凭证不能为空");
        }

        PlatformConfig config = findByPlatform(request.getPlatform());
        boolean isCreate = (config == null);
        
        if (isCreate) {
            config = new PlatformConfig();
            config.setPlatform(request.getPlatform());
            config.setDeleted(0);
        }
        
        try {
            String encrypted = EncryptionUtil.encrypt(request.getCredentials(), secretKey);
            config.setCredentials(encrypted);
        } catch (Exception e) {
            throw new RuntimeException("凭证加密失败", e);
        }
        
        if (request.getEnabled() != null) {
            config.setEnabled(request.getEnabled());
        } else {
            config.setEnabled(true);
        }
        
        if (isCreate) {
            platformConfigMapper.insert(config);
        } else {
            platformConfigMapper.updateById(config);
        }
        
        return toDTO(config);
    }

    @Transactional
    public void delete(String platform) {
        PlatformConfig config = findByPlatform(platform);
        if (config == null) {
            throw new IllegalArgumentException("平台配置不存在");
        }
        config.setDeleted(1);
        platformConfigMapper.updateById(config);
    }

    public Map<String, Object> testConnection(TestConnectionRequest request) {
        validatePlatform(request.getPlatform());
        
        String credentials = request.getCredentials();
        if (credentials == null || credentials.trim().isEmpty()) {
            PlatformConfig config = findByPlatform(request.getPlatform());
            if (config == null) {
                Map<String, Object> result = new HashMap<>();
                result.put("success", false);
                result.put("message", "请先配置凭证");
                return result;
            }
            try {
                credentials = EncryptionUtil.decrypt(config.getCredentials(), secretKey);
            } catch (Exception e) {
                Map<String, Object> result = new HashMap<>();
                result.put("success", false);
                result.put("message", "凭证解密失败");
                return result;
            }
        }

        boolean success = simulateConnectionTest(request.getPlatform(), credentials);
        Map<String, Object> result = new HashMap<>();
        result.put("success", success);
        result.put("message", success ? "连接成功" : "连接失败");
        return result;
    }

    public String getDecryptedCredentials(String platform) {
        PlatformConfig config = findByPlatform(platform);
        if (config == null) {
            throw new IllegalArgumentException("平台配置不存在");
        }
        try {
            return EncryptionUtil.decrypt(config.getCredentials(), secretKey);
        } catch (Exception e) {
            throw new RuntimeException("凭证解密失败", e);
        }
    }

    private PlatformConfig findByPlatform(String platform) {
        LambdaQueryWrapper<PlatformConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PlatformConfig::getPlatform, platform);
        wrapper.eq(PlatformConfig::getDeleted, 0);
        return platformConfigMapper.selectOne(wrapper);
    }

    private void validatePlatform(String platform) {
        if (!VALID_PLATFORMS.contains(platform)) {
            throw new IllegalArgumentException("不支持的平台: " + platform + 
                "，支持的平台: " + String.join(", ", VALID_PLATFORMS));
        }
    }

    private PlatformConfigDTO toDTO(PlatformConfig config) {
        PlatformConfigDTO dto = new PlatformConfigDTO();
        dto.setId(config.getId());
        dto.setPlatform(config.getPlatform());
        dto.setCredentialsMasked(EncryptionUtil.mask(config.getCredentials()));
        dto.setEnabled(config.getEnabled());
        dto.setCreatedAt(config.getCreatedAt());
        dto.setUpdatedAt(config.getUpdatedAt());
        return dto;
    }

    private boolean simulateConnectionTest(String platform, String credentials) {
        if (credentials == null || credentials.trim().isEmpty()) {
            return false;
        }
        if (credentials.length() < 10) {
            return false;
        }
        return true;
    }
}
