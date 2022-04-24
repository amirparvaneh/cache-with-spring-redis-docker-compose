package com.celonis.demo.repository;

import com.celonis.demo.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;


public interface ProductRepository extends JpaRepository<Product, String> {
}
