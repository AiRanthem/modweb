package cn.airanthem.modweb.client;

import com.digitalpetri.modbus.requests.ReadWriteMultipleRegistersRequest;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class ReadWriteMultipleRegistersRequestPrototype implements ModbusRequestPrototype<ReadWriteMultipleRegistersRequest> {

    private final int readAddress;
    private final int readQuantity;
    private final int writeAddress;
    private final int writeQuantity;
    private final byte[] data;

    @Override
    public ReadWriteMultipleRegistersRequest get() {
        ByteBuf buffer = PooledByteBufAllocator.DEFAULT.buffer(data.length);
        buffer.writeBytes(data);
        return new ReadWriteMultipleRegistersRequest(readAddress, readQuantity, writeAddress, writeQuantity, buffer);
    }
}
