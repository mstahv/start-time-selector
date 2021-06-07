package org.peimari.starttimeselector;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;

import org.peimari.starttimeselector.entities.Competition;
import org.peimari.starttimeselector.entities.CompetitionRepository;
import org.peimari.starttimeselector.entities.StartTimeRepository;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.ResourceBundleMessageSource;

@SpringBootApplication
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	@Bean
	public MessageSource messageSource () {
		ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
		messageSource.setDefaultEncoding("UTF-8");
		messageSource.setBasename("messages");
		messageSource.setFallbackToSystemLocale(true);
		messageSource.setUseCodeAsDefaultMessage(true);
		return messageSource;
	}
	
}
