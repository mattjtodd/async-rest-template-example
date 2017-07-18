package com.mattjtodd.asyncresttemplateexample;

import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsAsyncClientHttpRequestFactory;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.client.AsyncRestTemplate;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.stream.IntStream;

@SpringBootApplication
public class AsyncRestTemplateExampleApplication {

	public static void main(String[] args) {
		ConfigurableApplicationContext context = SpringApplication.run(AsyncRestTemplateExampleApplication.class, args);

		AsyncRestTemplate asyncRestTemplate = context.getBean(AsyncRestTemplate.class);

		System.out.println(ManagementFactory.getThreadMXBean().getThreadCount());

		try (CloseableHttpAsyncClient client = context.getBean(CloseableHttpAsyncClient.class)) {
			CompletableFuture[] completableFutures = IntStream
					.range(0, 50)
					.mapToObj(__ -> toFuture(asyncRestTemplate.getForEntity("http://www.google.co.uk", String.class)))
					.toArray(CompletableFuture[]::new);

			CompletableFuture
					.allOf(completableFutures)
					.join();

			System.out.println(Arrays.asList(completableFutures));
			System.out.println(ManagementFactory.getThreadMXBean().getThreadCount());

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static <T> CompletableFuture<ResponseEntity<T>> toFuture(ListenableFuture<ResponseEntity<T>> future) {
		CompletableFuture<ResponseEntity<T>> completableFuture = new CompletableFuture<>();
		future.addCallback(completableFuture::complete, completableFuture::completeExceptionally);
		return completableFuture;
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
