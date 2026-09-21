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

    // ---- Sandbox testing only -------------------------------------------------
    // Qikink's sandbox cannot see the products in your dashboard, so in sandbox mode
    // orders are sent with design details (search_from_my_products = 0).
    // Live mode ignores these and uses your dashboard products by SKU.

    /** Any design code (a new code creates a new design in your account). */
    private String designCode = "SHOPEASE-TEST";

    /** Public https link of a print-ready design image (PNG). Required in sandbox mode. */
    private String designLink = "";

    /** Public https link of a mockup image (JPG/PNG). Required in sandbox mode. */
    private String mockupLink = "";

    private String designWidthInches = "10";

    private String designHeightInches = "10";

    /** fr = front, bk = back, lp = left pocket, rp = right pocket, rs = right shoulder, ls = left shoulder. */
    private String placementSku = "fr";
}
