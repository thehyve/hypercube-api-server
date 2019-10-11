package nl.thehyve.hypercubeapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@EnableTransactionManagement
@EntityScan("nl.thehyve.hypercubeapi.*")
@EnableJpaRepositories("nl.thehyve.hypercubeapi.*")
@SpringBootApplication
@EnableConfigurationProperties({ LiquibaseProperties.class })
public class HypercubeApiServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(HypercubeApiServerApplication.class, args);
	}

}
