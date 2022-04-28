package cn.airanthem.modweb.client;

import com.digitalpetri.modbus.requests.ModbusRequest;

public interface ModbusRequestPrototype<T extends ModbusRequest> {
    T get();
}
