package cl.duoc.lmsellms;

import org.junit.jupiter.api.Disabled; // <-- ¡No olvides esta importación!
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Disabled("Deshabilitado temporalmente para evitar que intente levantar la base de datos y Eureka en las pruebas de JaCoCo.")
class LmSellMsApplicationTests {

    @Test
    void contextLoads() {
    }

}