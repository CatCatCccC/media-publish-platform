package com.media.publish.features.publish.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class PublishRecordDTO {
    private Long id;
    private Long articleId;
    private String articleTitle;
    private String platform;
    private Integer status;
    private String statusText;
    private String publishUrl;
    private String errorMessage;
    private LocalDateTime publishedAt;
    private LocalDateTime createdAt;
}
