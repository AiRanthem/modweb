package cn.airanthem.modweb.service;

import cn.airanthem.modweb.exception.NoHandlerException;
import cn.airanthem.modweb.iface.ModWebHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class ModWebHandlerManager {

    private static final Logger LOG = LoggerFactory.getLogger(ModWebHandlerManager.class);

    private final Map<String, ModWebHandler> handlerMap = new HashMap<>();

    public void register(String name, ModWebHandler handler) {
        if (!handlerMap.containsKey(name)) {
            handlerMap.put(name, handler);
        } else {
            LOG.error("multiple handlers named {}, register failed", name);
            throw new RuntimeException("duplicated handler name");
        }
    }

    public ModWebHandler getHandler(String name) throws NoHandlerException {
        ModWebHandler handler = handlerMap.getOrDefault(name, null);
        if (handler == null) {
            throw new NoHandlerException(name);
        }
        return handler;
    }
}
