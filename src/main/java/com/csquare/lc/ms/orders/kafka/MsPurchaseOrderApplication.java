package com.csquare.lc.ms.orders.kafka;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication(scanBasePackages = {"com.csquare.ms.lib","com.csquare.lc.ms.orders.lib", "com.csquare.lc.ms.orders.kafka"})
//@EnableJpaRepositories("com.csquare.lc.ms.orders.lib.repos")
@EnableMongoRepositories("com.csquare.lc.ms.orders.lib.repos")
//@EntityScan("com.csquare.lc.ms.orders.lib.model")
public class MsPurchaseOrderApplication {

	public static void main(String[] args) {
		new SpringApplicationBuilder(MsPurchaseOrderApplication.class)
				.properties("spring.config.name:application,db,log,spy", "spring.config.location:classpath:/")
				.build()
				.run(args);
	}

}
