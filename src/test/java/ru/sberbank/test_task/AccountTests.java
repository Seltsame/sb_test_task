package ru.sberbank.test_task;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import ru.sberbank.test_task.common.ResponseDto;
import ru.sberbank.test_task.dto.AccountResponseDto;
import ru.sberbank.test_task.dto.CloseRequestDto;
import ru.sberbank.test_task.dto.OpenRequestDto;

import java.net.URI;
import java.time.LocalDate;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test_task")
class AccountTests {

    @Autowired
    TestRestTemplate testRestTemplate;

    public static String resourceUrl;

    private static final ParameterizedTypeReference<ResponseDto<AccountResponseDto>> RESPONSE_ENTITY
            = new ParameterizedTypeReference<>() {
    };

    @LocalServerPort
    int port;

    @BeforeEach
    public void setupUrl() {
        resourceUrl = "http://localhost:" + port + "/account";
    }

    @Test
    void open() { //номер аккаунта через рандом, чтобы постоянно не чистить базу
        String openUrl = resourceUrl + "/open";
        LocalDate date = LocalDate.of(2021, 2, 25);
        int min = 100;
        int max = 2000;
        int diff = max - min;
        Random random = new Random();
        int randomAccount = random.nextInt(diff + 1);
        randomAccount += min;
        String accountNumber = String.valueOf(randomAccount);

        OpenRequestDto requestDto = new OpenRequestDto(accountNumber, date);

        RequestEntity<OpenRequestDto> request = RequestEntity.post(URI.create(openUrl))
                .contentType(MediaType.APPLICATION_JSON)
                .body(requestDto);

        ResponseEntity<ResponseDto<AccountResponseDto>> response
                = testRestTemplate.exchange(openUrl, HttpMethod.POST, request, RESPONSE_ENTITY);

        assertThat(response.getBody()).isNotNull();
        AccountResponseDto data = response.getBody().getData();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(data.getData()).isNotNull();
        assertThat(data.getData()).isEqualTo("SUCCESS");
    }

    @Test
    void invalidOpenAccount() {
        String openUrl = resourceUrl + "/open";
        LocalDate date = LocalDate.of(2021, 2, 25);
        String accountNumber = "01";
        OpenRequestDto requestDto = new OpenRequestDto(accountNumber, date);

        RequestEntity<OpenRequestDto> request = RequestEntity.post(URI.create(openUrl))
                .contentType(MediaType.APPLICATION_JSON)
                .body(requestDto);

        ResponseEntity<ResponseDto<AccountResponseDto>> response
                = testRestTemplate.exchange(openUrl, HttpMethod.POST, request, RESPONSE_ENTITY);

        assertThat(response.getBody()).isNotNull();
        String error = response.getBody().getError();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(error).isNotNull();
        assertThat(error).isEqualTo("Номер счёта должен быть указан целым числом!");
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
            "88|2021-02-27",
            "89|2021-02-27",})
    void invalidCloseAccount(String accountNumber, LocalDate date) {
        String closeUrl = resourceUrl + "/close";
        CloseRequestDto requestDto = new CloseRequestDto(accountNumber, date);

        RequestEntity<CloseRequestDto> request = RequestEntity.put(URI.create(closeUrl))
                .contentType(MediaType.APPLICATION_JSON)
                .body(requestDto);

        ResponseEntity<ResponseDto<AccountResponseDto>> response
                = testRestTemplate.exchange(closeUrl, HttpMethod.PUT, request, RESPONSE_ENTITY);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        String error = response.getBody().getError();
        assertThat(error).isEqualTo("Ошибка! Существует несколько счетов с номером: "
                + accountNumber + ", на дату закрытия: " + date + ". Или счёта не существует!");
    }

    @Test
    void positiveCloseAccount() {
        String closeUrl = resourceUrl + "/close";
        LocalDate date = LocalDate.of(2021, 2, 23);
        CloseRequestDto requestDto = new CloseRequestDto("2", date);

        RequestEntity<CloseRequestDto> request = RequestEntity.put(URI.create(closeUrl))
                .contentType(MediaType.APPLICATION_JSON)
                .body(requestDto);

        ResponseEntity<ResponseDto<AccountResponseDto>> response
                = testRestTemplate.exchange(closeUrl, HttpMethod.PUT, request, RESPONSE_ENTITY);

        assertThat(response.getBody()).isNotNull();
        AccountResponseDto data = response.getBody().getData();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(data.getData()).isEqualTo("SUCCESS");
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
            "2021-02-03|1|OPEN",
            "2021-02-23|1|CLOSE"})
    void checkAccountStatus(LocalDate date, String accountNumber, String expectedStatus) {
        String checkUrl = resourceUrl + "/check?date=" + date + "&accountNumber=" + accountNumber;
        ResponseEntity<ResponseDto<AccountResponseDto>> response
                = testRestTemplate.exchange(checkUrl, HttpMethod.GET, null, RESPONSE_ENTITY);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        AccountResponseDto data = response.getBody().getData();
        assertThat(data.getData()).isEqualTo(expectedStatus);
    }
}
