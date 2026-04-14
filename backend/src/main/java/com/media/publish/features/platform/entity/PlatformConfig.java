package com.media.publish.features.platform.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("platform_config")
public class PlatformConfig {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String platform;

    private String credentials;

    private Boolean enabled;

    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
