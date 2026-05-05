package com.medicore.api.controller;

import com.medicore.api.model.Inventory;
import com.medicore.api.repository.InventoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/labtech/inventory")
@CrossOrigin(origins = "*")
public class LabInventoryController {

    @Autowired
    private InventoryRepository inventoryRepository;

    @GetMapping
    public ResponseEntity<?> getInventory() {
        List<Inventory> items = inventoryRepository.findAll();
        return ResponseEntity.ok(Map.of("status", "success", "data", items));
    }

    @PostMapping("/sync")
    public ResponseEntity<?> syncStock(@RequestBody Map<String, Object> body) {
        Long id = Long.valueOf(body.get("id").toString());
        Integer quantity = Integer.valueOf(body.get("quantity").toString());
        
        Inventory item = inventoryRepository.findById(id).orElseThrow();
        item.setQuantity(quantity);
        item.setUpdatedAt(LocalDateTime.now());
        
        if (quantity == 0) item.setStatus("Out of Stock");
        else if (quantity < item.getReorderLevel()) item.setStatus("Low");
        else item.setStatus("In Stock");
        
        inventoryRepository.save(item);
        return ResponseEntity.ok(Map.of("status", "success", "message", "Stock levels synchronized"));
    }
}
