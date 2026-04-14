package com.media.publish.features.article.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.media.publish.features.article.dto.ArticleCreateRequest;
import com.media.publish.features.article.dto.ArticleDTO;
import com.media.publish.features.article.dto.ArticleUpdateRequest;
import com.media.publish.features.article.entity.Article;
import com.media.publish.features.article.mapper.ArticleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ArticleService {

    private final ArticleMapper articleMapper;
    private final ObjectMapper objectMapper;

    public Page<ArticleDTO> list(int page, int size) {
        Page<Article> articlePage = new Page<>(page, size);
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Article::getCreatedAt);
        
        Page<Article> result = articleMapper.selectPage(articlePage, wrapper);
        
        Page<ArticleDTO> dtoPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        dtoPage.setRecords(result.getRecords().stream().map(this::toDTO).toList());
        return dtoPage;
    }

    public ArticleDTO getById(Long id) {
        Article article = articleMapper.selectById(id);
        if (article == null) {
            throw new IllegalArgumentException("文章不存在");
        }
        return toDTO(article);
    }

    @Transactional
    public ArticleDTO create(ArticleCreateRequest request) {
        if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("标题不能为空");
        }
        
        Article article = new Article();
        article.setTitle(request.getTitle());
        article.setContent(request.getContent());
        article.setSummary(request.getSummary());
        article.setCoverImage(request.getCoverImage());
        article.setImages(request.getImages());
        article.setStatus(0);
        article.setDeleted(0);
        
        articleMapper.insert(article);
        return toDTO(article);
    }

    @Transactional
    public ArticleDTO update(ArticleUpdateRequest request) {
        if (request.getId() == null) {
            throw new IllegalArgumentException("文章ID不能为空");
        }
        
        Article article = articleMapper.selectById(request.getId());
        if (article == null) {
            throw new IllegalArgumentException("文章不存在");
        }
        
        if (request.getTitle() != null) {
            article.setTitle(request.getTitle());
        }
        if (request.getContent() != null) {
            article.setContent(request.getContent());
        }
        if (request.getSummary() != null) {
            article.setSummary(request.getSummary());
        }
        if (request.getCoverImage() != null) {
            article.setCoverImage(request.getCoverImage());
        }
        if (request.getImages() != null) {
            article.setImages(request.getImages());
        }
        if (request.getStatus() != null) {
            article.setStatus(request.getStatus());
        }
        
        articleMapper.updateById(article);
        return toDTO(article);
    }

    @Transactional
    public void delete(Long id) {
        Article article = articleMapper.selectById(id);
        if (article == null) {
            throw new IllegalArgumentException("文章不存在");
        }
        article.setDeleted(1);
        articleMapper.updateById(article);
    }

    private ArticleDTO toDTO(Article article) {
        ArticleDTO dto = new ArticleDTO();
        dto.setId(article.getId());
        dto.setTitle(article.getTitle());
        dto.setContent(article.getContent());
        dto.setSummary(article.getSummary());
        dto.setCoverImage(article.getCoverImage());
        dto.setStatus(article.getStatus());
        dto.setCreatedAt(article.getCreatedAt());
        dto.setUpdatedAt(article.getUpdatedAt());
        
        if (article.getImages() != null && !article.getImages().isEmpty()) {
            try {
                dto.setImages(objectMapper.readValue(article.getImages(), 
                    new TypeReference<List<String>>() {}));
            } catch (JsonProcessingException e) {
                dto.setImages(Collections.emptyList());
            }
        } else {
            dto.setImages(Collections.emptyList());
        }
        
        return dto;
    }
}
