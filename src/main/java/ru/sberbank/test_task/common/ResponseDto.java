package ru.sberbank.test_task.common;

import lombok.Value;

@Value
public class ResponseDto<T> {
    String error;
    T data;
}
