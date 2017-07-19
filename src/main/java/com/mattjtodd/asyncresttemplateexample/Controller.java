package com.mattjtodd.asyncresttemplateexample;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.AsyncRestTemplate;
import org.springframework.web.context.request.async.DeferredResult;

import java.lang.management.ManagementFactory;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@RestController
public class Controller {
    private final AsyncRestTemplate nettyAsyncRestTemplate;

    private final AsyncRestTemplate httpCommonsAsyncRestTemplate;

    private final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);

    Controller(@Autowired @Qualifier("Netty") AsyncRestTemplate nettyAsyncRestTemplate,
               @Autowired @Qualifier("HttpCommons") AsyncRestTemplate httpCommonsAsyncRestTemplate) {
        this.nettyAsyncRestTemplate = nettyAsyncRestTemplate;
        this.httpCommonsAsyncRestTemplate = httpCommonsAsyncRestTemplate;
    }

    @RequestMapping("/http")
    public DeferredResult<String> http() {
        return operation(nettyAsyncRestTemplate);
    }

    @RequestMapping("/http2")
    public DeferredResult<String> http2() {
        return operation(httpCommonsAsyncRestTemplate);
    }

    DeferredResult<String> operation(AsyncRestTemplate asyncRestTemplate) {
        System.out.println(ManagementFactory.getThreadMXBean().getThreadCount());
        System.out.println(Thread.currentThread());

        DeferredResult<String> deferredResult = new DeferredResult<>();

        List<CompletableFuture<ResponseEntity<String>>> completableFutures = IntStream
                .range(0, 50)
                .mapToObj(__ -> toFuture(asyncRestTemplate.getForEntity("http://www.google.co.uk", String.class)))
                .collect(Collectors.toList());

        CompletableFuture
                .allOf(completableFutures.toArray(new CompletableFuture[completableFutures.size()]))
                .thenAccept(__ -> {
                    deferredResult.setResult(completableFutures.toString());
                    System.out.println(Thread.currentThread());
                });

        return deferredResult;
    }

    @RequestMapping("/delay")
    public DeferredResult<String> delay() {
        System.out.println(ManagementFactory.getThreadMXBean().getThreadCount());
        System.out.println(Thread.currentThread());

        DeferredResult<String> deferredResult = new DeferredResult<>();

        executorService.schedule(() -> {
            deferredResult.setResult("Completed");
            System.out.println(Thread.currentThread());
        }, 15, TimeUnit.SECONDS);

        return deferredResult;
    }

    private static <T> CompletableFuture<ResponseEntity<T>> toFuture(ListenableFuture<ResponseEntity<T>> future) {
        CompletableFuture<ResponseEntity<T>> completableFuture = new CompletableFuture<>();
        future.addCallback(completableFuture::complete, completableFuture::completeExceptionally);
        return completableFuture;
    }
}
