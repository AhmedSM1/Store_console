package sa.com.store.products.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import sa.com.store.products.controller.dto.DiscountDto;

import sa.com.store.products.entity.Category;
import sa.com.store.products.entity.ProductOrder;
import sa.com.store.products.model.UserDTO;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class DiscountServiceTest {

    @InjectMocks
    private DiscountService discountService;



    private List<ProductOrder> testItems;
    private UserDTO regularCustomer;
    private UserDTO employeeUser;
    private UserDTO affiliateUser;
    private UserDTO longTermCustomer;

    @BeforeEach
    void setUp() {
        testItems = new ArrayList<>();

        regularCustomer = UserDTO.builder()
                .username("regular_customer")
                .email("regular@example.com")
                .role("ROLE_CUSTOMER")
                .isAffiliate(false)
                .creationTime(OffsetDateTime.now())
                .build();

        employeeUser = UserDTO.builder()
                .username("employee_user")
                .email("employee@example.com")
                .role("ROLE_MANAGER")
                .isAffiliate(false)
                .creationTime(OffsetDateTime.now())
                .build();

        affiliateUser = UserDTO.builder()
                .username("affiliate_user")
                .email("affiliate@example.com")
                .role("ROLE_CUSTOMER")
                .isAffiliate(true)
                .creationTime(OffsetDateTime.now())
                .build();

        longTermCustomer = UserDTO.builder()
                .username("long_term_customer")
                .email("longterm@example.com")
                .role("ROLE_CUSTOMER")
                .isAffiliate(false)
                .creationTime(OffsetDateTime.now().minusYears(3))
                .build();
    }

    @Nested
    @DisplayName("Employee Discount Tests (30%)")
    class EmployeeDiscountTests {

        @Test
        @DisplayName("Employee discount should be 30% on non-grocery items")
        void testEmployeeDiscountPercentageNonGrocery() {
            // Arrange
            ProductOrder electronics = ProductOrder.builder()
                    .productId("prod-1")
                    .quantity(1)
                    .unitPrice(BigDecimal.valueOf(100.0))
                    .totalPrice(BigDecimal.valueOf(100.0))
                    .category(Category.electronics)
                    .build();
            testItems.add(electronics);

            // Act
            DiscountDto result = discountService.calculateFinalPrice(testItems, employeeUser);

            // Assert
            assertEquals("100.0", result.priceBeforeDiscount());
            assertEquals("Employee Discount (30%)", result.discountType());
            assertTrue(Double.parseDouble(result.priceAfterDiscount()) < 100.0);
        }

        @Test
        @DisplayName("Employee discount should exclude grocery items from percentage discount")
        void testEmployeeDiscountExcludesGrocery() {
            // Arrange - 90 electronics + 10 grocery
            ProductOrder electronics = ProductOrder.builder()
                    .productId("prod-1")
                    .quantity(1)
                    .unitPrice(BigDecimal.valueOf(90.0))
                    .totalPrice(BigDecimal.valueOf(90.0))
                    .category(Category.electronics)
                    .build();

            ProductOrder grocery = ProductOrder.builder()
                    .productId("prod-2")
                    .quantity(1)
                    .unitPrice(BigDecimal.valueOf(10.0))
                    .totalPrice(BigDecimal.valueOf(10.0))
                    .category(Category.groceries)
                    .build();

            testItems.add(electronics);
            testItems.add(grocery);

            // Act
            DiscountDto result = discountService.calculateFinalPrice(testItems, employeeUser);

            // Assert - Only 90 is eligible for 30% discount
            assertEquals("100.0", result.priceBeforeDiscount());
            assertEquals("Employee Discount (30%)", result.discountType());
            assertEquals("73.000", result.priceAfterDiscount());
            assertEquals("27.000", result.discountAmount());
        }

        @Test
        @DisplayName("Employee with 990 should get 327 discount (297 percentage + 30 flat)")
        void testEmployeeDiscountWith990Amount() {
            // Arrange - 990 in electronics
            ProductOrder electronics = ProductOrder.builder()
                    .productId("prod-1")
                    .quantity(1)
                    .unitPrice(BigDecimal.valueOf(990.0))
                    .totalPrice(BigDecimal.valueOf(990.0))
                    .category(Category.electronics)
                    .build();
            testItems.add(electronics);

            // Act
            DiscountDto result = discountService.calculateFinalPrice(testItems, employeeUser);

            // Assert - 990 * 0.30 = 297
            // 990 - 297 = 693
            // floor(693/100) = 6, 6 * 5 = 30
            // 693 - 30 = 663
            assertEquals("990.0", result.priceBeforeDiscount());
            assertEquals("Employee Discount (30%)", result.discountType());
            BigDecimal discountAmount = new BigDecimal(result.discountAmount());
            assertEquals(new BigDecimal("327.000"), discountAmount); // 297 + 30
            assertEquals("663.000", result.priceAfterDiscount());
        }

        @Test
        @DisplayName("Employee with multiple items should calculate correctly")
        void testEmployeeDiscountMultipleItems() {
            // Arrange - 3 items totaling 300
            ProductOrder electronics1 = ProductOrder.builder()
                    .productId("prod-1")
                    .quantity(2)
                    .unitPrice(BigDecimal.valueOf(100.0))
                    .totalPrice(BigDecimal.valueOf(200.0))
                    .category(Category.electronics)
                    .build();

            ProductOrder electronics2 = ProductOrder.builder()
                    .productId("prod-2")
                    .quantity(1)
                    .unitPrice(BigDecimal.valueOf(100.0))
                    .totalPrice(BigDecimal.valueOf(100.0))
                    .category(Category.furniture)
                    .build();

            testItems.add(electronics1);
            testItems.add(electronics2);

            // Act
            DiscountDto result = discountService.calculateFinalPrice(testItems, employeeUser);

            // Assert - 300 total, 30% = 90 discount, then floor(210/100)*5 = 10
            assertEquals("300.0", result.priceBeforeDiscount());
            assertEquals("Employee Discount (30%)", result.discountType());
            BigDecimal discountAmount = new BigDecimal(result.discountAmount());
            assertEquals(new BigDecimal("100.000"), discountAmount); // 90 + 10
        }
    }

    @Nested
    @DisplayName("Affiliate Discount Tests (10%)")
    class AffiliateDiscountTests {

        @Test
        @DisplayName("Affiliate discount should be 10% on non-grocery items")
        void testAffiliateDiscountPercentageNonGrocery() {
            // Arrange - Electronics item: 100
            ProductOrder electronics = ProductOrder.builder()
                    .productId("prod-1")
                    .quantity(1)
                    .unitPrice(BigDecimal.valueOf(100.0))
                    .totalPrice(BigDecimal.valueOf(100.0))
                    .category(Category.electronics)
                    .build();
            testItems.add(electronics);

            // Act
            DiscountDto result = discountService.calculateFinalPrice(testItems, affiliateUser);

            // Assert - 10% of 100 = 10, then flat discount
            assertEquals("100.0", result.priceBeforeDiscount());
            assertEquals("Affiliate Discount (10%)", result.discountType());
            assertEquals("90.000", result.priceAfterDiscount());
            assertEquals("10.000", result.discountAmount());
        }

        @Test
        @DisplayName("Affiliate discount should exclude grocery items")
        void testAffiliateDiscountExcludesGrocery() {
            // Arrange - 50 electronics + 50 grocery
            ProductOrder electronics = ProductOrder.builder()
                    .productId("prod-1")
                    .quantity(1)
                    .unitPrice(BigDecimal.valueOf(50.0))
                    .totalPrice(BigDecimal.valueOf(50.0))
                    .category(Category.electronics)
                    .build();

            ProductOrder grocery = ProductOrder.builder()
                    .productId("prod-2")
                    .quantity(1)
                    .unitPrice(BigDecimal.valueOf(50.0))
                    .totalPrice(BigDecimal.valueOf(50.0))
                    .category(Category.groceries)
                    .build();

            testItems.add(electronics);
            testItems.add(grocery);

            // Act
            DiscountDto result = discountService.calculateFinalPrice(testItems, affiliateUser);

            // Assert - Only 50 is eligible for 10% discount
            assertEquals("100.0", result.priceBeforeDiscount());
            assertEquals("Affiliate Discount (10%)", result.discountType());
            assertEquals("95.000", result.priceAfterDiscount());
            assertEquals("5.000", result.discountAmount());
        }

        @Test
        @DisplayName("Affiliate with 990 should get 139 discount (99 percentage + 40 flat)")
        void testAffiliateDiscountWith990Amount() {
            // Arrange - 990 in electronics
            ProductOrder electronics = ProductOrder.builder()
                    .productId("prod-1")
                    .quantity(1)
                    .unitPrice(BigDecimal.valueOf(990.0))
                    .totalPrice(BigDecimal.valueOf(990.0))
                    .category(Category.electronics)
                    .build();
            testItems.add(electronics);

            // Act
            DiscountDto result = discountService.calculateFinalPrice(testItems, affiliateUser);

            // Assert - 990 * 0.10 = 99
            // 990 - 99 = 891
            // floor(891/100) = 8, 8 * 5 = 40
            // 891 - 40 = 851
            assertEquals("990.0", result.priceBeforeDiscount());
            assertEquals("Affiliate Discount (10%)", result.discountType());
            BigDecimal discountAmount = new BigDecimal(result.discountAmount());
            assertEquals(new BigDecimal("139.000"), discountAmount); // 99 + 40
            assertEquals("851.000", result.priceAfterDiscount());
        }
    }

    @Nested
    @DisplayName("Long-Term Customer Loyalty Discount Tests (5%)")
    class LongTermCustomerTests {

        @Test
        @DisplayName("Long-term customer (2+ years) should get 5% loyalty discount")
        void testLongTermCustomerLoyaltyDiscount() {
            // Arrange - Electronics item: 100
            ProductOrder electronics = ProductOrder.builder()
                    .productId("prod-1")
                    .quantity(1)
                    .unitPrice(BigDecimal.valueOf(100.0))
                    .totalPrice(BigDecimal.valueOf(100.0))
                    .category(Category.electronics)
                    .build();
            testItems.add(electronics);

            // Act
            DiscountDto result = discountService.calculateFinalPrice(testItems, longTermCustomer);

            // Assert - 5% of 100 = 5
            assertEquals("100.0", result.priceBeforeDiscount());
            assertEquals("Loyalty Discount (5%)", result.discountType());
            assertEquals("95.000", result.priceAfterDiscount());
            assertEquals("5.000", result.discountAmount());
        }

        @Test
        @DisplayName("Long-term customer with 990 should get 94.5 discount (49.5 percentage + 45 flat)")
        void testLongTermCustomerDiscountWith990Amount() {
            // Arrange - 990 in electronics
            ProductOrder electronics = ProductOrder.builder()
                    .productId("prod-1")
                    .quantity(1)
                    .unitPrice(BigDecimal.valueOf(990.0))
                    .totalPrice(BigDecimal.valueOf(990.0))
                    .category(Category.electronics)
                    .build();
            testItems.add(electronics);

            // Act
            DiscountDto result = discountService.calculateFinalPrice(testItems, longTermCustomer);

            // Assert - 990 * 0.05 = 49.50
            // 990 - 49.50 = 940.50
            // floor(940.50/100) = 9, 9 * 5 = 45
            // 940.50 - 45 = 895.50
            assertEquals("990.0", result.priceBeforeDiscount());
            assertEquals("Loyalty Discount (5%)", result.discountType());
            BigDecimal discountAmount = new BigDecimal(result.discountAmount());
            assertEquals(new BigDecimal("94.500"), discountAmount);
        }

        @Test
        @DisplayName("Long-term customer discount excludes grocery items")
        void testLongTermCustomerExcludesGrocery() {
            // Arrange - 80 furniture + 20 grocery
            ProductOrder furniture = ProductOrder.builder()
                    .productId("prod-1")
                    .quantity(1)
                    .unitPrice(BigDecimal.valueOf(80.0))
                    .totalPrice(BigDecimal.valueOf(80.0))
                    .category(Category.furniture)
                    .build();

            ProductOrder grocery = ProductOrder.builder()
                    .productId("prod-2")
                    .quantity(1)
                    .unitPrice(BigDecimal.valueOf(20.0))
                    .totalPrice(BigDecimal.valueOf(20.0))
                    .category(Category.groceries)
                    .build();

            testItems.add(furniture);
            testItems.add(grocery);

            // Act
            DiscountDto result = discountService.calculateFinalPrice(testItems, longTermCustomer);

            // Assert - Only 80 is eligible for 5% discount
            assertEquals("100.0", result.priceBeforeDiscount());
            assertEquals("Loyalty Discount (5%)", result.discountType());
            assertEquals("96.000", result.priceAfterDiscount());
            assertEquals("4.000", result.discountAmount());
        }
    }

    @Nested
    @DisplayName("Regular Customer Discount Tests")
    class RegularCustomerTests {

        @Test
        @DisplayName("Regular customer should get no percentage discount but flat discount")
        void testRegularCustomerFlatDiscountOnly() {
            // Arrange - Electronics item: 100
            ProductOrder electronics = ProductOrder.builder()
                    .productId("prod-1")
                    .quantity(1)
                    .unitPrice(BigDecimal.valueOf(100.0))
                    .totalPrice(BigDecimal.valueOf(100.0))
                    .category(Category.electronics)
                    .build();
            testItems.add(electronics);

            // Act
            DiscountDto result = discountService.calculateFinalPrice(testItems, regularCustomer);

            // Assert - No percentage, only flat: floor(100/100)*5 = 5
            assertEquals("100.0", result.priceBeforeDiscount());
            assertEquals("Regular discount", result.discountType());
            assertEquals("95.0", result.priceAfterDiscount());
            assertEquals("5.0", result.discountAmount());
        }

        @Test
        @DisplayName("Regular customer with 990 should get 45 flat discount (9*5)")
        void testRegularCustomerWith990Amount() {
            // Arrange - 990 in electronics
            ProductOrder electronics = ProductOrder.builder()
                    .productId("prod-1")
                    .quantity(1)
                    .unitPrice(BigDecimal.valueOf(990.0))
                    .totalPrice(BigDecimal.valueOf(990.0))
                    .category(Category.electronics)
                    .build();
            testItems.add(electronics);

            // Act
            DiscountDto result = discountService.calculateFinalPrice(testItems, regularCustomer);

            // Assert - floor(990/100) = 9, 9 * 5 = 45
            assertEquals("990.0", result.priceBeforeDiscount());
            assertEquals("Regular discount", result.discountType());
            assertEquals("945.0", result.priceAfterDiscount());
            assertEquals("45.0", result.discountAmount());
        }

        @Test
        @DisplayName("Regular customer with mixed items (groceries not discounted)")
        void testRegularCustomerMixedItems() {
            // Arrange - 50 electronics + 50 grocery
            ProductOrder electronics = ProductOrder.builder()
                    .productId("prod-1")
                    .quantity(1)
                    .unitPrice(BigDecimal.valueOf(50.0))
                    .totalPrice(BigDecimal.valueOf(50.0))
                    .category(Category.electronics)
                    .build();

            ProductOrder grocery = ProductOrder.builder()
                    .productId("prod-2")
                    .quantity(1)
                    .unitPrice(BigDecimal.valueOf(50.0))
                    .totalPrice(BigDecimal.valueOf(50.0))
                    .category(Category.groceries)
                    .build();

            testItems.add(electronics);
            testItems.add(grocery);

            // Act
            DiscountDto result = discountService.calculateFinalPrice(testItems, regularCustomer);

            // Assert - Total 100, no percentage, floor(100/100)*5 = 5
            assertEquals("100.0", result.priceBeforeDiscount());
            assertEquals("Regular discount", result.discountType());
            assertEquals("95.0", result.priceAfterDiscount());
            assertEquals("5.0", result.discountAmount());
        }
    }

    @Nested
    @DisplayName("Grocery Item Exclusion Tests")
    class GroceryExclusionTests {

        @Test
        @DisplayName("Grocery items should NOT be included in percentage discount calculation")
        void testGroceryNotIncludedInPercentageDiscount() {
            // Arrange - 100 grocery items only
            ProductOrder grocery = ProductOrder.builder()
                    .productId("prod-1")
                    .quantity(1)
                    .unitPrice(BigDecimal.valueOf(100.0))
                    .totalPrice(BigDecimal.valueOf(100.0))
                    .category(Category.groceries)
                    .build();
            testItems.add(grocery);

            // Act
            DiscountDto result = discountService.calculateFinalPrice(testItems, employeeUser);

            // Assert - No percentage discount on groceries, only flat
            assertEquals("100.0", result.priceBeforeDiscount());
            assertEquals("95.00", result.priceAfterDiscount());
            assertEquals("5.00", result.discountAmount());
        }

        @Test
        @DisplayName("Only non-grocery items should be used for percentage discount")
        void testOnlyNonGroceryForPercentageDiscount() {
            // Arrange - 60 clothes + 40 grocery
            ProductOrder clothes = ProductOrder.builder()
                    .productId("prod-1")
                    .quantity(1)
                    .unitPrice(BigDecimal.valueOf(60.0))
                    .totalPrice(BigDecimal.valueOf(60.0))
                    .category(Category.clothes)
                    .build();

            ProductOrder grocery = ProductOrder.builder()
                    .productId("prod-2")
                    .quantity(1)
                    .unitPrice(BigDecimal.valueOf(40.0))
                    .totalPrice(BigDecimal.valueOf(40.0))
                    .category(Category.groceries)
                    .build();

            testItems.add(clothes);
            testItems.add(grocery);

            // Act
            DiscountDto result = discountService.calculateFinalPrice(testItems, affiliateUser);

            // Assert - Only 60 eligible for 10% discount
            assertEquals("100.0", result.priceBeforeDiscount());
            assertEquals("94.000", result.priceAfterDiscount());
            assertEquals("6.000", result.discountAmount());
        }
    }

    @Nested
    @DisplayName("Flat Discount Calculation Tests (5 for every 100)")
    class FlatDiscountTests {

        @ParameterizedTest
        @ValueSource(ints = {99, 100, 199, 200, 299, 300, 500, 990, 1000, 1500})
        @DisplayName("Flat discount should be 5 for every 100 (floor division) - Parameterized")
        void testFlatDiscountCalculationParameterized(int amount) {
            // Arrange
            testItems.clear();
            ProductOrder electronics = ProductOrder.builder()
                    .productId("prod-1")
                    .quantity(1)
                    .unitPrice(BigDecimal.valueOf(amount))
                    .totalPrice(BigDecimal.valueOf(amount))
                    .category(Category.electronics)
                    .build();
            testItems.add(electronics);

            // Act
            DiscountDto result = discountService.calculateFinalPrice(testItems, regularCustomer);

            // Assert - Calculate expected discount
            int expectedFloorDivisor = amount / 100;
            int expectedDiscount = expectedFloorDivisor * 5;
            int expectedFinalPrice = amount - expectedDiscount;

            assertEquals(String.valueOf(amount), result.priceBeforeDiscount());
            assertEquals(String.valueOf(expectedFinalPrice), result.priceAfterDiscount());
            assertEquals(String.valueOf(expectedDiscount), result.discountAmount());
        }

        @Test
        @DisplayName("Flat discount with 99 should be 0 ")
        void testFlatDiscountWith99() {
            // Arrange - 99 in electronics
            ProductOrder electronics = ProductOrder.builder()
                    .productId("prod-1")
                    .quantity(1)
                    .unitPrice(BigDecimal.valueOf(99.0))
                    .totalPrice(BigDecimal.valueOf(99.0))
                    .category(Category.electronics)
                    .build();
            testItems.add(electronics);

            // Act
            DiscountDto result = discountService.calculateFinalPrice(testItems, regularCustomer);

            // Assert - floor(99/100) = 0, 0 * 5 = 0
            assertEquals("99.0", result.priceBeforeDiscount());
            assertEquals("99.0", result.priceAfterDiscount());
            assertEquals("0.0", result.discountAmount());
        }

        @Test
        @DisplayName("Flat discount with 199 should be 5 ")
        void testFlatDiscountWith199() {
            // Arrange - 199 in electronics
            ProductOrder electronics = ProductOrder.builder()
                    .productId("prod-1")
                    .quantity(1)
                    .unitPrice(BigDecimal.valueOf(199.0))
                    .totalPrice(BigDecimal.valueOf(199.0))
                    .category(Category.electronics)
                    .build();
            testItems.add(electronics);

            // Act
            DiscountDto result = discountService.calculateFinalPrice(testItems, regularCustomer);

            // Assert - floor(199/100) = 1, 1 * 5 = 5
            assertEquals("199.0", result.priceBeforeDiscount());
            assertEquals("194.0", result.priceAfterDiscount());
            assertEquals("5.0", result.discountAmount());
        }

        @Test
        @DisplayName("Flat discount with 1500 should be 75 ")
        void testFlatDiscountWith1500() {
            // Arrange - 1500 in electronics
            ProductOrder electronics = ProductOrder.builder()
                    .productId("prod-1")
                    .quantity(1)
                    .unitPrice(BigDecimal.valueOf(1500.0))
                    .totalPrice(BigDecimal.valueOf(1500.0))
                    .category(Category.electronics)
                    .build();
            testItems.add(electronics);

            // Act
            DiscountDto result = discountService.calculateFinalPrice(testItems, regularCustomer);

            // Assert - floor(1500/100) = 15, 15 * 5 = 75
            assertEquals("1500.0", result.priceBeforeDiscount());
            assertEquals("1425.0", result.priceAfterDiscount());
            assertEquals("75.0", result.discountAmount());
        }
    }

    @Nested
    @DisplayName("Priority Order Tests")
    class PriorityOrderTests {

        @Test
        @DisplayName("Employee discount should take priority over affiliate and loyalty")
        void testEmployeeDiscountPriority() {
            // Arrange - User with MANAGER role (employee) who is also affiliate
            UserDTO multiRoleUser = UserDTO.builder()
                    .username("multi_role")
                    .email("multi@example.com")
                    .role("ROLE_MANAGER")
                    .isAffiliate(true)
                    .creationTime(OffsetDateTime.now().minusYears(3))
                    .build();

            ProductOrder electronics = ProductOrder.builder()
                    .productId("prod-1")
                    .quantity(1)
                    .unitPrice(BigDecimal.valueOf(100.0))
                    .totalPrice(BigDecimal.valueOf(100.0))
                    .category(Category.electronics)
                    .build();
            testItems.add(electronics);

            // Act
            DiscountDto result = discountService.calculateFinalPrice(testItems, multiRoleUser);

            // Assert - Should use 30% employee discount, not 10% affiliate
            assertEquals("Employee Discount (30%)", result.discountType());
        }

        @Test
        @DisplayName("Affiliate discount should take priority over loyalty - ONLY ONE DISCOUNT APPLIES")
        void testAffiliateDiscountPriority() {
            // Arrange - User who is affiliate AND long-term customer
            UserDTO affiliateLongTerm = UserDTO.builder()
                    .username("affiliate_long_term")
                    .email("affiliatelt@example.com")
                    .role("ROLE_CUSTOMER")
                    .isAffiliate(true)
                    .creationTime(OffsetDateTime.now().minusYears(3))
                    .build();

            ProductOrder electronics = ProductOrder.builder()
                    .productId("prod-1")
                    .quantity(1)
                    .unitPrice(BigDecimal.valueOf(100.0))
                    .totalPrice(BigDecimal.valueOf(100.0))
                    .category(Category.electronics)
                    .build();
            testItems.add(electronics);

            // Act
            DiscountDto result = discountService.calculateFinalPrice(testItems, affiliateLongTerm);

            // Assert - Should use 10% affiliate discount, NOT 5% loyalty
            // This verifies that only ONE discount type applies
            assertEquals("Affiliate Discount (10%)", result.discountType());
            assertEquals("100.0", result.priceBeforeDiscount());
            // 100 - (100 * 0.10) = 90, floor(90/100)*5 = 0
            assertEquals("90.000", result.priceAfterDiscount());
            assertEquals("10.000", result.discountAmount());
        }

        @Test
        @DisplayName("CRITICAL: Affiliate (2+ years) should NOT get loyalty discount, ONLY affiliate discount")
        void testAffiliateDoesNotGetLoyaltyDiscount() {
            // Arrange - Affiliate customer with 2+ years registration
            UserDTO affiliateLongTerm = UserDTO.builder()
                    .username("affiliate_2plus_years")
                    .email("affiliatelt2@example.com")
                    .role("ROLE_CUSTOMER")
                    .isAffiliate(true)
                    .creationTime(OffsetDateTime.now().minusYears(5)) // 5 years old
                    .build();

            ProductOrder item = ProductOrder.builder()
                    .productId("prod-1")
                    .quantity(1)
                    .unitPrice(BigDecimal.valueOf(1000.0))
                    .totalPrice(BigDecimal.valueOf(1000.0))
                    .category(Category.electronics)
                    .build();
            testItems.add(item);

            // Act
            DiscountDto result = discountService.calculateFinalPrice(testItems, affiliateLongTerm);

            // Assert - Should use 10% affiliate (100), NOT 5% loyalty (50)
            assertEquals("Affiliate Discount (10%)", result.discountType());
            // 1000 - (1000 * 0.10) = 900, floor(900/100)*5 = 45
            // Total discount = 100 + 45 = 145
            assertEquals("145.000", result.discountAmount());
            assertEquals("855.000", result.priceAfterDiscount());
        }
    }

    @Nested
    @DisplayName("Edge Cases & Complex Scenarios")
    class EdgeCasesTests {

        @Test
        @DisplayName("Empty items list should result in zero price")
        void testEmptyItemsList() {
            // Arrange - Empty list
            testItems = Collections.emptyList();

            // Act
            DiscountDto result = discountService.calculateFinalPrice(testItems, regularCustomer);

            // Assert
            assertEquals("0", result.priceBeforeDiscount());
            assertEquals("0", result.priceAfterDiscount());
            assertEquals("Regular discount", result.discountType());
            assertEquals("0", result.discountAmount());
        }

        @Test
        @DisplayName("Single item with zero quantity should result in zero price")
        void testZeroQuantityItem() {
            // Arrange - Item with zero quantity
            ProductOrder electronics = ProductOrder.builder()
                    .productId("prod-1")
                    .quantity(0)
                    .unitPrice(BigDecimal.valueOf(100.0))
                    .totalPrice(BigDecimal.ZERO)
                    .category(Category.electronics)
                    .build();
            testItems.add(electronics);

            // Act
            DiscountDto result = discountService.calculateFinalPrice(testItems, employeeUser);

            // Assert
            assertEquals("0.0", result.priceBeforeDiscount());
            assertEquals("0.000", result.priceAfterDiscount());
            assertEquals("Employee Discount (30%)", result.discountType());
        }

        @Test
        @DisplayName("Discount should never be negative")
        void testDiscountNeverNegative() {
            // Arrange - Price that could theoretically go negative
            ProductOrder item = ProductOrder.builder()
                    .productId("prod-1")
                    .quantity(1)
                    .unitPrice(BigDecimal.valueOf(1.0))
                    .totalPrice(BigDecimal.valueOf(1.0))
                    .category(Category.electronics)
                    .build();
            testItems.add(item);

            // Act
            DiscountDto result = discountService.calculateFinalPrice(testItems, employeeUser);

            // Assert - Should not be negative
            BigDecimal finalPrice = new BigDecimal(result.priceAfterDiscount());
            assertTrue(finalPrice.compareTo(BigDecimal.ZERO) >= 0);
        }
    }

    @Nested
    @DisplayName("Complex Multi-Category Order Tests")
    class ComplexOrderTests {

        @Test
        @DisplayName("Complex order with multiple categories and employee discount")
        void testComplexOrderEmployeeDiscount() {
            // Arrange - Multiple categories: 100 electronics, 200 furniture, 50 clothes, 50 grocery
            testItems.add(ProductOrder.builder()
                    .productId("prod-1").quantity(1)
                    .unitPrice(BigDecimal.valueOf(100.0))
                    .totalPrice(BigDecimal.valueOf(100.0))
                    .category(Category.electronics).build());

            testItems.add(ProductOrder.builder()
                    .productId("prod-2").quantity(1)
                    .unitPrice(BigDecimal.valueOf(200.0))
                    .totalPrice(BigDecimal.valueOf(200.0))
                    .category(Category.furniture).build());

            testItems.add(ProductOrder.builder()
                    .productId("prod-3").quantity(1)
                    .unitPrice(BigDecimal.valueOf(50.0))
                    .totalPrice(BigDecimal.valueOf(50.0))
                    .category(Category.clothes).build());

            testItems.add(ProductOrder.builder()
                    .productId("prod-4").quantity(1)
                    .unitPrice(BigDecimal.valueOf(50.0))
                    .totalPrice(BigDecimal.valueOf(50.0))
                    .category(Category.groceries).build());

            // Act
            DiscountDto result = discountService.calculateFinalPrice(testItems, employeeUser);

            // Assert - Total 400, non-grocery 350
            assertEquals("400.0", result.priceBeforeDiscount());
            assertEquals("Employee Discount (30%)", result.discountType());
            BigDecimal discountAmount = new BigDecimal(result.discountAmount());
            assertEquals(new BigDecimal("115.000"), discountAmount);
            assertEquals("285.000", result.priceAfterDiscount());
        }

        @Test
        @DisplayName("Complex order with multiple categories and affiliate discount")
        void testComplexOrderAffiliateDiscount() {
            // Arrange - 150 electronics, 150 furniture, 100 grocery
            testItems.add(ProductOrder.builder()
                    .productId("prod-1").quantity(1)
                    .unitPrice(BigDecimal.valueOf(150.0))
                    .totalPrice(BigDecimal.valueOf(150.0))
                    .category(Category.electronics).build());

            testItems.add(ProductOrder.builder()
                    .productId("prod-2").quantity(1)
                    .unitPrice(BigDecimal.valueOf(150.0))
                    .totalPrice(BigDecimal.valueOf(150.0))
                    .category(Category.furniture).build());

            testItems.add(ProductOrder.builder()
                    .productId("prod-3").quantity(1)
                    .unitPrice(BigDecimal.valueOf(100.0))
                    .totalPrice(BigDecimal.valueOf(100.0))
                    .category(Category.groceries).build());

            // Act
            DiscountDto result = discountService.calculateFinalPrice(testItems, affiliateUser);

            // Assert - Total 400, non-grocery 300
            assertEquals("400.0", result.priceBeforeDiscount());
            assertEquals("Affiliate Discount (10%)", result.discountType());
            BigDecimal discountAmount = new BigDecimal(result.discountAmount());
            assertEquals(new BigDecimal("45.000"), discountAmount);
            assertEquals("355.000", result.priceAfterDiscount());
        }

        @Test
        @DisplayName("Multiple quantities calculation")
        void testMultipleQuantitiesCalculation() {
            // Arrange - 5 items of 100 each = 500
            ProductOrder item = ProductOrder.builder()
                    .productId("prod-1")
                    .quantity(5)
                    .unitPrice(BigDecimal.valueOf(100.0))
                    .totalPrice(BigDecimal.valueOf(500.0))
                    .category(Category.electronics)
                    .build();
            testItems.add(item);

            // Act
            DiscountDto result = discountService.calculateFinalPrice(testItems, regularCustomer);

            // Assert - floor(500/100) = 5, 5 * 5 = 25
            assertEquals("500.0", result.priceBeforeDiscount());
            assertEquals("475.0", result.priceAfterDiscount());
            assertEquals("25.0", result.discountAmount());
        }
    }

    @Nested
    @DisplayName("Flat Discount Formula Verification (5 for every 100)")
    class FlatDiscountFormulaVerificationTests {

        @Test
        @DisplayName("VERIFY: For every 100, get exactly 5 discount (not 5%)")
        void testFlatDiscountFormula() {
            // Test cases to verify the formula: 5 for every 100 (not 5%)
            var testCases = List.of(
                    new Object[]{100, 5},      // 100 -> 5
                    new Object[]{200, 10},     // 200 -> 10
                    new Object[]{300, 15},     // 300 -> 15
                    new Object[]{500, 25},     // 500 -> 25
                    new Object[]{990, 45},     // 990 -> 45
                    new Object[]{1000, 50},    // 1000 -> 50
                    new Object[]{2000, 100}    // 2000 -> 100
            );

            for (Object[] testCase : testCases) {
                // Arrange
                int amount = (int) testCase[0];
                int expectedDiscount = (int) testCase[1];

                testItems.clear();
                ProductOrder item = ProductOrder.builder()
                        .productId("prod-1")
                        .quantity(1)
                        .unitPrice(BigDecimal.valueOf(amount))
                        .totalPrice(BigDecimal.valueOf(amount))
                        .category(Category.electronics)
                        .build();
                testItems.add(item);

                // Act
                DiscountDto result = discountService.calculateFinalPrice(testItems, regularCustomer);

                // Assert - Verify 5 for every 100
                assertEquals(String.valueOf(expectedDiscount), result.discountAmount(),
                        String.format("For amount %d, expected discount %d, but got %s",
                                amount, expectedDiscount, result.discountAmount()));
                assertEquals(String.valueOf(amount - expectedDiscount), result.priceAfterDiscount(),
                        String.format("Final price calculation incorrect for amount %d", amount));
            }
        }

        @Test
        @DisplayName("VERIFY: 990 amount should have exactly 45 flat discount (9 * 5, not 49.5)")
        void testVerify990FlatDiscount() {
            ProductOrder item = ProductOrder.builder()
                    .productId("prod-1")
                    .quantity(1)
                    .unitPrice(BigDecimal.valueOf(990.0))
                    .totalPrice(BigDecimal.valueOf(990.0))
                    .category(Category.electronics)
                    .build();
            testItems.add(item);

            // Act
            DiscountDto result = discountService.calculateFinalPrice(testItems, regularCustomer);

            // Assert - Must be 45 (9 * 5), NOT 49.5 (5% of 990)
            assertEquals("45.0", result.discountAmount(), "990 should get exactly 45 discount (9*5)");
            assertEquals("945.0", result.priceAfterDiscount());
        }
    }
}