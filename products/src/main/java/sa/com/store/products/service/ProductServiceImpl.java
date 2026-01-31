package sa.com.store.products.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sa.com.store.products.controller.dto.CreateProductRequest;
import sa.com.store.products.controller.dto.ProductDTO;
import sa.com.store.products.entity.Product;
import sa.com.store.products.mapper.ProductMapper;
import sa.com.store.products.repository.ProductRepository;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    @Override
    public ProductDTO createNewProduct(CreateProductRequest request) {
        ProductDTO productDTO = ProductDTO.builder()
                .name(request.name())
                .description(request.description())
                .price(request.price())
                .quantity(request.quantity())
                .category(request.category())
                .build();
        Product product = productMapper.toEntity(productDTO);
        Product savedProduct = productRepository.save(product);
        return productMapper.toDTO(savedProduct);
    }

    @Override
    public List<ProductDTO> getAllProducts() {
        return productRepository.findAll()
                .stream()
                .map(productMapper::toDTO)
                .toList();
    }

    @Override
    public ProductDTO getProductById(String id) {
        return productRepository.findById(id)
                .map(productMapper::toDTO).orElseThrow(() -> new IllegalArgumentException("Product not found: " + id));
    }

    @Override
    public List<ProductDTO> getAllProductsByCategory(String category) {
        return productRepository.findByCategory(category)
                .stream()
                .map(productMapper::toDTO)
                .toList();
    }

    @Override
    public List<ProductDTO> searchProducts(String name) {
        return productRepository.findByNameContainingIgnoreCase(name)
                .stream()
                .limit(20)
                .map(productMapper::toDTO)
                .toList();
    }

    @Override
    @Transactional
    public ProductDTO updateProduct(String id, ProductDTO productDTO) {
        return productRepository.findById(id)
                .map(existingProduct -> {
                    existingProduct.setName(productDTO.getName());
                    existingProduct.setDescription(productDTO.getDescription());
                    existingProduct.setPrice(productDTO.getPrice());
                    existingProduct.setQuantity(productDTO.getQuantity());
                    Product savedProduct = productRepository.save(existingProduct);
                    return productMapper.toDTO(savedProduct);
                })
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
    }

    @Override
    public void deleteProduct(String id) {
        productRepository.deleteById(id);
    }
}
