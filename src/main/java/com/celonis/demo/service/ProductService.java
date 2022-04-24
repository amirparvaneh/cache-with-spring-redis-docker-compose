package com.celonis.demo.service;

import com.celonis.demo.annotations.ReadOnlyCacheable;
import com.celonis.demo.model.BackUp;
import com.celonis.demo.model.Product;
import com.celonis.demo.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.HashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;

    @CachePut(value = "PRODUCT", key = "'PRONUM:' + #product.city")
    public Product saveProduct(Product product) {
        log.info("Saving product into DB");
        return productRepository.save(product);
    }

    @Cacheable(value = "PRODUCT", key = "'PRONUM:' + #city")
    public Product getProductById(String city) {
        log.info("Looking into DB for product {}", city);
        return productRepository.findById(city).orElse(null);
    }

    @ReadOnlyCacheable(value = "PRODUCT", key = "'PRONUM:' + #city")
    public Product getProductReadOnlyById(String city) {
        log.info("Looking into DB for product {}", city);
        return productRepository.findById(city).orElse(null);
    }

    @CacheEvict(value = "PRODUCT", key = "'PRONUM:' + #city")
    public void deleteProductById(String city) {
        productRepository.deleteById(city);
        log.info("Evicting Product number {} from cache", city);
    }

    //backup method for removed data
    public void savedRemovedFromCache(Product product) {
        BackUp backUp = new BackUp();
        backUp.setCityBackUp(product.getCity());
        backUp.setCountryBackUp(product.getCountry());
        log.info("Saving product into h2dataBase");
    }

    //thread
    private void waitSomeTime() {
        System.out.println("Waiting");
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("Wait End");
    }
}
