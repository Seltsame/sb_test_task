package ru.sberbank.test_task.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.sberbank.test_task.MyException;
import ru.sberbank.test_task.ValidateException;
import ru.sberbank.test_task.common.ResponseDto;
import ru.sberbank.test_task.dto.AccountResponseDto;
import ru.sberbank.test_task.dto.CloseRequestDto;
import ru.sberbank.test_task.dto.OpenRequestDto;
import ru.sberbank.test_task.service.AccountService;

import java.sql.SQLException;
import java.time.LocalDate;

@Slf4j
@RestController
@RequestMapping("account")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @PostMapping("open")
    ResponseDto<AccountResponseDto> open(@RequestBody OpenRequestDto openRequestDto) {
        log.debug("open: started with data: {}", openRequestDto);
        AccountResponseDto result = accountService.openAccount(openRequestDto);
        log.info("open: finished for data: {} with result: {}", openRequestDto, result);
        return new ResponseDto<>(null, result);
    }

    @PutMapping("close")
    ResponseDto<AccountResponseDto> close(@RequestBody CloseRequestDto closeRequestDto) throws MyException {
        log.debug("close: started with data: {}", closeRequestDto);
        AccountResponseDto result = accountService.closeAccount(closeRequestDto);
        log.info("close: finished for data: {} with result: {}", closeRequestDto, result);
        return new ResponseDto<>(null, result);
    }

    @GetMapping("check")
    ResponseDto<AccountResponseDto> check(@RequestParam(name = "accountNumber") String accountNumber,
                                          @RequestParam("date")
                                          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        log.debug("check: started with account number: {}, and date:  {}", accountNumber, date);
        AccountResponseDto result = accountService.checkAccount(accountNumber, date);
        log.info("check: finished for account number: {}, and date:  {}, with result: {}", accountNumber, date, result);
        return new ResponseDto<>(null, result);
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    ResponseDto<AccountResponseDto> handleValidateException(ValidateException err) {
        String errMsg = err.getMessage();
        log.error("handleValidateException: finished with exception: {}", errMsg);
        return new ResponseDto<>(errMsg, null);
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    ResponseDto<AccountResponseDto> handleException(SQLException err) {
        log.error("handleValidateException: finished with exception: ", err);
        return new ResponseDto<>("ERROR", null);
    }
}
