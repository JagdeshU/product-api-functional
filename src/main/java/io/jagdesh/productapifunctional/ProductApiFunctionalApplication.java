package io.jagdesh.productapifunctional;

import io.jagdesh.productapifunctional.handler.ProductHandler;
import io.jagdesh.productapifunctional.model.Product;
import io.jagdesh.productapifunctional.repository.ProductRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import static org.springframework.web.reactive.function.server.RouterFunctions.nest;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@SpringBootApplication
public class ProductApiFunctionalApplication {

	public static void main(String[] args) {
		SpringApplication.run(ProductApiFunctionalApplication.class, args);
	}

	@Bean
	CommandLineRunner init(ProductRepository repo) {
		return args -> {
			Flux<Product> productFlux = Flux.just(
					new Product(null, "Big Latte", 2.99),
					new Product(null, "Big Decaf", 2.49),
					new Product(null, "Green Tea", 1.99)
			).flatMap(repo::save);

			productFlux
					.thenMany(repo.findAll())
					.subscribe(System.out::println);
		};
	}

	@Bean
	RouterFunction<ServerResponse> routes(ProductHandler handler) {
		return route()
				.path("/products",
						builder -> builder.nest(
								accept(MediaType.APPLICATION_JSON)
										.or(contentType(MediaType.APPLICATION_JSON))
										.or(accept(MediaType.TEXT_EVENT_STREAM)),
								nestedBuilder -> nestedBuilder
										.GET("/events", handler::getProductEvents)
										.GET("/{id}", handler::getProduct)
										.GET(handler::getAllProducts)
										.PUT("/{id}", handler::updateProduct)
										.POST(handler::saveProduct))
								.DELETE("/{id}", handler::deleteProduct)
								.DELETE(handler::deleteProducts)
				).build();
	}

}
