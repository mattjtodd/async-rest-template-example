package com.mattjtodd.asyncresttemplateexample;

import org.springframework.http.ResponseEntity;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.AsyncRestTemplate;
import org.springframework.web.context.request.async.DeferredResult;

import java.lang.management.ManagementFactory;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@RestController
public class Controller {
    private final AsyncRestTemplate asyncRestTemplate;


    Controller(AsyncRestTemplate asyncRestTemplate) {
        this.asyncRestTemplate = asyncRestTemplate;
    }

    @RequestMapping("/")
    public DeferredResult<String> operation() {
        System.out.println(ManagementFactory.getThreadMXBean().getThreadCount());

        DeferredResult<String> deferredResult = new DeferredResult<>();

        List<CompletableFuture<ResponseEntity<String>>> completableFutures = IntStream
                .range(0, 50)
                .mapToObj(__ -> toFuture(asyncRestTemplate.getForEntity("http://www.google.co.uk", String.class)))
                .collect(Collectors.toList());

        CompletableFuture
                .allOf(completableFutures.toArray(new CompletableFuture[completableFutures.size()]))
                .thenAccept(__ -> deferredResult.setResult("Completed"));

        return deferredResult;
    }

    private static <T> CompletableFuture<ResponseEntity<T>> toFuture(ListenableFuture<ResponseEntity<T>> future) {
        CompletableFuture<ResponseEntity<T>> completableFuture = new CompletableFuture<>();
        future.addCallback(completableFuture::complete, completableFuture::completeExceptionally);
        return completableFuture;
    }
}
