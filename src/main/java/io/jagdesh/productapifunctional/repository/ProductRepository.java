package io.jagdesh.productapifunctional.repository;

import io.jagdesh.productapifunctional.model.Product;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface ProductRepository
        extends ReactiveMongoRepository<Product, String> {
}
