package com.team23.global.seed;

import com.team23.order.delivery.domain.Address;
import com.team23.order.delivery.domain.Delivery;
import com.team23.order.delivery.domain.Receiver;
import com.team23.order.delivery.repository.DeliveryRepository;
import com.team23.order.domain.Order;
import com.team23.order.domain.OrderItem;
import com.team23.order.repository.OrderRepository;
import com.team23.category.domain.Category;
import com.team23.product.domain.Product;
import com.team23.product.domain.Sku;
import com.team23.product.domain.SkuOption;
import com.team23.category.repository.CategoryRepository;
import com.team23.product.repository.ProductRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@Profile("perf")
@RequiredArgsConstructor
@ConditionalOnProperty(
        prefix = "app.seed.performance",
        name = "enabled",
        havingValue = "true"
)
public class PerformanceSeedRunner implements CommandLineRunner {

    private static final String SEED_MARKER_SLUG = "perf-category-001";
    private static final String ORDER_PRODUCT_NAME = "Perf Order Product";
    private static final int CATALOG_BATCH_SIZE = 500;
    private static final int CANCEL_ORDER_BATCH_SIZE = 200;

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final DeliveryRepository deliveryRepository;
    private final EntityManager entityManager;
    private final TransactionTemplate transactionTemplate;

    @Value("${app.seed.performance.category-count:30}")
    private int categoryCount;

    @Value("${app.seed.performance.product-count:10000}")
    private int productCount;

    @Value("${app.seed.performance.skus-per-product:3}")
    private int skusPerProduct;

    @Value("${app.seed.performance.order-sku-stock:1000000}")
    private int orderSkuStock;

    @Value("${app.seed.performance.cancel-order-count:1000}")
    private int cancelOrderCount;

    @Value("${app.seed.performance.cancel-member-id:1}")
    private Long cancelMemberId;

    @Override
    public void run(String... args) {
        validateConfig();

        if (categoryRepository.existsBySlug(SEED_MARKER_SLUG)) {
            log.info("Performance seed already exists. markerSlug={}", SEED_MARKER_SLUG);
            return;
        }

        log.info("Performance seed started: categories={}, products={}, skusPerProduct={}, cancelOrders={}",
                categoryCount, productCount, skusPerProduct, cancelOrderCount);

        List<Category> categories = transactionTemplate.execute(status -> seedCategories());
        seedCatalogProducts(categories);
        Product orderProduct = transactionTemplate.execute(status -> seedOrderTestProduct(categories.get(0)));
        seedCancelableOrders(orderProduct);

        log.info("Performance seed completed: orderProductId={}, orderSkuId={}, cancelMemberId={}",
                orderProduct.getId(),
                orderProduct.getSkuses().get(0).getId(),
                cancelMemberId);
    }

    private void validateConfig() {
        if (categoryCount <= 0) {
            throw new IllegalArgumentException(
                    "Invalid perf seed config: categoryCount must be > 0");
        }
        if (productCount <= 0) {
            throw new IllegalArgumentException(
                    "Invalid perf seed config: productCount must be > 0");
        }
        if (skusPerProduct <= 0) {
            throw new IllegalArgumentException(
                    "Invalid perf seed config: skusPerProduct must be > 0");
        }
        if (cancelOrderCount < 0) {
            throw new IllegalArgumentException(
                    "Invalid perf seed config: cancelOrderCount must be >= 0");
        }
        if (orderSkuStock < 0) {
            throw new IllegalArgumentException(
                    "Invalid perf seed config: orderSkuStock must be >= 0");
        }
        if (cancelMemberId == null || cancelMemberId <= 0) {
            throw new IllegalArgumentException(
                    "Invalid perf seed config: cancelMemberId must be > 0");
        }
    }

    private List<Category> seedCategories() {
        List<Category> categories = new ArrayList<>();

        for (int i = 1; i <= categoryCount; i++) {
            Category category = Category.create(
                    "Perf Category " + format(i),
                    "perf-category-" + format(i)
            );
            categories.add(categoryRepository.save(category));
        }

        return categories;
    }

    private void seedCatalogProducts(List<Category> categories) {
        for (int start = 1; start <= productCount; start += CATALOG_BATCH_SIZE) {
            final int batchStart = start;
            final int batchEnd = Math.min(start + CATALOG_BATCH_SIZE - 1, productCount);
            int processed = batchEnd;

            transactionTemplate.executeWithoutResult(status -> {
                for (int i = batchStart; i <= batchEnd; i++) {
                    Category category = categories.get((i - 1) % categories.size());
                    Product product = Product.register(
                            catalogProductName(i),
                            BigDecimal.valueOf(5_000 + (i % 296) * 1_000),
                            catalogDescription(i),
                            "https://example.com/perf/products/" + i + ".jpg",
                            category
                    );

                    productRepository.save(product);
                    addCatalogSkus(product, i);

                    if (i % 20 == 0) {
                        product.discontinue();
                    }
                }

                productRepository.flush();
                entityManager.clear();
            });

            if (processed % CATALOG_BATCH_SIZE == 0 || processed == productCount) {
                log.info("Performance catalog seed progress: {}/{} products",
                        processed, productCount);
            }
        }
    }

    private Product seedOrderTestProduct(Category category) {
        Product product = Product.register(
                ORDER_PRODUCT_NAME,
                BigDecimal.valueOf(29_900),
                "Performance test product for order-create scenario",
                "https://example.com/perf/order-product.jpg",
                category
        );
        productRepository.saveAndFlush(product);
        product.addSku(List.of(
                new SkuOption("color", "black"),
                new SkuOption("size", "free")
        ), orderSkuStock);

        return productRepository.saveAndFlush(product);
    }

    private void seedCancelableOrders(Product product) {
        Sku sku = product.getSkuses().get(0);

        for (int start = 1; start <= cancelOrderCount; start += CANCEL_ORDER_BATCH_SIZE) {
            final int batchStart = start;
            final int batchEnd = Math.min(start + CANCEL_ORDER_BATCH_SIZE - 1, cancelOrderCount);
            int processed = batchEnd;

            transactionTemplate.executeWithoutResult(status -> {
                for (int i = batchStart; i <= batchEnd; i++) {
                    Order order = Order.createForMember(
                            cancelMemberId,
                            List.of(OrderItem.of(product, sku, 1))
                    );
                    orderRepository.save(order);

                    Delivery delivery = Delivery.prepare(
                            order.getId(),
                            new Address("12345", "서울시 강남구 성능테스트로", i + "호"),
                            new Receiver("perf-cancel-user", "010-0000-0000"),
                            "performance cancel seed"
                    );
                    deliveryRepository.save(delivery);
                }

                orderRepository.flush();
                deliveryRepository.flush();
                entityManager.clear();
            });

            if (processed % CANCEL_ORDER_BATCH_SIZE == 0 || processed == cancelOrderCount) {
                log.info("Performance cancel-order seed progress: {}/{} orders",
                        processed, cancelOrderCount);
            }
        }
    }

    private void addCatalogSkus(Product product, int productIndex) {
        for (int skuIndex = 1; skuIndex <= skusPerProduct; skuIndex++) {
            int stock = stockFor(productIndex, skuIndex);
            product.addSku(List.of(
                    new SkuOption("color", colorFor(productIndex + skuIndex)),
                    new SkuOption("size", sizeFor(skuIndex))
            ), stock);
        }
    }

    private int stockFor(int productIndex, int skuIndex) {
        if (productIndex % 10 == 0) {
            return 0;
        }
        return 10 + ((productIndex + skuIndex) % 90);
    }

    private String catalogProductName(int index) {
        String[] types = {"T-Shirt", "Shoes", "Bag", "Pants", "Jacket"};
        return "Perf " + types[index % types.length] + " " + format(index);
    }

    private String catalogDescription(int index) {
        String[] keywords = {"cotton", "denim", "running", "winter", "premium"};
        return "Performance seed product " + index
                + " keyword=" + keywords[index % keywords.length]
                + " category-load-test";
    }

    private String colorFor(int index) {
        String[] colors = {"black", "white", "navy", "gray", "green"};
        return colors[index % colors.length];
    }

    private String sizeFor(int index) {
        String[] sizes = {"S", "M", "L", "XL"};
        return sizes[index % sizes.length];
    }

    private String format(int value) {
        return String.format("%03d", value);
    }
}
