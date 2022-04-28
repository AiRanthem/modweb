package cn.airanthem.modweb.client;

import com.digitalpetri.modbus.requests.WriteMultipleRegistersRequest;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class WriteMultipleRegistersRequestPrototype implements ModbusRequestPrototype<WriteMultipleRegistersRequest> {
    private final int address;
    private final int quantity;
    private final byte[] data;

    @Override
    public WriteMultipleRegistersRequest get() {
        ByteBuf buffer = PooledByteBufAllocator.DEFAULT.buffer(data.length);
        buffer.writeBytes(data);
        return new WriteMultipleRegistersRequest(address, quantity, buffer);
    }
}
