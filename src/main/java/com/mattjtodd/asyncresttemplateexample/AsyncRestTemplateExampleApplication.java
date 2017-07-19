package com.mattjtodd.asyncresttemplateexample;

import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.omg.CORBA.TIMEOUT;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.HttpComponentsAsyncClientHttpRequestFactory;
import org.springframework.http.client.Netty4ClientHttpRequestFactory;
import org.springframework.web.client.AsyncRestTemplate;

@SpringBootApplication
public class AsyncRestTemplateExampleApplication {

	private static final int TIMEOUT = 3000;

	public static void main(String[] args) {
		SpringApplication.run(AsyncRestTemplateExampleApplication.class, args);
	}

	@Bean("HttpCommons")
	public AsyncRestTemplate getAsyncRestTemplate() {

		HttpComponentsAsyncClientHttpRequestFactory asyncRequestFactory = new HttpComponentsAsyncClientHttpRequestFactory();
		asyncRequestFactory.setHttpAsyncClient(asyncHttpClient());
		return new AsyncRestTemplate(asyncRequestFactory);
	}

	@Bean("Netty")
	public AsyncRestTemplate getNettyAsyncRestTemplate() {
		Netty4ClientHttpRequestFactory factory = new Netty4ClientHttpRequestFactory();
		factory.setReadTimeout(TIMEOUT);
		return new AsyncRestTemplate(factory);
	}

	@Bean
	public CloseableHttpAsyncClient asyncHttpClient() {

		IOReactorConfig ioReactorConfig = IOReactorConfig
				.custom()
				.setConnectTimeout(TIMEOUT)
				.setSoTimeout(TIMEOUT)
				.setIoThreadCount(5)
				.build();

		return HttpAsyncClients
				.custom()
				.setDefaultIOReactorConfig(ioReactorConfig)
				.build();
	}
}
