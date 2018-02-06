package org.onap.aai.web;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@Configuration
public class WebConfiguration {

	@Bean
	public WebMvcConfigurerAdapter forwardToIndex() {
		return new WebMvcConfigurerAdapter() {
			@Override
			public void addViewControllers(ViewControllerRegistry registry) {
				registry.addViewController("/swagger").setViewName(
						"redirect:/swagger/index.html");
				registry.addViewController("/swagger/").setViewName(
						"redirect:/swagger/index.html");
                registry.addViewController("/docs").setViewName(
                        "redirect:/docs/html/index.html");
                registry.addViewController("/docs/").setViewName(
                         "redirect:/docs/html/index.html");
			}
		};
	}
}