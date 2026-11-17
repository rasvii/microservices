package com.tutorial.orderservice.service;

import com.tutorial.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.tutorial.orderservice.dto.*;
import com.tutorial.orderservice.model.*;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;


    public void placeOrder(OrderRequest orderRequest){
        Order order = new Order();
        order.setOrderNumber(UUID.randomUUID().toString());
        List<OrderLineItems> orderLineItems = orderRequest.getOrderLineItemsDtoList().stream()
                .map(this::mapToDto).toList();
        order.setOrderLineItemsList(orderLineItems);
        orderRepository.save(order);
    }

    private OrderLineItems mapToDto(OrderLineItemsDto dto){
        OrderLineItems items = new OrderLineItems();
        items.setPrice(dto.getPrice());
        items.setQuantity(dto.getQuantity());
        items.setSkuCode(dto.getSkuCode());
        return items;
    }
}
