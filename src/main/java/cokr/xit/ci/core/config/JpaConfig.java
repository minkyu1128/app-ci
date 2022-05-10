package cokr.xit.ci.core.config;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(basePackages = "cokr.xit.ci")
public class JpaConfig {
}
