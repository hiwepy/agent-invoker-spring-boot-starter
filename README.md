# agent-invoker-spring-boot-starter

Spring Boot 自动配置模块，为 [agent-invoker-java-sdk](../agent-invoker-java-sdk) 提供自动装配，实现多 Provider AI 智能体调用路由。

## 支持的 Provider

| Provider | providerCode | 条件装配 | 依赖 |
|----------|-------------|----------|------|
| OpenClaw | `openclaw` | `OpenClawClient` Bean 存在 + `agents.provider.openclaw.enabled=true`（默认开启） | `openclaw-spring-boot-starter` |
| Hermes | `hermes` | `HermesClient` Bean 存在 + `agents.provider.hermes.enabled=true`（默认关闭） | `hermes-spring-boot-starter` |
| OpenCode | `opencode` | 通过 `agent-invoker-java-sdk` 内置的 `OpenCodeAgentInvoker` | `opencode-spring-boot-starter` |

## Maven 坐标

```xml
<dependency>
    <groupId>io.github.hiwepy</groupId>
    <artifactId>agent-invoker-spring-boot-starter</artifactId>
    <version>3.3.x.20260520-SNAPSHOT</version>
</dependency>
```

## 配置属性

配置前缀：`agents.provider`

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `agents.provider.enabled` | `boolean` | `true` | 启用/禁用本 Starter |
| `agents.provider.default-provider` | `String` | `openclaw` | 默认 Provider（当 `providerCode` 为空时回退） |
| **OpenClaw** | | | |
| `agents.provider.openclaw.enabled` | `boolean` | `true` | 启用 OpenClaw adapter |
| `agents.provider.openclaw.gateway-base-url` | `String` | `http://localhost:18789` | Gateway 根地址（文档/迁移用，运行时以 `openclaw.gateway-base-url` 为准） |
| `agents.provider.openclaw.hooks-token` | `String` | `null` | Webhook 鉴权 token（文档/迁移用） |
| `agents.provider.openclaw.callback-base-url` | `String` | `http://localhost:7088` | 回调基础 URL；未设置时回退 `openclaw.callback-base-url` |
| `agents.provider.openclaw.transport` | `String` | `http` | 传输方式 |
| **Hermes** | | | |
| `agents.provider.hermes.enabled` | `boolean` | `false` | 启用 Hermes adapter |
| `agents.provider.hermes.callback-base-url` | `String` | `http://localhost:7088` | 回调基础 URL |
| `agents.provider.hermes.default-instructions` | `String` | `null` | 默认 instructions（可被 `variables["hermes.instructions"]` 覆盖） |

## 快速使用

### 1. 添加依赖

```xml
<!-- OpenClaw Provider（默认） -->
<dependency>
    <groupId>io.github.hiwepy</groupId>
    <artifactId>openclaw-spring-boot-starter</artifactId>
    <version>2.7.x.20260527-SNAPSHOT</version>
</dependency>
<dependency>
    <groupId>io.github.hiwepy</groupId>
    <artifactId>agent-invoker-spring-boot-starter</artifactId>
    <version>3.3.x.20260520-SNAPSHOT</version>
</dependency>
```

### 2. 配置

```yaml
openclaw:
  gateway-base-url: http://localhost:18789
  hooks-token: your-hooks-token
  callback-base-url: http://your-app:7088

agents:
  provider:
    default-provider: openclaw
    openclaw:
      enabled: true
      callback-base-url: http://your-app:7088
```

### 3. 注入使用

```java
@Service
@RequiredArgsConstructor
public class AgentService {

    private final AgentInvokerRouter router;

    public String submitTask(String taskId, String prompt) {
        AgentInvokeCmd cmd = AgentInvokeCmd.builder()
                .taskId(taskId)
                .providerCode("openclaw")
                .enhancedPrompt(prompt)
                .build();

        AgentInvoker invoker = router.route(cmd);
        SubmitResult result = invoker.submit(cmd);
        return result.getProviderTaskId();
    }
}
```

## 自动注册的 Bean

| Bean | 类型 | 条件 |
|------|------|------|
| `aiAgentInvokerRouter` | `AgentInvokerRouter` | `@ConditionalOnMissingBean` |
| `callbackRouter` | `CallbackRouter` | `@ConditionalOnMissingBean` |
| `openClawAiAgentInvoker` | `OpenClawAgentInvoker` | `OpenClawClient` Bean 存在 + `agents.provider.openclaw.enabled=true` |
| `hermesAgentInvoker` | `HermesAgentInvoker` | `HermesClient` Bean 存在 + `agents.provider.hermes.enabled=true` |

## 与 agent-invoker-java-sdk 的关系

本 Starter 仅负责 Spring Boot 自动装配。核心抽象（`AgentInvoker`、`AgentInvokerRouter`、`CallbackRouter`）和各 Provider adapter（`OpenClawAgentInvoker`、`HermesAgentInvoker`、`OpenCodeAgentInvoker`）均在 [agent-invoker-java-sdk](../agent-invoker-java-sdk) 中实现。
