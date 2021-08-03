package it.olly.teamsconnectorsample;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

import it.olly.teamsconnectorsample.filter.ApiFilter;

/**
 * Extends SpringBootServletInitializer 4 tomcat deployment<br>
 * <br>
 * NOTE: to run this webapp on tomcat10, the generated war has to be converted
 * using jakartaee-migration Tool.<br>
 * This could be done in two ways:
 * <ol>
 * <li>by Using jakartaee-migration Tool.</li>
 * <li>by adding the generated WAR file to /webapps-javaee folder (if it doesn't
 * exist, create it) instead of /webapp. on startup, tomcat will convert the old
 * war to a new one, placing it on /webapp folder, once the convertion has been
 * completed</li>
 * </ol>
 * For more details, see:
 * https://www.appsdeveloperblog.com/deploy-a-spring-boot-rest-app-as-a-war-to-tomcat-10/
 * 
 * @author alessio olivieri
 *
 */
@SpringBootApplication
public class TeamsConnectorSampleApplication extends SpringBootServletInitializer {

	public static void main(String[] args) {
		SpringApplication.run(TeamsConnectorSampleApplication.class, args);
	}

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(TeamsConnectorSampleApplication.class);
	}

	@Bean
	public HazelcastInstance hazelcastInstance() {
		return Hazelcast.newHazelcastInstance();
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
