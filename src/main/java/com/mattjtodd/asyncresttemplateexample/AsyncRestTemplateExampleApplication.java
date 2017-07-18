package com.mattjtodd.asyncresttemplateexample;

import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.HttpComponentsAsyncClientHttpRequestFactory;
import org.springframework.web.client.AsyncRestTemplate;

@SpringBootApplication
public class AsyncRestTemplateExampleApplication {

	public static void main(String[] args) {
		SpringApplication.run(AsyncRestTemplateExampleApplication.class, args);
	}

	@Bean
	public AsyncRestTemplate getAsyncRestTemplate() {

		HttpComponentsAsyncClientHttpRequestFactory asyncRequestFactory = new HttpComponentsAsyncClientHttpRequestFactory();
		asyncRequestFactory.setHttpAsyncClient(asyncHttpClient());
		return new AsyncRestTemplate(asyncRequestFactory);
	}

	@Bean
	public CloseableHttpAsyncClient asyncHttpClient() {
		int connectionTimeout = 3000;

		IOReactorConfig ioReactorConfig = IOReactorConfig
				.custom()
				.setConnectTimeout(connectionTimeout)
				.setSoTimeout(connectionTimeout)
				.setIoThreadCount(1)
				.build();

		return HttpAsyncClients
				.custom()
				.setDefaultIOReactorConfig(ioReactorConfig)
				.build();
	}
}
