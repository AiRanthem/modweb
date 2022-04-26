package cn.airanthem.modweb.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "modweb")
@Component
@Getter
@Setter
public class ApplicationConfig {
    /**
     * Modbus 监听端口
     */
    private Integer port;
}
