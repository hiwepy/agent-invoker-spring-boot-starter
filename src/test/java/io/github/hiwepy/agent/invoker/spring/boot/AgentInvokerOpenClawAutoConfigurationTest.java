package io.github.hiwepy.agent.invoker.spring.boot;

import io.github.hiwepy.agent.invoker.AgentInvokerRouter;
import io.github.hiwepy.agent.invoker.openclaw.OpenClawAgentInvoker;
import io.github.hiwepy.openclaw.OpenClawClient;
import io.github.hiwepy.openclaw.OpenClawClientConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 验证 agent-invoker 自动配置在 OpenClawClient 可用时将 adapter 注册到 Router。
 */
class AgentInvokerOpenClawAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(AgentInvokerAutoConfiguration.class))
            .withUserConfiguration(TestOpenClawBeans.class)
            .withPropertyValues(
                    "openclaw.callback-base-url=http://callback.from-openclaw:8080",
                    "agents.provider.default-provider=openclaw");

    @Test
    void shouldRegisterOpenClawInvokerInRouter() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(OpenClawAgentInvoker.class);
            AgentInvokerRouter router = context.getBean(AgentInvokerRouter.class);
            OpenClawAgentInvoker invoker = context.getBean(OpenClawAgentInvoker.class);
            assertThat(router.getInvokers()).hasSize(1);
            assertThat(router.route("openclaw")).isSameAs(invoker);
        });
    }

    @Test
    void shouldRouteNullProviderToConfiguredDefault() {
        contextRunner.run(context -> {
            AgentInvokerRouter router = context.getBean(AgentInvokerRouter.class);
            OpenClawAgentInvoker invoker = context.getBean(OpenClawAgentInvoker.class);
            assertThat(router.getDefaultProvider()).isEqualTo("openclaw");
            assertThat(router.route((String) null)).isSameAs(invoker);
        });
    }

    @Test
    void shouldBridgeCallbackBaseUrlFromOpenClawProperties() {
        contextRunner.run(context -> {
            OpenClawAgentInvoker invoker = context.getBean(OpenClawAgentInvoker.class);
            assertThat(invoker.getCallbackBaseUrl()).isEqualTo("http://callback.from-openclaw:8080");
        });
    }

    /**
     * 测试用 OpenClawClient Bean，模拟 openclaw-spring-boot-starter 提供的客户端。
     */
    @Configuration
    static class TestOpenClawBeans {

        @Bean
        OpenClawClient openClawClient() {
            OpenClawClientConfig config = new OpenClawClientConfig();
            config.setGatewayBaseUrl("http://gw.test:18789");
            config.setHooksToken("hook-secret");
            return new OpenClawClient(config);
        }
    }
}
