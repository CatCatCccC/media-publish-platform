package com.media.publish.features.publish.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.media.publish.common.exception.BizException;
import com.media.publish.common.exception.code.PublishErrorCode;
import com.media.publish.features.article.entity.Article;
import com.media.publish.features.article.mapper.ArticleMapper;
import com.media.publish.features.platform.service.PlatformConfigService;
import com.media.publish.features.publish.constant.PublishStatus;
import com.media.publish.features.publish.dto.PublishRecordDTO;
import com.media.publish.features.publish.dto.PublishRequest;
import com.media.publish.features.publish.entity.PublishRecord;
import com.media.publish.features.publish.mapper.PublishRecordMapper;
import com.media.publish.features.publish.strategy.PublishResult;
import com.media.publish.features.publish.strategy.PublishStrategy;
import com.media.publish.features.publish.strategy.PublishStrategyFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 发布服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PublishService {

    private final PublishRecordMapper publishRecordMapper;
    private final ArticleMapper articleMapper;
    private final PlatformConfigService platformConfigService;
    private final PublishStrategyFactory strategyFactory;

    /**
     * 分页查询发布记录
     */
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

    /**
     * 根据文章ID获取发布记录
     */
    public List<PublishRecordDTO> getByArticleId(Long articleId) {
        LambdaQueryWrapper<PublishRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PublishRecord::getArticleId, articleId);
        wrapper.eq(PublishRecord::getDeleted, 0);
        wrapper.orderByDesc(PublishRecord::getCreatedAt);

        List<PublishRecord> records = publishRecordMapper.selectList(wrapper);
        return records.stream().map(this::toDTO).toList();
    }

    /**
     * 发布文章到多个平台
     */
    @Transactional
    public List<PublishRecordDTO> publish(PublishRequest request) {
        // 参数校验
        validatePublishRequest(request);

        // 获取文章
        Article article = getArticle(request.getArticleId());

        List<PublishRecordDTO> results = new ArrayList<>();

        // 遍历平台发布
        for (String platformCode : request.getPlatforms()) {
            PublishRecordDTO result = publishToPlatform(platformCode, article);
            results.add(result);
        }

        return results;
    }

    /**
     * 发布到单个平台
     */
    private PublishRecordDTO publishToPlatform(String platformCode, Article article) {
        // 创建发布记录
        PublishRecord record = createPublishRecord(article.getId(), platformCode);

        try {
            // 获取平台凭证
            String credential = getPlatformCredential(platformCode);

            // 获取发布策略
            PublishStrategy strategy = strategyFactory.getStrategy(platformCode);

            // 执行发布
            log.info("开始发布文章 [{}] 到平台 [{}]", article.getTitle(), platformCode);
            PublishResult result = strategy.publish(article, credential);

            // 更新发布记录
            updatePublishRecord(record, result);

        } catch (BizException e) {
            log.error("发布失败 [{}]: {}", platformCode, e.getMessage());
            updatePublishRecord(record, PublishResult.failed(platformCode, e.getMessage()));
        } catch (Exception e) {
            log.error("发布异常 [{}]: {}", platformCode, e.getMessage(), e);
            updatePublishRecord(record, PublishResult.failed(platformCode, "发布异常: " + e.getMessage()));
        }

        return toDTO(record);
    }

    /**
     * 校验发布请求
     */
    private void validatePublishRequest(PublishRequest request) {
        if (request.getArticleId() == null) {
            throw new BizException(PublishErrorCode.ARTICLE_ID_EMPTY);
        }
        if (request.getPlatforms() == null || request.getPlatforms().isEmpty()) {
            throw new BizException(PublishErrorCode.PLATFORM_EMPTY);
        }
    }

    /**
     * 获取文章
     */
    private Article getArticle(Long articleId) {
        Article article = articleMapper.selectById(articleId);
        if (article == null) {
            throw new BizException(PublishErrorCode.ARTICLE_NOT_FOUND);
        }
        return article;
    }

    /**
     * 获取平台凭证
     */
    private String getPlatformCredential(String platformCode) {
        try {
            return platformConfigService.getDecryptedCredentials(platformCode);
        } catch (Exception e) {
            throw new BizException(PublishErrorCode.PLATFORM_NOT_CONFIGURED, platformCode);
        }
    }

    /**
     * 创建发布记录
     */
    private PublishRecord createPublishRecord(Long articleId, String platformCode) {
        PublishRecord record = new PublishRecord();
        record.setArticleId(articleId);
        record.setPlatform(platformCode);
        record.setStatus(PublishStatus.PENDING.getCode());
        record.setDeleted(0);
        record.setPublishedAt(LocalDateTime.now());
        publishRecordMapper.insert(record);
        return record;
    }

    /**
     * 更新发布记录
     */
    private void updatePublishRecord(PublishRecord record, PublishResult result) {
        record.setStatus(result.getStatus().getCode());
        record.setPublishUrl(result.getArticleUrl());
        record.setErrorMessage(result.getErrorMessage());
        publishRecordMapper.updateById(record);
    }

    /**
     * 转换为DTO
     */
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

        // 获取文章标题
        Article article = articleMapper.selectById(record.getArticleId());
        dto.setArticleTitle(article != null ? article.getTitle() : "未知文章");

        // 设置状态文本
        dto.setStatusText(PublishStatus.fromCode(record.getStatus()).getDescription());

        return dto;
    }
}
