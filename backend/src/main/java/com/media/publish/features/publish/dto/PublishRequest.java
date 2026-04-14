package com.media.publish.features.publish.dto;

import lombok.Data;
import java.util.List;

@Data
public class PublishRequest {
    private Long articleId;
    private List<String> platforms;
}
