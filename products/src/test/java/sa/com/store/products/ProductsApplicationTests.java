package sa.com.store.products;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
        "ENV=test"
})
class ProductsApplicationTests {

	@Test
	void contextLoads() {
	}

}
