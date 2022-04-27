package cn.airanthem.modweb.listener;

import cn.airanthem.modweb.client.ModWebClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.annotation.Resource;

@Component
public class StopApplicationListener implements ApplicationListener<ContextClosedEvent> {

    private static final Logger LOG = LoggerFactory.getLogger(StopApplicationListener.class);

    @Resource
    ModWebClient modWebClient;

    @Override
    public void onApplicationEvent(@Nonnull ContextClosedEvent event) {
        modWebClient.close();
    }
}
