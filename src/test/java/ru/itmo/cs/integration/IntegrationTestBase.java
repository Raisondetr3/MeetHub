package ru.itmo.cs.integration;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.itmo.cs.entity.User;
import ru.itmo.cs.service.JwtService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@ActiveProfiles("test")
@Rollback
@Transactional
public class IntegrationTestBase {

    @MockBean
    protected JwtService jwtService;

    @BeforeEach
    void setUpMocks() {
        when(jwtService.getSecretKey()).thenReturn("H++QPACG3PMKlsKILieJCXpW0DrJUMpiPTcU/KosFYU=");
        when(jwtService.getJwtExpiration()).thenReturn(3600000L);
        when(jwtService.extractUsername(anyString())).thenReturn("testUser");
        when(jwtService.isTokenValid(anyString(), any())).thenReturn(true);
    }

    protected String generateToken(User user) {
        return "Bearer mockedToken";
    }
}