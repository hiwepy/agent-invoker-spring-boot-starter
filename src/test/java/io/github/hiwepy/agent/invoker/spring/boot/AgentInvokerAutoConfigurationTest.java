package io.github.hiwepy.agent.invoker.spring.boot;

import io.github.hiwepy.agent.invoker.AgentInvokerRouter;
import io.github.hiwepy.agent.invoker.CallbackRouter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AgentInvokerAutoConfigurationTest {

    @Test
    void shouldCreateRouter() {
        AgentInvokerRouter router = new AgentInvokerRouter();
        assertNotNull(router);
        assertTrue(router.getInvokers().isEmpty());
    }

    @Test
    void shouldCreateCallbackRouter() {
        AgentInvokerRouter invokerRouter = new AgentInvokerRouter();
        CallbackRouter callbackRouter = new CallbackRouter(invokerRouter);
        assertNotNull(callbackRouter);
    }

    @Test
    void shouldBindProperties() {
        AgentInvokerProperties props = new AgentInvokerProperties();
        assertTrue(props.isEnabled());
        assertEquals("openclaw", props.getDefaultProvider());
        assertTrue(props.getOpenclaw().isEnabled());
        assertEquals("http://localhost:18789", props.getOpenclaw().getGatewayBaseUrl());
        assertEquals("http://localhost:7088", props.getOpenclaw().getCallbackBaseUrl());
    }
}
