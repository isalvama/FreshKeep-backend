package com.isalvama.fresh_keep.shared.infrastructure.ai;

import org.junit.jupiter.api.Test;
import org.springframework.ai.google.genai.GoogleGenAiChatModel;
import org.springframework.ai.model.google.genai.autoconfigure.chat.GoogleGenAiChatAutoConfiguration;
import org.springframework.ai.model.tool.autoconfigure.ToolCallingAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class GoogleGenAiChatModelConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(ToolCallingAutoConfiguration.class, GoogleGenAiChatAutoConfiguration.class))
            .withPropertyValues(
                    "spring.ai.google.genai.api-key=test-key",
                    "spring.ai.google.genai.chat.options.model=gemini-3.7-flash",
                    "spring.ai.google.genai.chat.options.temperature=0.2"
            );

    @Test
    void chatModelBeanIsConfiguredWithThePropertiesModelAndTemperature() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(GoogleGenAiChatModel.class);

            GoogleGenAiChatModel chatModel = context.getBean(GoogleGenAiChatModel.class);

            assertThat(chatModel.getOptions().getModel()).isEqualTo("gemini-3.7-flash");
            assertThat(chatModel.getOptions().getTemperature()).isEqualTo(0.2);
        });
    }
}
