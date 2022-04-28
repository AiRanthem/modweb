package cn.airanthem.modweb.client;

import cn.airanthem.modweb.config.ModWebConfig;
import cn.airanthem.modweb.enums.StatusCode;
import cn.airanthem.modweb.exception.ModbusRequestException;
import cn.airanthem.modweb.exception.ServiceRequestException;
import cn.airanthem.modweb.proto.Request;
import cn.airanthem.modweb.proto.Response;
import com.digitalpetri.modbus.master.ModbusTcpMaster;
import com.digitalpetri.modbus.master.ModbusTcpMasterConfig;
import com.digitalpetri.modbus.requests.ModbusRequest;
import com.digitalpetri.modbus.responses.ModbusResponse;
import com.digitalpetri.modbus.responses.ReadHoldingRegistersResponse;
import com.digitalpetri.modbus.responses.ReadInputRegistersResponse;
import com.digitalpetri.modbus.responses.ReadWriteMultipleRegistersResponse;
import com.digitalpetri.modbus.responses.WriteMultipleRegistersResponse;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import io.netty.buffer.ByteBuf;
import io.netty.util.ReferenceCountUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class ModWebClient {
    private static final Logger LOG = LoggerFactory.getLogger(ModWebClient.class);

    private final Map<Integer, ModbusTcpMaster> slaves = new ConcurrentHashMap<>();

    @Resource
    ModWebConfig modWebConfig;

    @AllArgsConstructor
    @Getter
    public static class Result {
        Integer status;
        byte[] body;
    }

    public static class RequestExecutor {
        private final Map<Integer, ModbusTcpMaster> masters;

        public RequestExecutor(Map<Integer, ModbusTcpMaster> masters) {
            this.masters = masters;
        }

        public Map<Integer, Result> requestService(String name, byte[] payload) throws ServiceRequestException {
            Request requestBody = Request.newBuilder().setName(name).setPayload(ByteString.copyFrom(payload)).build();
            byte[] bodyBytes = requestBody.toByteArray();
            // use simple 1 -1 1 encode to ensure buffer size is even
            if (bodyBytes.length >= 3 && bodyBytes.length % 2 != 0) {
                int x = bodyBytes.length;
                bodyBytes = Arrays.copyOf(bodyBytes, x + 3);
                bodyBytes[x] = 1;
                bodyBytes[x + 1] = -1;
                bodyBytes[x + 2] = 1;
            }
            ReadWriteMultipleRegistersRequestPrototype prototype = new ReadWriteMultipleRegistersRequestPrototype(0, 0, 0, bodyBytes.length / 2, bodyBytes);
            try {
                return requestModbus(prototype, 0, (Function<ReadWriteMultipleRegistersResponse, Result>) response -> {
                    ByteBuf buf = response.getRegisters();
                    byte[] bytes = new byte[buf.readableBytes()];
                    buf.readBytes(bytes);
                    try {
                        Response proto = Response.parseFrom(bytes);
                        return new Result(proto.getStatus(), proto.getBody().toByteArray());
                    } catch (InvalidProtocolBufferException e) {
                        LOG.error("service handler {} provides a bad response body", name);
                        return new Result(StatusCode.BAD_RESPONSE.getValue(), new byte[0]);
                    }
                });
            } catch (Exception e) {
                LOG.error("request service [{}] error", name, e);
                throw new ServiceRequestException(name, e);
            }
        }

        public Map<Integer, Result> readInputRegister(int address, int quantity, int unitId) {
            ReadInputRegistersRequestPrototype request = new ReadInputRegistersRequestPrototype(address, quantity);
            try {
                return requestModbus(request, unitId, (Function<ReadInputRegistersResponse, Result>) response -> {
                    ByteBuf buf = response.getRegisters();
                    byte[] bytes = new byte[buf.readableBytes()];
                    buf.readBytes(bytes);
                    return new Result(0, bytes);
                });
            } catch (Exception e) {
                throw new ModbusRequestException(e);
            }
        }

        public Map<Integer, Result> readHoldingRegisters(int address, int quantity, int unitId) throws ModbusRequestException {
            ReadHoldingRegistersRequestPrototype request = new ReadHoldingRegistersRequestPrototype(address, quantity);
            try {
                return requestModbus(request, unitId, (Function<ReadHoldingRegistersResponse, Result>) response -> {
                    ByteBuf buf = response.getRegisters();
                    byte[] bytes = new byte[buf.readableBytes()];
                    buf.readBytes(bytes);
                    return new Result(0, bytes);
                });
            } catch (Exception e) {
                throw new ModbusRequestException(e);
            }
        }

        public Map<Integer, Result> writeMultipleRegisters(int address, byte[] bytes, int unitId) throws ModbusRequestException {
            WriteMultipleRegistersRequestPrototype request = new WriteMultipleRegistersRequestPrototype(address, bytes.length / 2, bytes);
            try {
                return requestModbus(request, unitId, (Function<WriteMultipleRegistersResponse, Result>) response -> new Result(0, new byte[0]));
            } catch (Exception e) {
                throw new ModbusRequestException(e);
            }
        }

        /**
         * @param callBack a function transforms abstract ModbusResponse to a Result that you need
         */
        @SuppressWarnings("unchecked")
        private <Q extends ModbusRequest, A extends ModbusResponse> Map<Integer, Result> requestModbus(ModbusRequestPrototype<Q> prototype, int unitId, Function<A, Result> callBack) throws ExecutionException, InterruptedException {
            Map<Integer, Future<ModbusResponse>> futureMap = new HashMap<>();
            Map<Integer, Result> resultMap = new HashMap<>();
            for (Map.Entry<Integer, ModbusTcpMaster> entry : masters.entrySet()) {
                ModbusTcpMaster master = entry.getValue();
                CompletableFuture<ModbusResponse> future = master.sendRequest(prototype.get(), unitId);
                futureMap.put(entry.getKey(), future);
            }
            // join
            for (Map.Entry<Integer, Future<ModbusResponse>> entry : futureMap.entrySet()) {
                ModbusResponse response = entry.getValue().get();
                resultMap.put(entry.getKey(), callBack.apply((A) response));
                ReferenceCountUtil.release(response);
            }
            return resultMap;
        }
    }

    public void putPeer(Integer id, String ipv4, Integer port) {
        slaves.put(id, generateMaster(ipv4, port));
        LOG.info("Modbus slave {}:{} added", ipv4, port);
    }

    public RequestExecutor all() {
        return new RequestExecutor(slaves);
    }

    public RequestExecutor selected(int... ids) {
        List<Integer> idList = Arrays.stream(ids).boxed().collect(Collectors.toList());
        return new RequestExecutor(
                slaves.entrySet().stream()
                        .filter(e -> idList.contains(e.getKey()))
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))
        );
    }

    public void close() {
        slaves.values().forEach(ModbusTcpMaster::disconnect);
        slaves.clear();
        LOG.info("All modbus connections closed.");
    }

    private ModbusTcpMaster generateMaster(String ipv4, Integer port) {
        ModbusTcpMasterConfig config = new ModbusTcpMasterConfig
                .Builder(ipv4)
                .setPort(port)
                .setTimeout(Duration.ofMillis(modWebConfig.getTimeout()))
                .setLazy(false)
                .build();
        ModbusTcpMaster modbusTcpMaster = new ModbusTcpMaster(config);
        modbusTcpMaster.connect();
        return modbusTcpMaster;
    }
}
