package cn.airanthem.modweb.service;

import cn.airanthem.modweb.annotation.ModBusService;
import cn.airanthem.modweb.iface.ModWebHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Map;

@Service
public class ModWebRegisterService implements ApplicationListener<ContextRefreshedEvent> {

    private static final Logger LOG = LoggerFactory.getLogger(ModWebRegisterService.class);

    @Resource
    ModWebHandlerManager manager;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        ApplicationContext applicationContext = event.getApplicationContext();
        Map<String, Object> beansWithAnnotation = applicationContext.getBeansWithAnnotation(ModBusService.class);
        for (Map.Entry<String, Object> entry : beansWithAnnotation.entrySet()) {
            Object handlerToRegister = entry.getValue();
            Class<?> handlerToRegisterClass = handlerToRegister.getClass();
            ModBusService annotation = handlerToRegisterClass.getAnnotation(ModBusService.class);
            String name = annotation.name();
            if (ModWebHandler.class.isAssignableFrom(handlerToRegisterClass)) {
                manager.register(name, (ModWebHandler) handlerToRegister);
            }else {
                LOG.warn("handler named {} [{}] doesn't implement ModWebHandler, skipped", name, entry.getKey());
            }
        }
    }
}
