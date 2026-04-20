package com.vetrifresh.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.vetrifresh.model.Category;
import com.vetrifresh.repository.CategoryRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    public Category getCategoryById(Long id) {
        return categoryRepository.findById(id)
               .orElseThrow(() -> new RuntimeException("Category not found"));
    }

    public Category save(Category category) {
        return categoryRepository.save(category);
    }

    public void deleteCategory(Long id) {
        categoryRepository.deleteById(id);
    }
}