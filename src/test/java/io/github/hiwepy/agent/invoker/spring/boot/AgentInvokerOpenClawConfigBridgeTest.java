package io.github.hiwepy.agent.invoker.spring.boot;

import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AgentInvokerOpenClawConfigBridgeTest {

    @Test
    void shouldPreferExplicitAgentInvokerCallbackUrl() {
        MockEnvironment env = new MockEnvironment();
        env.setProperty("agents.provider.openclaw.callback-base-url", "http://from-agent-invoker");

        AgentInvokerProperties props = new AgentInvokerProperties();

        assertEquals("http://from-agent-invoker",
                AgentInvokerOpenClawConfigBridge.resolveCallbackBaseUrl(env, props));
    }

    @Test
    void shouldBridgeFromOpenClawWhenAgentInvokerKeyUnset() {
        MockEnvironment env = new MockEnvironment();
        env.setProperty("openclaw.callback-base-url", "http://from-openclaw");

        AgentInvokerProperties props = new AgentInvokerProperties();

        assertEquals("http://from-openclaw",
                AgentInvokerOpenClawConfigBridge.resolveCallbackBaseUrl(env, props));
    }
}
