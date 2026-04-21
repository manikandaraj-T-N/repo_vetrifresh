package com.vetrifresh.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.vetrifresh.model.Blog;

public interface BlogRepository extends JpaRepository<Blog, Long> {
    List<Blog> findByIsPublishedTrueOrderByCreatedAtDesc();
    List<Blog> findByCategoryAndIsPublishedTrue(String category);


    List<Blog> findTop5ByIsPublishedTrueOrderByCreatedAtDesc();
    List<Blog> findByIsPublishedTrueAndCategoryOrderByCreatedAtDesc(String category);
}