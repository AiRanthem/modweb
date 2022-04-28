package cn.airanthem.modweb.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class ServiceRuntimeException extends Throwable{
    int code;
}
