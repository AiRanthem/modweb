package cn.airanthem.modweb.service;

import cn.airanthem.modweb.enums.StatusCode;
import cn.airanthem.modweb.exception.NoHandlerException;
import cn.airanthem.modweb.exception.ServiceRuntimeException;
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
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Arrays;

@Service
public class ModBusRequestHandler implements ServiceRequestHandler {

    private static final Logger LOG = LoggerFactory.getLogger(ModBusRequestHandler.class);

    @Resource
    ModWebHandlerManager manager;

    @Resource
    PayloadCacheService payloadCache;

    @Resource
    RequestContext context;

    @Override
    public void onReadWriteMultipleRegisters(ServiceRequest<ReadWriteMultipleRegistersRequest, ReadWriteMultipleRegistersResponse> service) {
        ReadWriteMultipleRegistersRequest modbusRequest = service.getRequest();
        String remoteAddress = service.getChannel().remoteAddress().toString();
        ByteBuf values = modbusRequest.getValues();
        byte[] body = new byte[values.readableBytes()];
        values.readBytes(body);
        if (body.length > 3 && body[body.length - 1] == 1 && body[body.length - 2] == -1 && body[body.length - 3] == 1) {
            body = Arrays.copyOfRange(body, 0, body.length - 3);
        }
        try {
            Request request = Request.parseFrom(body);
            String name = request.getName();
            byte[] payload = request.getPayload().toByteArray();
            payloadCache.push(remoteAddress, payload);
            int part = request.getPart();
            if (part == 0) {
                // context set here
                context.setAddress(remoteAddress);
                ModWebHandler handler = manager.getHandler(name);
                byte[] result = handler.handle(payloadCache.get(remoteAddress));
                service.sendResponse(response(StatusCode.OK.getValue(), result));
            } else if (part > 0){
                service.sendResponse(response(StatusCode.ROGER_THAT.getValue()));
            } else {
                payloadCache.drop(remoteAddress);
                service.sendResponse(response(StatusCode.OK.getValue()));
            }
        } catch (NoHandlerException e) {
            LOG.error("an unregistered handler name requested {}", e.getName());
            service.sendResponse(response(StatusCode.NO_HANDLER.getValue()));
        } catch (InvalidProtocolBufferException e) {
            LOG.error("request body is not a supported proto");
            service.sendResponse(response(StatusCode.BAD_REQUEST.getValue()));
        } catch (ServiceRuntimeException e) {
            LOG.error("handler throws a controlled exception with code {}", e.getCode(), e.getCause());
            service.sendResponse(response(e.getCode()));
        } catch (Throwable e) {
            LOG.error("unknown error", e);
            service.sendResponse(response(StatusCode.UNKNOWN_ERROR.getValue()));
        }
        ReferenceCountUtil.release(modbusRequest);
    }

    private ReadWriteMultipleRegistersResponse response(Integer statusCode, byte[] body) {
        Response response = Response.newBuilder()
                .setStatus(statusCode)
                .setBody(ByteString.copyFrom(body)).build();
        byte[] result = response.toByteArray();
        ByteBuf buffer = PooledByteBufAllocator.DEFAULT.buffer(result.length);
        buffer.writeBytes(result);
        return new ReadWriteMultipleRegistersResponse(buffer);
    }

    private ReadWriteMultipleRegistersResponse response(Integer statusCode) {
        return response(statusCode, new byte[0]);
    }
}
