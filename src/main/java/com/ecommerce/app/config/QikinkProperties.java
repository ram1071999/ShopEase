package com.ecommerce.app.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "qikink")
@Data
public class QikinkProperties {

    /** Master switch — auto-push to Qikink is skipped entirely if this is false. */
    private boolean enabled = false;

    /** true = https://sandbox.qikink.com (testing), false = https://api.qikink.com (live orders, real cost). */
    private boolean sandbox = true;

    private String clientId;

    private String clientSecret;
}
