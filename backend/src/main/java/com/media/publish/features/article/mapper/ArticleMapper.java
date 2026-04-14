package com.media.publish.features.article.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.media.publish.features.article.entity.Article;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ArticleMapper extends BaseMapper<Article> {
}
