package cn.airanthem.modweb.exception;

import lombok.Getter;

@Getter
public class DuplicateHandlerException extends RuntimeException{
    public DuplicateHandlerException(String name) {
        super("duplicate handler with name " + name);
        this.name = name;
    }

    String name;
}
