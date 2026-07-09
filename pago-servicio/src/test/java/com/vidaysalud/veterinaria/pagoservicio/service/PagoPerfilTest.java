package com.vidaysalud.veterinaria.pagoservicio;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
class PagoPerfilTest {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private org.springframework.core.env.Environment environment;

    @Test
    void debeCargarPerfilTest() {
        assertTrue(
                Arrays.asList(environment.getActiveProfiles()).contains("test")
        );
    }

    @Test
    void debeUsarBaseDeDatosDeTest() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            String url = connection.getMetaData().getURL();

            assertTrue(
                    url.contains("veterinaria_db_test"),
                    "La prueba debe usar la base veterinaria_db_test, pero está usando: " + url
            );
        }
    }
}