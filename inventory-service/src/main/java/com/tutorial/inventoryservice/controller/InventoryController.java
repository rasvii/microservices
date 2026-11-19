package com.tutorial.inventoryservice.controller;

import com.tutorial.inventoryservice.dto.InventoryResponse;
import com.tutorial.inventoryservice.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<InventoryResponse> isInStock(@RequestParam("skuCode") List<String> skuCode){
        List<InventoryResponse> response = inventoryService.isInStock(skuCode);
        return response;
    }
}
