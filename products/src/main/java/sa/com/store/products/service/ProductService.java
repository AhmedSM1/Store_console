package sa.com.store.products.service;

import sa.com.store.products.controller.dto.CreateProductRequest;
import sa.com.store.products.controller.dto.ProductDTO;

import java.util.List;
import java.util.Optional;

public interface ProductService {

    ProductDTO createNewProduct(CreateProductRequest request);

    List<ProductDTO> getAllProducts();

    ProductDTO getProductById(String id);

    List<ProductDTO> getAllProductsByCategory(String category);

    List<ProductDTO> searchProducts(String name);

    ProductDTO updateProduct(String id, ProductDTO productDTO);

    void deleteProduct(String id);

    void decreaseProductQuantity(String  productId, Integer quantity);
}