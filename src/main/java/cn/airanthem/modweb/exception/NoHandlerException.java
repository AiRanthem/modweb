package cn.airanthem.modweb.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
public class NoHandlerException extends RuntimeException{
    public NoHandlerException(String name) {
        super("no handler with name " + name);
        this.name = name;
    }

    String name;
}
