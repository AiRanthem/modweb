package cn.airanthem.modweb.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "modweb")
@Component
@Getter
@Setter
public class ModWebConfig {
    /**
     * Modbus 监听端口
     */
    private Integer port = 50052;

    /**
     * 请求超时时间
     */
    private Integer timeout = Integer.MAX_VALUE;
}
