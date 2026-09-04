package com.isalvama.fresh_keep.shared.infrastructure.ai;

import org.junit.jupiter.api.Test;
import org.springframework.ai.model.ollama.autoconfigure.OllamaApiAutoConfiguration;
import org.springframework.ai.model.ollama.autoconfigure.OllamaChatAutoConfiguration;
import org.springframework.ai.model.tool.autoconfigure.ToolCallingAutoConfiguration;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class OllamaChatModelConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(ToolCallingAutoConfiguration.class, OllamaApiAutoConfiguration.class, OllamaChatAutoConfiguration.class))
            .withPropertyValues("spring.ai.ollama.chat.options.model=llama3.1:latest");

    @Test
    void chatModelBeanIsConfiguredWithThePropertiesModel() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(OllamaChatModel.class);

            OllamaChatModel chatModel = context.getBean(OllamaChatModel.class);

            assertThat(chatModel.getOptions().getModel()).isEqualTo("llama3.1:latest");
        });
    }
}
