package it.olly.teamsconnectorsample;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

import it.olly.teamsconnectorsample.filter.ApiFilter;

@SpringBootApplication
public class TeamsConnectorSampleApplication {

	public static void main(String[] args) {
		SpringApplication.run(TeamsConnectorSampleApplication.class, args);
	}

	@Bean
	public FilterRegistrationBean<ApiFilter> apiFilter() {
		FilterRegistrationBean<ApiFilter> registrationBean = new FilterRegistrationBean<>();
		registrationBean.setFilter(new ApiFilter());
		registrationBean.addUrlPatterns(ApiFilter.URL_PATTERNS);
		return registrationBean;
	}

	@Bean
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}

}
