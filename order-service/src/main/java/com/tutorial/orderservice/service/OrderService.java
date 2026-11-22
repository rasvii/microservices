package com.tutorial.orderservice.service;

import com.tutorial.orderservice.event.OrderPlacedEvent;
import com.tutorial.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import com.tutorial.orderservice.dto.*;
import com.tutorial.orderservice.model.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final WebClient.Builder webClientBuilder;
    private final KafkaTemplate<String, OrderPlacedEvent> kafkaTemplate;

    public String placeOrder(OrderRequest orderRequest){
        Order order = new Order();
        order.setOrderNumber(UUID.randomUUID().toString());
        List<OrderLineItems> orderLineItems = orderRequest.getOrderLineItemsDtoList().stream()
                .map(this::mapToDto).toList();
        order.setOrderLineItemsList(orderLineItems);

        List<String> skuCodes = order.getOrderLineItemsList().stream().map(OrderLineItems::getSkuCode).toList();

        InventoryResponse[] inventoryResponses = webClientBuilder.build().get()
                .uri("http://inventory-service/api/inventory",
                        uriBuilder -> uriBuilder
                                .queryParam("skuCode", skuCodes)
                                .build())
                .retrieve()
                .bodyToMono(InventoryResponse[].class)
                .block();

        boolean allProductsInStock = inventoryResponses != null
                && Arrays.stream(inventoryResponses)
                .allMatch(response -> Boolean.TRUE.equals(response.getIsInStock()));

        if(!allProductsInStock) {
            throw new IllegalArgumentException("Product not in stock. Please try again later");
        }

        orderRepository.save(order);
        kafkaTemplate.send("notificationTopic", new OrderPlacedEvent(order.getOrderNumber()));
        return "Order placed successfully";
    }

    private OrderLineItems mapToDto(OrderLineItemsDto dto){
        OrderLineItems items = new OrderLineItems();
        items.setPrice(dto.getPrice());
        items.setQuantity(dto.getQuantity());
        items.setSkuCode(dto.getSkuCode());
        return items;
    }
}
