package io.jagdesh.productapifunctional.handler;

import io.jagdesh.productapifunctional.model.Product;
import io.jagdesh.productapifunctional.model.ProductEvent;
import io.jagdesh.productapifunctional.repository.ProductRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Component
public class ProductHandler {

    private ProductRepository repo;

    public ProductHandler(ProductRepository repo) {
        this.repo = repo;
    }

    private Mono<ServerResponse> notFound() {
        return ServerResponse.notFound().build();
    }

    public Mono<ServerResponse> getAllProducts(ServerRequest request) {
        Flux<Product> products = repo.findAll();
        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(products, Product.class);
    }

    public Mono<ServerResponse> getProduct(ServerRequest request) {
        String id = request.pathVariable("id");
        Mono<Product> productMono = repo.findById(id);
        return productMono
                .flatMap(product ->
                        ServerResponse.ok()
                                .contentType(MediaType.APPLICATION_JSON)
                                .body(BodyInserters.fromValue(product)))
                .switchIfEmpty(notFound());
    }

    public Mono<ServerResponse> saveProduct(ServerRequest serverRequest) {
        Mono<Product> productMono = serverRequest.bodyToMono(Product.class);
        return productMono.flatMap(product ->
                ServerResponse.status(HttpStatus.CREATED)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(repo.save(product), Product.class));
    }

    public Mono<ServerResponse> updateProduct(ServerRequest serverRequest) {
        String id = serverRequest.pathVariable("id");
        Mono<Product> existingProductMono = repo.findById(id);
        Mono<Product> productMono = serverRequest.bodyToMono(Product.class);
        return productMono.zipWith(existingProductMono,
                        (product, existingProduct) ->
                                new Product(existingProduct.getId(), product.getName(), product.getPrice()))
                .flatMap(product ->
                        ServerResponse.ok()
                                .contentType(MediaType.APPLICATION_JSON)
                                .body(repo.save(product), Product.class)
                ).switchIfEmpty(notFound());
    }

    public Mono<ServerResponse> deleteProduct(ServerRequest serverRequest) {
        String id = serverRequest.pathVariable("id");
        Mono<Product> productMono = repo.findById(id);
        return productMono
                .flatMap(product ->
                        ServerResponse.ok()
                                .build(repo.delete(product))
                                .switchIfEmpty(notFound()));
    }

    public Mono<ServerResponse> deleteProducts(ServerRequest serverRequest) {
        return ServerResponse.ok()
                .build(repo.deleteAll());
    }

    public Mono<ServerResponse> getProductEvents(ServerRequest serverRequest) {
        Flux<ProductEvent> eventsFlux = Flux.interval(Duration.ofSeconds(1))
                .map(val -> new ProductEvent(val, "Product Event"));
        return ServerResponse.ok()
                .contentType(MediaType.TEXT_EVENT_STREAM)
                .body(eventsFlux, ProductEvent.class);
    }

}
