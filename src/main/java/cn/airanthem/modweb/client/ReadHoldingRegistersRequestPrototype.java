package cn.airanthem.modweb.client;

import com.digitalpetri.modbus.requests.ReadHoldingRegistersRequest;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class ReadHoldingRegistersRequestPrototype implements ModbusRequestPrototype<ReadHoldingRegistersRequest> {
    private int address;
    private int quantity;

    @Override
    public ReadHoldingRegistersRequest get() {
        return new ReadHoldingRegistersRequest(address, quantity);
    }
}
