package cn.airanthem.modweb.client;

import com.digitalpetri.modbus.requests.ReadInputRegistersRequest;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class ReadInputRegistersRequestPrototype implements ModbusRequestPrototype<ReadInputRegistersRequest> {
    private int address;
    private int quantity;

    @Override
    public ReadInputRegistersRequest get() {
        return new ReadInputRegistersRequest(address, quantity);
    }
}
