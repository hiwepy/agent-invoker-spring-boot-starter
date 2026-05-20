package io.github.hiwepy.agent.invoker.spring.boot;

import io.github.hiwepy.agent.invoker.AiAgentInvokerRouter;
import io.github.hiwepy.agent.invoker.CallbackRouter;
import io.github.hiwepy.agent.invoker.openclaw.OpenClawAiAgentInvoker;
import io.github.hiwepy.openclaw.OpenClawClient;
import io.github.hiwepy.openclaw.spring.boot.OpenClawAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Agent Invoker 自动配置。
 *
 * <p>注册 {@link AiAgentInvokerRouter}、{@link CallbackRouter}，
 * 并在 OpenClaw 可用时条件装配 {@link OpenClawAiAgentInvoker}。</p>
 */
@Configuration
@ConditionalOnClass(AiAgentInvokerRouter.class)
@EnableConfigurationProperties(AgentInvokerProperties.class)
@ConditionalOnProperty(prefix = AgentInvokerProperties.PREFIX, name = "enabled", havingValue = "true", matchIfMissing = true)
public class AgentInvokerAutoConfiguration {

    /**
     * AI Agent 调用路由器 Bean。收集所有 AiAgentInvoker 实现。
     */
    @Bean
    @ConditionalOnMissingBean
    public AiAgentInvokerRouter aiAgentInvokerRouter() {
        return new AiAgentInvokerRouter();
    }

    /**
     * 回调路由器 Bean。
     */
    @Bean
    @ConditionalOnMissingBean
    public CallbackRouter callbackRouter(AiAgentInvokerRouter invokerRouter) {
        return new CallbackRouter(invokerRouter);
    }

    /**
     * OpenClaw adapter — 仅在 OpenClawClient 可用时装配。
     */
    @Configuration
    @ConditionalOnClass(OpenClawClient.class)
    @AutoConfigureAfter(OpenClawAutoConfiguration.class)
    @ConditionalOnProperty(prefix = AgentInvokerProperties.PREFIX + ".openclaw", name = "enabled", havingValue = "true", matchIfMissing = true)
    static class OpenClawInvokerConfiguration {

        @Bean
        @ConditionalOnMissingBean
        @ConditionalOnBean(OpenClawClient.class)
        public OpenClawAiAgentInvoker openClawAiAgentInvoker(
                OpenClawClient openClawClient,
                AgentInvokerProperties properties) {
            OpenClawAiAgentInvoker invoker = new OpenClawAiAgentInvoker(
                    openClawClient,
                    properties.getOpenclaw().getCallbackBaseUrl());
            return invoker;
        }

        /**
         * 将 OpenClaw adapter 注册到 Router。
         */
        @Bean
        @ConditionalOnBean({OpenClawAiAgentInvoker.class, AiAgentInvokerRouter.class})
        public Object registerOpenClawInvoker(
                OpenClawAiAgentInvoker openClawInvoker,
                AiAgentInvokerRouter router) {
            router.register(openClawInvoker);
            return "openclawInvokerRegistered";
        }
    }
}
