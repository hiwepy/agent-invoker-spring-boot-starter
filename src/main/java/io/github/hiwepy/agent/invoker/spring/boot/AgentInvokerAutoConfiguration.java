package io.github.hiwepy.agent.invoker.spring.boot;

import io.github.hiwepy.agent.invoker.AgentInvokerRouter;
import io.github.hiwepy.agent.invoker.CallbackRouter;
import io.github.hiwepy.agent.invoker.hermes.HermesAgentInvoker;
import io.github.hiwepy.agent.invoker.openclaw.OpenClawAgentInvoker;
import io.github.hiwepy.hermes.HermesClient;
import io.github.hiwepy.openclaw.OpenClawClient;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;

/**
 * Agent Invoker 自动配置。
 *
 * <p>注册 {@link AgentInvokerRouter}、{@link CallbackRouter}，
 * 并在 OpenClaw 可用时条件装配 {@link OpenClawAgentInvoker}。</p>
 *
 * <p>配置说明：{@code openclaw.*}（openclaw-spring-boot-starter）负责 Gateway 客户端；
 * {@code agents.provider.*} 负责 invoker 路由与 adapter 行为。callback 基础 URL 可通过
 * {@link AgentInvokerOpenClawConfigBridge} 从 {@code openclaw.callback-base-url} 桥接。</p>
 */
@Configuration
@ConditionalOnClass(AgentInvokerRouter.class)
@EnableConfigurationProperties(AgentInvokerProperties.class)
@ConditionalOnProperty(prefix = AgentInvokerProperties.PREFIX, name = "enabled", havingValue = "true", matchIfMissing = true)
@Import({AgentInvokerAutoConfiguration.OpenClawInvokerConfiguration.class,
         AgentInvokerAutoConfiguration.HermesInvokerConfiguration.class})
public class AgentInvokerAutoConfiguration {

    /**
     * AI Agent 调用路由器 Bean。收集所有 AiAgentInvoker 实现，并应用 {@code agents.provider.default-provider}。
     */
    @Bean
    @ConditionalOnMissingBean
    public AgentInvokerRouter aiAgentInvokerRouter(AgentInvokerProperties properties) {
        AgentInvokerRouter router = new AgentInvokerRouter();
        router.setDefaultProvider(properties.getDefaultProvider());
        return router;
    }

    /**
     * 回调路由器 Bean。
     */
    @Bean
    @ConditionalOnMissingBean
    public CallbackRouter callbackRouter(AgentInvokerRouter invokerRouter) {
        return new CallbackRouter(invokerRouter);
    }

    /**
     * OpenClaw adapter — 仅在 OpenClawClient 可用时装配。
     */
    @Configuration
    @ConditionalOnClass(OpenClawClient.class)
    @AutoConfigureAfter(name = "com.github.hiwepy.openclaw.spring.boot.OpenClawAutoConfiguration")
    @ConditionalOnProperty(prefix = AgentInvokerProperties.PREFIX + ".openclaw", name = "enabled", havingValue = "true", matchIfMissing = true)
    static class OpenClawInvokerConfiguration {

        /**
         * 创建 OpenClaw adapter 并注册到 Router；callback 基础 URL 支持从 {@code openclaw.callback-base-url} 桥接。
         */
        @Bean
        @ConditionalOnMissingBean
        @ConditionalOnBean(OpenClawClient.class)
        public OpenClawAgentInvoker openClawAiAgentInvoker(
                OpenClawClient openClawClient,
                AgentInvokerProperties properties,
                Environment environment,
                AgentInvokerRouter router) {
            String callbackBaseUrl = AgentInvokerOpenClawConfigBridge.resolveCallbackBaseUrl(
                    environment, properties);
            OpenClawAgentInvoker invoker = new OpenClawAgentInvoker(openClawClient, callbackBaseUrl);
            router.register(invoker);
            return invoker;
        }
    }

    /**
     * Hermes adapter — 仅在 HermesClient 可用时装配。
     */
    @Configuration
    @ConditionalOnClass(HermesClient.class)
    @ConditionalOnBean(HermesClient.class)
    @AutoConfigureAfter(name = "io.github.hiwepy.hermes.spring.boot.HermesAutoConfiguration")
    @ConditionalOnProperty(prefix = AgentInvokerProperties.PREFIX + ".hermes", name = "enabled", havingValue = "true")
    static class HermesInvokerConfiguration {

        @Bean
        @ConditionalOnMissingBean
        public HermesAgentInvoker hermesAgentInvoker(
                HermesClient hermesClient,
                AgentInvokerProperties properties,
                AgentInvokerRouter router) {
            AgentInvokerProperties.Hermes hermesProps = properties.getHermes();
            HermesAgentInvoker invoker = new HermesAgentInvoker(
                    hermesClient,
                    hermesProps.getCallbackBaseUrl());
            router.register(invoker);
            return invoker;
        }
    }
}
