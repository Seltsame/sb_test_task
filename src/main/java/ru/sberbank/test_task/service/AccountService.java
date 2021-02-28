package ru.sberbank.test_task.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.sberbank.test_task.MyException;
import ru.sberbank.test_task.ValidateException;
import ru.sberbank.test_task.dto.AccountResponseDto;
import ru.sberbank.test_task.dto.CloseRequestDto;
import ru.sberbank.test_task.dto.OpenRequestDto;
import ru.sberbank.test_task.model.Account;
import ru.sberbank.test_task.repository.AccountRepository;

import javax.transaction.Transactional;
import java.time.LocalDate;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    //паттерн для проверки, что строка - это число
    private static final Pattern pattern = Pattern.compile("\\d+?");

    @Transactional
    public AccountResponseDto openAccount(OpenRequestDto openRequestDto) {
        String accountNumber = openRequestDto.getAccountNumber();
        validateAccountNumber(accountNumber);
        if (!accountRepository.existsByOpenDateBeforeAndAccountNumber(
                openRequestDto.getOpenDate(), accountNumber)) {
            Account accountEntity = toEntity(openRequestDto);
            accountRepository.save(accountEntity);
            return new AccountResponseDto("SUCCESS");
        } else {
            return new AccountResponseDto("ERROR");
        }
    }

    @Transactional
    public AccountResponseDto closeAccount(CloseRequestDto closeRequestDto) throws MyException {
        String accountNumber = closeRequestDto.getAccountNumber();
        validateAccountNumber(accountNumber);
        LocalDate closeDate = closeRequestDto.getCloseDate();
        List<Account> activeAccounts = accountRepository.findAllByDateAndAccountNumber(closeDate, accountNumber);
        if (activeAccounts.size() != 1) {
            throw new ValidateException(
                    "Ошибка! Существует несколько счетов с номером: " + accountNumber + ", на дату закрытия: " + closeDate +
                            ". Или счёта не существует!");
        } else {
            Account account = activeAccounts.get(0);
            account.setCloseDate(closeDate);
            try {
                accountRepository.save(account);
            } catch (Exception exc) { //при ошибке записи в бд выводит на экран ERROR
                throw new MyException("ERROR");
            }
            return new AccountResponseDto("SUCCESS");
        }
    }

    public AccountResponseDto checkAccount(String accountNumber, LocalDate date) {
        validateAccountNumber(accountNumber);
        List<Account> activeAccounts = accountRepository.findAllByDateAndAccountNumber(date, accountNumber);
        if (activeAccounts.size() > 0) {
            return new AccountResponseDto("OPEN");
        } else {
            return new AccountResponseDto("CLOSE");
        }
    }

    //проверка на то, что номер счёта - натуральное число > 0
    private static boolean isNumeric(String str) {
        if (str != null) {
            Matcher matcher = pattern.matcher(str);
            while (matcher.find()) {
                int result = Integer.parseInt(str.substring(matcher.start(), matcher.end()));
                return result > 0;
            }
        }
        return false;
    }

    //ошибка при несоотвтетсвии номер счета - число
    private void validateAccountNumber(String number) {
        if (!isNumeric(number)) {
            throw new ValidateException("Номер счёта должен быть указан целым числом!");
        }
    }

    private Account toEntity(OpenRequestDto openRequestDto) {
        Account account = new Account();
        account.setAccountNumber(openRequestDto.getAccountNumber());
        account.setOpenDate(openRequestDto.getOpenDate());
        account.setCloseDate(null);
        return account;
    }
}
