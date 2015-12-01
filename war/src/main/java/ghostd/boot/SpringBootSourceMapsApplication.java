package ghostd.boot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class SpringBootSourceMapsApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringBootSourceMapsApplication.class, args);
    }
}
