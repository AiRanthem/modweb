package cn.airanthem.modweb.listener;

import cn.airanthem.modweb.annotation.ModWebService;
import cn.airanthem.modweb.iface.ModWebHandler;
import cn.airanthem.modweb.service.ModWebHandlerManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Map;

@Service
public class ModWebRegisterListener implements ApplicationListener<ContextRefreshedEvent> {

    private static final Logger LOG = LoggerFactory.getLogger(ModWebRegisterListener.class);

    @Resource
    ModWebHandlerManager manager;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        ApplicationContext applicationContext = event.getApplicationContext();
        Map<String, Object> beansWithAnnotation = applicationContext.getBeansWithAnnotation(ModWebService.class);
        for (Map.Entry<String, Object> entry : beansWithAnnotation.entrySet()) {
            Object handlerToRegister = entry.getValue();
            Class<?> handlerToRegisterClass = handlerToRegister.getClass();
            ModWebService annotation = handlerToRegisterClass.getAnnotation(ModWebService.class);
            String name = annotation.name();
            if (ModWebHandler.class.isAssignableFrom(handlerToRegisterClass)) {
                manager.register(name, (ModWebHandler) handlerToRegister);
                LOG.info("modbus service handler named {} registered", name);
            }else {
                LOG.warn("modbus service handler named {} [{}] doesn't implement ModWebHandler, skipped", name, entry.getKey());
            }
        }
    }
}
