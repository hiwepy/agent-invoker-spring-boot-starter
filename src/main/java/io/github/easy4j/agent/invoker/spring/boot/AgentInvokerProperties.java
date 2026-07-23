package io.github.easy4j.agent.invoker.spring.boot;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Agent Invoker 集成配置。
 *
 * <p>配置前缀：{@code agents.provider}，业务配额使用 {@code agents.quota}。</p>
 */
@ConfigurationProperties(prefix = AgentInvokerProperties.PREFIX)
public class AgentInvokerProperties {

    public static final String PREFIX = "agents.provider";

    /** 是否启用本 Starter */
    private boolean enabled = true;

    /** 默认 Provider（如 "openclaw"） */
    private String defaultProvider = "openclaw";

    /** OpenClaw Provider 配置 */
    private final OpenClaw openclaw = new OpenClaw();

    /** Hermes Provider 配置 */
    private final Hermes hermes = new Hermes();

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public String getDefaultProvider() { return defaultProvider; }
    public void setDefaultProvider(String defaultProvider) { this.defaultProvider = defaultProvider; }

    public OpenClaw getOpenclaw() { return openclaw; }

    public Hermes getHermes() { return hermes; }

    /**
     * OpenClaw Provider 专属配置（{@code agents.provider.openclaw.*}）。
     *
     * <p>Gateway 连接与 Hook 鉴权请配置 {@code openclaw.*}（由 openclaw-spring-boot-starter 装配
     * {@link io.github.easy4j.openclaw.OpenClawClient}）。本段 {@code gatewayBaseUrl} / {@code hooksToken}
     * 等字段保留用于文档与迁移，adapter 运行时以 {@code openclaw.*} 为准；{@code callbackBaseUrl}
     * 可通过 {@link AgentInvokerOpenClawConfigBridge} 从 {@code openclaw.callback-base-url} 桥接。</p>
     */
    public static class OpenClaw {

        /** 是否启用 OpenClaw adapter */
        private boolean enabled = true;

        /**
         * OpenClaw Gateway 根地址（文档/迁移用；实际 {@link io.github.easy4j.openclaw.OpenClawClient} 使用 {@code openclaw.gateway-base-url}）。
         */
        private String gatewayBaseUrl = "http://localhost:18789";

        /**
         * Webhook 鉴权 token（文档/迁移用；实际客户端使用 {@code openclaw.hooks-token}）。
         */
        private String hooksToken;

        /**
         * API 密钥（文档/迁移用；实际客户端使用 {@code openclaw.api-key}）。
         */
        private String apiKey;

        /**
         * 回调基础 URL；未显式设置 {@code agents.provider.openclaw.callback-base-url} 时回退 {@code openclaw.callback-base-url}。
         */
        private String callbackBaseUrl = "http://localhost:7088";

        /** 传输方式：http 或 mq */
        private String transport = "http";

        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }

        public String getGatewayBaseUrl() { return gatewayBaseUrl; }
        public void setGatewayBaseUrl(String gatewayBaseUrl) { this.gatewayBaseUrl = gatewayBaseUrl; }

        public String getHooksToken() { return hooksToken; }
        public void setHooksToken(String hooksToken) { this.hooksToken = hooksToken; }

        public String getApiKey() { return apiKey; }
        public void setApiKey(String apiKey) { this.apiKey = apiKey; }

        public String getCallbackBaseUrl() { return callbackBaseUrl; }
        public void setCallbackBaseUrl(String callbackBaseUrl) { this.callbackBaseUrl = callbackBaseUrl; }

        public String getTransport() { return transport; }
        public void setTransport(String transport) { this.transport = transport; }
    }

    /**
     * Hermes Provider 专属配置（{@code agents.provider.hermes.*}）。
     *
     * <p>Hermes 通过 {@code hermes-java-sdk} 提供 Run-based 异步任务执行。
     * {@link io.github.easy4j.hermes.HermesClient} 由 hermes-spring-boot-starter 装配。</p>
     */
    public static class Hermes {

        /** 是否启用 Hermes adapter */
        private boolean enabled = false;

        /**
         * 回调基础 URL；Hermes 通过轮询驱动回调，此 URL 用于 POST 回调结果。
         */
        private String callbackBaseUrl = "http://localhost:7088";

        /** 默认 instructions（可被 variables["hermes.instructions"] 覆盖） */
        private String defaultInstructions;

        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }

        public String getCallbackBaseUrl() { return callbackBaseUrl; }
        public void setCallbackBaseUrl(String callbackBaseUrl) { this.callbackBaseUrl = callbackBaseUrl; }

        public String getDefaultInstructions() { return defaultInstructions; }
        public void setDefaultInstructions(String defaultInstructions) { this.defaultInstructions = defaultInstructions; }
    }
}
