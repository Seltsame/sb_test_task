package ru.sberbank.test_task.dto;

import lombok.Value;

import java.time.LocalDate;

@Value
public class CloseRequestDto {

    String accountNumber;
    LocalDate closeDate;
}
