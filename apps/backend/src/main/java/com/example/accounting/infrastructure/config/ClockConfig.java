package com.example.accounting.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

/**
 * Clock の Bean 定義
 *
 * <p>システムクロックを提供する。テスト時には固定クロックに置き換え可能。</p>
 */
@Configuration
public class ClockConfig {

    /**
     * システムクロックを提供する
     *
     * @return システムデフォルトゾーンのクロック
     */
    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }
}
