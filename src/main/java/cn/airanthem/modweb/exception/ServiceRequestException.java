package cn.airanthem.modweb.exception;

import lombok.Getter;

@Getter
public class ServiceRequestException extends RuntimeException{
    String name;

    public ServiceRequestException(String name, Throwable cause) {
        super("request service error", cause);
        this.name = name;
    }
}
