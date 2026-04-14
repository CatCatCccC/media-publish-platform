package com.media.publish.features.article.dto;

import lombok.Data;

@Data
public class ArticleCreateRequest {
    private String title;
    private String content;
    private String summary;
    private String coverImage;
    private String images;
}
