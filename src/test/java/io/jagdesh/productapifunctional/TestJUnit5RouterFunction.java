package io.jagdesh.productapifunctional;

import io.jagdesh.productapifunctional.model.Product;
import io.jagdesh.productapifunctional.model.ProductEvent;
import io.jagdesh.productapifunctional.repository.ProductRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.FluxExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.server.RouterFunction;
import reactor.test.StepVerifier;

import java.util.List;

@SpringBootTest
public class TestJUnit5RouterFunction {

    private WebTestClient client;
    private List<Product> expectedList;

    @Autowired
    private ProductRepository repo;

    @Autowired
    private RouterFunction routes;

    @BeforeEach
    public void beforeEach() {
        this.client = WebTestClient.bindToRouterFunction(routes)
                .configureClient()
                .baseUrl("/products")
                .build();
        this.expectedList = repo.findAll()
                .collectList()
                .block();
    }

    @Test
    public void testGetAllProducts() {
        client.get()
                .uri("/")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBodyList(Product.class)
                .isEqualTo(expectedList);
    }

    @Test
    public void testProductInvalidIdNotFound() {
        client.get()
                .uri("/aaa")
                .exchange()
                .expectStatus()
                .isNotFound();
    }

    @Test
    public void testProductIdFound() {
        Product expectedProduct = expectedList.get(0);
        client.get()
                .uri("/{id}", expectedProduct.getId())
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(Product.class)
                .isEqualTo(expectedProduct);
    }

    @Test
    public void testProductEvents() {
        FluxExchangeResult<ProductEvent> result = client.get()
                .uri("/events")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus()
                .isOk()
                .returnResult(ProductEvent.class);

        ProductEvent expectedEvent = new ProductEvent(0L, "Product Event");

        StepVerifier.create(result.getResponseBody())
                .expectNext(expectedEvent)
                .expectNextCount(2)
                .consumeNextWith(event ->
                        Assertions.assertEquals(Long.valueOf(3), event.getEventId())
                )
                .thenCancel()
                .verify();
    }

}
