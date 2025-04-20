package com.github.mangila.pokedex.scheduler;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.testcontainers.Testcontainers;

@Import(TestcontainersConfiguration.class)
@SpringBootTest(properties = "app.scheduler.enabled=false")
class SchedulerApplicationTests {

    @Test
    void contextLoads() {

    }

}
