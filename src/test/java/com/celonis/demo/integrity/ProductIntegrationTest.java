package com.celonis.demo.integrity;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.celonis.demo.model.Product;
import com.celonis.demo.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@AutoConfigureMockMvc
@DisplayName("GIVEN product controller is called")
@SpringBootTest
@ActiveProfiles("test")
@EnableCaching
class ProductIntegrationTest {

    String city = "rom";
    Product product = Product.builder()
            .city("hamburg")
            .country("Germany")
            .build();
    Product product2 = Product.builder()
            .city("denver")
            .country("Denmark")
            .build();
    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CacheManager cacheManager;

    @BeforeEach
    public void setup() {
        productRepository.deleteAll();
        cacheManager.getCache("PRODUCT").clear();
    }

    @Test
    @DisplayName("WHEN [POST] endpoint is hit THEN create a new product in database AND persist the same in Cache")
    public void createProduct() throws Exception {
        MvcResult mvcResult = mockMvc.perform(post("/api/v1/product")
                .content(objectMapper.writeValueAsString(product))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(201))
                .andReturn();

        String response = mvcResult.getResponse().getContentAsString();
        Product actualResponse = objectMapper.readValue(response, new TypeReference<Product>() {
        });

        assertAll(
                () -> assertEquals(product.getCity(), actualResponse.getCity()),
                () -> assertEquals(product.getCountry(), actualResponse.getCountry())
        );
        Product cachedProduct = (Product) cacheManager.getCache("PRODUCT")
                .get("PRONUM:" + actualResponse.getCity()).get();
        assertEquals(actualResponse, cachedProduct);

        cacheManager.getCache("Product").evict("PRONUM:" + actualResponse.getCity());
        productRepository.delete(actualResponse);
    }

    @Test
    @DisplayName("WHEN [GET] endpoint is hit with a valid Product city as path variable THEN get the Product from cache")
    public void getProduct_whenProductFound() throws Exception {
        cacheManager.getCache("Product").put("PRONUM:" + city, product);

        MvcResult mvcResult = mockMvc.perform(get("/api/v1/product/{city}", city)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(200))
                .andReturn();
        assertNull(productRepository.findById(product.getCity()).orElse(null));
        cacheManager.getCache("PRODUCT").evict("PRONUM:" + city);
    }

    @Test
    @DisplayName("WHEN [GET] endpoint is hit with a invalid Product city as path variable THEN send Product not found message")
    public void getProduct_whenProductNotFound() throws Exception {
        MvcResult mvcResult = mockMvc.perform(get("/api/v1/product/{city}", city)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(200))
                .andReturn();

        String response = mvcResult.getResponse().getContentAsString();

        assertEquals(response, "Product not found");
    }

    @Test
    @DisplayName("WHEN [GET] Read Only endpoint is hit with a valid product city as path variable THEN get the product from cache, if not exist then go to DB but do not persist the data in Cache")
    public void getProductReadonly_whenProductFound() throws Exception {
        productRepository.save(product);

        MvcResult mvcResult = mockMvc.perform(get("/api/v1/product/{city}/readonly", city)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(200))
                .andReturn();
        assertThrows(NullPointerException.class, () -> cacheManager.getCache("PRODUCT")
                .get("PRONUM:" + product.getCity()).get());
    }

    @Test
    @DisplayName("WHEN [GET] Read Only endpoint is hit with a invalid Product city as path variable THEN get the Product from cache, if not exist then go to DB AND finally return Product not found")
    public void getProductReadonly_whenProductNotFound() throws Exception {

        MvcResult mvcResult = mockMvc.perform(get("/api/v1/product/{city}/readonly", city)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(200))
                .andReturn();

        String response = mvcResult.getResponse().getContentAsString();

        assertEquals(response, "Product not found");

        assertThrows(NullPointerException.class, () -> cacheManager.getCache("PRODUCT")
                .get("PRONUM:" + product.getCity()).get());
    }

    @Test
    @DisplayName("WHEN [UPDATE] endpoint is hit with a valid product city as path variable THEN update the Product in database AND cache")
    public void updateProduct_GivenCorrectProduct() throws Exception {
        productRepository.save(product);
        Product updatedProduct = Product.builder()
                .city("hamburg")
                .country("Germany")
                .build();

        MvcResult mvcResult = mockMvc.perform(put("/api/v1/product/{city}", city)
                .content(objectMapper.writeValueAsString(updatedProduct))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(200))
                .andReturn();

        String response = mvcResult.getResponse().getContentAsString();
        Product actualResponse = objectMapper.readValue(response, new TypeReference<Product>() {
        });

        assertAll(
                () -> assertEquals(updatedProduct.getCity(), actualResponse.getCity()),
                () -> assertEquals(updatedProduct.getCountry(), actualResponse.getCountry())
        );
        Product cachedProduct = (Product) cacheManager.getCache("PRODUCT")
                .get("PRONUM:" + actualResponse.getCity()).get();
        assertEquals(updatedProduct, cachedProduct);

        cacheManager.getCache("PRODUCT").evict("PRONUM:" + actualResponse.getCity());
        productRepository.delete(actualResponse);
    }

    @Test
    @DisplayName("WHEN [UPDATE] endpoint is hit with a invalid product city as path variable THEN return product not found message")
    public void updateProduct_GivenIncorrectProduct() throws Exception {
        MvcResult mvcResult = mockMvc.perform(put("/api/v1/product/{city}", city)
                .content(objectMapper.writeValueAsString(product2))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(200))
                .andReturn();

        String response = mvcResult.getResponse().getContentAsString();

        assertEquals(response, "Product not found");
    }

    @Test
    @DisplayName("WHEN [DELETE] endpoint is hit with a valid Product city as path variable THEN delete that product from database AND cache")
    public void deleteProduct_GivenValidProduct() throws Exception {
        productRepository.save(product);
        cacheManager.getCache("PRODUCT").put("PRONUM:" + city, product);
    }

    @Test
    @DisplayName("WHEN [DELETE] endpoint is hit with a invalid product city as path variable THEN return product not found message")
    public void deleteProduct_GivenInvalidProduct() throws Exception {
        MvcResult mvcResult = mockMvc.perform(delete("/api/v1/product/{city}", city)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(200))
                .andReturn();
        String response = mvcResult.getResponse().getContentAsString();

        assertEquals(response, "Product not found");
    }
}
