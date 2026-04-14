package com.media.publish.features.article.dto;

import lombok.Data;

@Data
public class ArticleUpdateRequest {
    private Long id;
    private String title;
    private String content;
    private String summary;
    private String coverImage;
    private String images;
    private Integer status;
}
