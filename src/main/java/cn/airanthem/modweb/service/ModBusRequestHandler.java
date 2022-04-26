package cn.airanthem.modweb.service;

import cn.airanthem.modweb.enums.StatusCode;
import cn.airanthem.modweb.exception.NoHandlerException;
import cn.airanthem.modweb.iface.ModWebHandler;
import cn.airanthem.modweb.proto.Request;
import cn.airanthem.modweb.proto.Response;
import com.digitalpetri.modbus.requests.ReadWriteMultipleRegistersRequest;
import com.digitalpetri.modbus.responses.ReadWriteMultipleRegistersResponse;
import com.digitalpetri.modbus.slave.ServiceRequestHandler;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Arrays;

@Service
public class ModBusRequestHandler implements ServiceRequestHandler {

    @Resource
    ModWebHandlerManager manager;

    @Override
    public void onReadWriteMultipleRegisters(ServiceRequest<ReadWriteMultipleRegistersRequest, ReadWriteMultipleRegistersResponse> service) {
        ByteBuf values = service.getRequest().getValues();
        byte[] body = new byte[values.readableBytes()];
        values.readBytes(body);
        if (body[body.length - 1] == 1 && body[body.length - 2] == -1 && body[body.length - 3] == 1) {
            body = Arrays.copyOfRange(body, 0, body.length - 3);
        }
        try {
            Request request = Request.parseFrom(body);
            String name = request.getName();
            byte[] payload = request.getPayload().toByteArray();
            ModWebHandler handler = manager.getHandler(name);
            byte[] result = handler.handle(payload);
            service.sendResponse(response(StatusCode.OK, result));
        } catch (NoHandlerException e) {
            service.sendResponse(response(StatusCode.NO_HANDLER));
        } catch (InvalidProtocolBufferException e) {
            service.sendResponse(response(StatusCode.BAD_REQUEST));
        } catch (Throwable e) {
            service.sendResponse(response(StatusCode.UNKNOWN_ERROR));
        }
    }

    private ReadWriteMultipleRegistersResponse response(StatusCode statusCode, byte[] body) {
        Response response = Response.newBuilder()
                .setStatus(statusCode.getValue())
                .setBody(ByteString.copyFrom(body)).build();
        byte[] result = response.toByteArray();
        ByteBuf buffer = PooledByteBufAllocator.DEFAULT.buffer(result.length);
        buffer.writeBytes(result);
        return new ReadWriteMultipleRegistersResponse(buffer);
    }

    private ReadWriteMultipleRegistersResponse response(StatusCode statusCode) {
        return response(statusCode, new byte[0]);
    }
}
