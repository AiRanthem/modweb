package cn.airanthem.modweb.exception;

public class ModbusRequestException extends RuntimeException{
    public ModbusRequestException(Throwable cause) {
        super("modbus request error", cause);
    }
}
