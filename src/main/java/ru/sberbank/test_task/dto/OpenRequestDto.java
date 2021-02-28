package ru.sberbank.test_task.dto;

import lombok.Value;

import java.time.LocalDate;

@Value
public class OpenRequestDto {

    String accountNumber;
    LocalDate openDate;
}
