package cn.airanthem.modweb.config;

import cn.airanthem.modweb.service.ModBusRequestHandler;
import com.digitalpetri.modbus.slave.ModbusTcpSlave;
import com.digitalpetri.modbus.slave.ModbusTcpSlaveConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;

@Configuration
@ComponentScan("cn.airanthem.modweb")
@EnableConfigurationProperties(ApplicationConfig.class)
public class ModWebAutoConfig {
    private static final Logger LOG = LoggerFactory.getLogger(ModWebAutoConfig.class);

    @Resource
    ModBusRequestHandler mainHandler;

    @Resource
    ApplicationConfig applicationConfig;

    @Bean
    public ModbusTcpSlave modbusTcpSlave() {
        ModbusTcpSlaveConfig config = new ModbusTcpSlaveConfig.Builder().build();
        ModbusTcpSlave modbusTcpSlave = new ModbusTcpSlave(config);
        modbusTcpSlave.setRequestHandler(mainHandler);
        modbusTcpSlave.bind("0.0.0.0", applicationConfig.getPort());
        LOG.info("ModWeb started, listening at 0.0.0.0:{}", applicationConfig.getPort());
        return modbusTcpSlave;
    }
}
