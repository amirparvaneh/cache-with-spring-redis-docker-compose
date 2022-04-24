package com.celonis.demo.controller;


import com.celonis.demo.model.Product;
import com.celonis.demo.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api/v1")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PostMapping(value = "/product")
    public ResponseEntity<Product> createProduct(@RequestBody Product product) {
        Product savedProduct = productService.saveProduct(product);
        return new ResponseEntity<>(savedProduct, HttpStatus.CREATED);
    }

    @GetMapping(value = "/product/{city}")
    public ResponseEntity<?> getProduct(@PathVariable("city") String city) {
        Product savedProduct = productService.getProductById(city);
        if (savedProduct != null) {
            return new ResponseEntity<>(savedProduct, HttpStatus.OK);
        }
        return new ResponseEntity<>("Product not found", HttpStatus.OK);
    }

//    @GetMapping(value = "/product")
//    public ResponseEntity<List<Product>> getAllProduct() {
//        List<Product> productList = productService.getAllProduct();
//        return ResponseEntity.ok(productList);
//    }

    @PutMapping(value = "/product/{city}")
    public ResponseEntity<?> updateProduct(@PathVariable("city") String city, @RequestBody Product product) {
        Product savedProduct = productService.getProductById(product.getCity());
        if (savedProduct != null) {
            savedProduct.setCountry(product.getCountry());
            Product returnedProduct = productService.saveProduct(savedProduct);
            return new ResponseEntity<>(returnedProduct, HttpStatus.OK);
        }
        return new ResponseEntity<>("Product not found", HttpStatus.OK);
    }

    @DeleteMapping(value = "/product/{city}")
    public ResponseEntity<String> deleteProduct(@PathVariable("city") String city) {
        Product savedProduct = productService.getProductById(city);
        if (savedProduct != null) {
            productService.deleteProductById(city);
            return new ResponseEntity<>("Product deleted", HttpStatus.OK);
        }
        return new ResponseEntity<>("Product not found", HttpStatus.OK);
    }

//    @DeleteMapping(value = "/product")
//    public ResponseEntity<String> deleteAllProduct() {
//        productService.deleteAllProduct();
//        return new ResponseEntity<>("All Products deleted", HttpStatus.OK);
//    }

    @GetMapping(value = "/product/{city}/readonly")
    public ResponseEntity<?> getProductReadonly(@PathVariable("city") String city) {
        Product savedProduct = productService.getProductReadOnlyById(city);
        if (savedProduct != null) {
            return new ResponseEntity<>(savedProduct, HttpStatus.OK);
        }
        return new ResponseEntity<>("Product not found", HttpStatus.OK);
    }
}

