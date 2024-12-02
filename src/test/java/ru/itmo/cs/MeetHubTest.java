package ru.itmo.cs;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
@SpringBootTest
@ActiveProfiles("test")
class MeetHubTest {

//    @BeforeAll
//    public static void loadEnv() {
//        Dotenv dotenv = Dotenv.configure().load();
//        dotenv.entries().forEach(entry -> System.setProperty(entry.getKey(), entry.getValue()));
//    }

    @BeforeAll
    public static void setUp() {
        // Database configuration
        System.setProperty("spring.datasource.url", "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1");
        System.setProperty("spring.datasource.username", "sa");
        System.setProperty("spring.datasource.password", "");
        System.setProperty("spring.datasource.driver-class-name", "org.h2.Driver");

        // JWT configuration
        System.setProperty("security.jwt.secret-key", "H++QPACG3PMKlsKILieJCXpW0DrJUMpiPTcU/KosFYU=");
        System.setProperty("security.jwt.expiration-time", "3600000");
    }

  @Test
  void contextLoads() {}
}
