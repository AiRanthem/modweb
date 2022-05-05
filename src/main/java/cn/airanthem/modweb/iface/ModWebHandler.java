package cn.airanthem.modweb.iface;

import cn.airanthem.modweb.exception.ServiceRuntimeException;

public interface ModWebHandler {
    byte[] handle(byte[] payload) throws ServiceRuntimeException;
}
