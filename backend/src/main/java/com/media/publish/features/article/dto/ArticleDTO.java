package com.media.publish.features.article.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ArticleDTO {
    private Long id;
    private String title;
    private String content;
    private String summary;
    private String coverImage;
    private List<String> images;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
