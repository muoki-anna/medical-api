package com.medicore.api.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "inventory")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Inventory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "item_code", unique = true)
    private String itemCode;

    private String name;
    private String category;
    private Integer quantity;
    private String unit;

    @Column(name = "reorder_level")
    private Integer reorderLevel;

    private String status;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();
}
