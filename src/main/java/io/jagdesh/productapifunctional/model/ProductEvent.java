package io.jagdesh.productapifunctional.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ProductEvent {

    private Long eventId;
    private String eventType;
}
