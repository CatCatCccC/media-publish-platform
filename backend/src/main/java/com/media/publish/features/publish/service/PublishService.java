package com.media.publish.features.publish.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.media.publish.features.article.entity.Article;
import com.media.publish.features.article.mapper.ArticleMapper;
import com.media.publish.features.platform.service.PlatformConfigService;
import com.media.publish.features.publish.dto.PublishRecordDTO;
import com.media.publish.features.publish.dto.PublishRequest;
import com.media.publish.features.publish.entity.PublishRecord;
import com.media.publish.features.publish.mapper.PublishRecordMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PublishService {

    private final PublishRecordMapper publishRecordMapper;
    private final ArticleMapper articleMapper;
    private final PlatformConfigService platformConfigService;

    private static final int STATUS_PENDING = 0;
    private static final int STATUS_SUCCESS = 1;
    private static final int STATUS_FAILED = 2;

    public Page<PublishRecordDTO> list(int page, int size, Integer status) {
        Page<PublishRecord> recordPage = new Page<>(page, size);
        LambdaQueryWrapper<PublishRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PublishRecord::getDeleted, 0);
        
        if (status != null) {
            wrapper.eq(PublishRecord::getStatus, status);
        }
        
        wrapper.orderByDesc(PublishRecord::getCreatedAt);
        
        Page<PublishRecord> result = publishRecordMapper.selectPage(recordPage, wrapper);
        
        Page<PublishRecordDTO> dtoPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        dtoPage.setRecords(result.getRecords().stream().map(this::toDTO).toList());
        return dtoPage;
    }

    public List<PublishRecordDTO> getByArticleId(Long articleId) {
        LambdaQueryWrapper<PublishRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PublishRecord::getArticleId, articleId);
        wrapper.eq(PublishRecord::getDeleted, 0);
        wrapper.orderByDesc(PublishRecord::getCreatedAt);
        
        List<PublishRecord> records = publishRecordMapper.selectList(wrapper);
        return records.stream().map(this::toDTO).toList();
    }

    @Transactional
    public List<PublishRecordDTO> publish(PublishRequest request) {
        if (request.getArticleId() == null) {
            throw new IllegalArgumentException("文章ID不能为空");
        }
        if (request.getPlatforms() == null || request.getPlatforms().isEmpty()) {
            throw new IllegalArgumentException("请选择至少一个发布平台");
        }

        Article article = articleMapper.selectById(request.getArticleId());
        if (article == null) {
            throw new IllegalArgumentException("文章不存在");
        }

        List<PublishRecordDTO> results = new ArrayList<>();
        
        for (String platform : request.getPlatforms()) {
            PublishRecord record = new PublishRecord();
            record.setArticleId(request.getArticleId());
            record.setPlatform(platform);
            record.setStatus(STATUS_PENDING);
            record.setDeleted(0);
            record.setPublishedAt(LocalDateTime.now());
            
            publishRecordMapper.insert(record);
            
            try {
                String publishUrl = simulatePublish(platform, article);
                record.setStatus(STATUS_SUCCESS);
                record.setPublishUrl(publishUrl);
                record.setErrorMessage(null);
            } catch (Exception e) {
                record.setStatus(STATUS_FAILED);
                record.setErrorMessage(e.getMessage());
            }
            
            publishRecordMapper.updateById(record);
            results.add(toDTO(record));
        }
        
        return results;
    }

    private String simulatePublish(String platform, Article article) {
        try {
            String credentials = platformConfigService.getDecryptedCredentials(platform);
            
            Thread.sleep(500);
            
            String url = switch (platform) {
                case "CSDN" -> "https://blog.csdn.net/article/" + System.currentTimeMillis();
                case "ZHIHU" -> "https://zhuanlan.zhihu.com/p/" + System.currentTimeMillis();
                case "WECHAT" -> "https://mp.weixin.qq.com/s/" + System.currentTimeMillis();
                default -> "https://example.com/article/" + System.currentTimeMillis();
            };
            
            return url;
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("平台凭证未配置，请先配置平台凭证");
        } catch (Exception e) {
            throw new RuntimeException("发布失败: " + e.getMessage());
        }
    }

    private PublishRecordDTO toDTO(PublishRecord record) {
        PublishRecordDTO dto = new PublishRecordDTO();
        dto.setId(record.getId());
        dto.setArticleId(record.getArticleId());
        dto.setPlatform(record.getPlatform());
        dto.setStatus(record.getStatus());
        dto.setPublishUrl(record.getPublishUrl());
        dto.setErrorMessage(record.getErrorMessage());
        dto.setPublishedAt(record.getPublishedAt());
        dto.setCreatedAt(record.getCreatedAt());
        
        Article article = articleMapper.selectById(record.getArticleId());
        dto.setArticleTitle(article != null ? article.getTitle() : "未知文章");
        
        dto.setStatusText(switch (record.getStatus()) {
            case STATUS_PENDING -> "发布中";
            case STATUS_SUCCESS -> "已发布";
            case STATUS_FAILED -> "发布失败";
            default -> "未知";
        });
        
        return dto;
    }
}
