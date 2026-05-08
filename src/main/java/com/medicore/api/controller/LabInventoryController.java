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

    @PostMapping
    public ResponseEntity<?> saveItem(@RequestBody Inventory item) {
        if (item.getReorderLevel() == null) item.setReorderLevel(10);
        item.setUpdatedAt(LocalDateTime.now());
        
        if (item.getQuantity() == 0) item.setStatus("Out of Stock");
        else if (item.getQuantity() < item.getReorderLevel()) item.setStatus("Low");
        else item.setStatus("In Stock");
        
        Inventory saved = inventoryRepository.save(item);
        return ResponseEntity.ok(Map.of("status", "success", "data", saved));
    }

    @DeleteMapping
    public ResponseEntity<?> deleteItem(@RequestParam Long id) {
        inventoryRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("status", "success", "message", "Item decommissioned"));
    }
}
