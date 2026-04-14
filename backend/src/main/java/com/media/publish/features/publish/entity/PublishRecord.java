package com.media.publish.features.publish.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("publish_record")
public class PublishRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long articleId;

    private String platform;

    private Integer status;

    private String publishUrl;

    private String errorMessage;

    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime publishedAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime createdAt;
}
