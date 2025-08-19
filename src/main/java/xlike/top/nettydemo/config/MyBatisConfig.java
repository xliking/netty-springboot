package xlike.top.nettydemo.config;

import com.baomidou.mybatisplus.autoconfigure.ConfigurationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import xlike.top.nettydemo.enums.MessageType;
import xlike.top.nettydemo.handler.MessageTypeHandler;

@Configuration
public class MyBatisConfig {
    
    @Bean
    public ConfigurationCustomizer configurationCustomizer() {
        return configuration -> {
            configuration.getTypeHandlerRegistry().register(MessageType.class, MessageTypeHandler.class);
        };
    }
}
