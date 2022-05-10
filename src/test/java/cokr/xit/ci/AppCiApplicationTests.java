package cokr.xit.ci;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestEntityManager;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@DataJpaTest
@AutoConfigureTestEntityManager
@AutoConfigureTestDatabase
class AppCiApplicationTests {

    @Test
    void contextLoads() {
    }

}
