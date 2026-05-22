package io.github.hiwepy.agent.invoker.spring.boot;

import org.springframework.core.env.Environment;

/**
 * 解析 agent-invoker 与 {@code openclaw-spring-boot-starter} 之间的配置桥接。
 *
 * <p>Gateway 连接与 Hook 鉴权以 {@code openclaw.*} 为准（由 openclaw-spring-boot-starter 装配
 * {@link io.github.hiwepy.openclaw.OpenClawClient}）。{@code agents.provider.openclaw.*} 仅覆盖
 * invoker adapter 行为；未显式设置时回退到 {@code openclaw.callback-base-url}。</p>
 */
final class AgentInvokerOpenClawConfigBridge {

    private static final String AGENT_CALLBACK_KEY = "agents.provider.openclaw.callback-base-url";
    private static final String OPENCLAW_CALLBACK_KEY = "openclaw.callback-base-url";

    private AgentInvokerOpenClawConfigBridge() {
    }

    /**
     * 解析 OpenClaw adapter 使用的 callback 基础 URL。
     *
     * <p>优先级：显式 {@code agents.provider.openclaw.callback-base-url}
     * &gt; {@code openclaw.callback-base-url} &gt; {@link AgentInvokerProperties.OpenClaw} 默认值。</p>
     */
    static String resolveCallbackBaseUrl(Environment environment, AgentInvokerProperties properties) {
        if (environment != null && environment.containsProperty(AGENT_CALLBACK_KEY)) {
            String fromEnv = environment.getProperty(AGENT_CALLBACK_KEY);
            if (fromEnv != null && !fromEnv.isEmpty()) {
                return fromEnv;
            }
        }
        if (environment != null) {
            String fromOpenClaw = environment.getProperty(OPENCLAW_CALLBACK_KEY);
            if (fromOpenClaw != null && !fromOpenClaw.isEmpty()) {
                return fromOpenClaw;
            }
        }
        String fromProperties = properties.getOpenclaw().getCallbackBaseUrl();
        return fromProperties != null ? fromProperties : "http://localhost:7088";
    }
}
