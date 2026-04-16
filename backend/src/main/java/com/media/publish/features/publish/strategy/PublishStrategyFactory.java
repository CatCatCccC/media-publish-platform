package com.media.publish.features.publish.strategy;

import com.media.publish.features.publish.constant.PlatformType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 发布策略工厂
 * 根据平台类型获取对应的发布策略
 */
@Slf4j
@Component
public class PublishStrategyFactory {

    private final Map<PlatformType, PublishStrategy> strategyMap;

    /**
     * 构造函数，自动注入所有PublishStrategy实现
     */
    public PublishStrategyFactory(List<PublishStrategy> strategies) {
        this.strategyMap = strategies.stream()
                .collect(Collectors.toMap(
                        PublishStrategy::getPlatformType,
                        Function.identity()
                ));
        
        log.info("已加载 {} 个发布策略: {}", 
                strategyMap.size(), 
                strategyMap.keySet());
    }

    /**
     * 获取指定平台的发布策略
     *
     * @param platformType 平台类型
     * @return 发布策略
     */
    public PublishStrategy getStrategy(PlatformType platformType) {
        PublishStrategy strategy = strategyMap.get(platformType);
        if (strategy == null) {
            throw new IllegalArgumentException("不支持的平台: " + platformType);
        }
        return strategy;
    }

    /**
     * 根据平台代码获取发布策略
     *
     * @param platformCode 平台代码
     * @return 发布策略
     */
    public PublishStrategy getStrategy(String platformCode) {
        PlatformType platformType = PlatformType.fromCode(platformCode);
        return getStrategy(platformType);
    }

    /**
     * 检查是否支持指定平台
     *
     * @param platformCode 平台代码
     * @return 是否支持
     */
    public boolean isSupported(String platformCode) {
        try {
            PlatformType platformType = PlatformType.fromCode(platformCode);
            return strategyMap.containsKey(platformType);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
