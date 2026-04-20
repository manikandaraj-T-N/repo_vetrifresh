package com.vetrifresh.controller;

import com.vetrifresh.model.Category;
import com.vetrifresh.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryRepository categoryRepository;

    @GetMapping
    public ResponseEntity<List<Category>> getAll() {
        return ResponseEntity.ok(categoryRepository.findByIsActiveTrue());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Category> getById(@PathVariable Long id) {
        return ResponseEntity.ok(categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found")));
    }

    @PostMapping("/admin")
    public ResponseEntity<Category> create(@RequestBody Category category) {
        return ResponseEntity.ok(categoryRepository.save(category));
    }

    @PutMapping("/admin/{id}")
    public ResponseEntity<Category> update(@PathVariable Long id, @RequestBody Category updated) {
        Category cat = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));
        cat.setName(updated.getName());
        cat.setDescription(updated.getDescription());
        cat.setSlug(updated.getSlug());
        cat.setImageUrl(updated.getImageUrl());
        return ResponseEntity.ok(categoryRepository.save(cat));
    }
}
