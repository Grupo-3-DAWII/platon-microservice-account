package edu.cibertec.account;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.SpringApplication;

import static org.mockito.Mockito.mockStatic;

class AccountApplicationTest {

    @Test
    void shouldStartSpringApplication() {
        try (MockedStatic<SpringApplication> springApplication = mockStatic(SpringApplication.class)) {
            AccountApplication.main(new String[0]);

            springApplication.verify(() -> SpringApplication.run(AccountApplication.class, new String[0]));
        }
    }
}
