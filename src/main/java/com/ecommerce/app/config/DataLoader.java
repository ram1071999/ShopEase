package com.ecommerce.app.config;

import com.ecommerce.app.model.Product;
import com.ecommerce.app.model.User;
import com.ecommerce.app.repository.ProductRepository;
import com.ecommerce.app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {

    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        seedProducts();
        seedAdminUser();
    }

    /** Builds a product and links it to a dropshipping supplier in one call. */
    private Product product(String name, String description, BigDecimal sellingPrice, String imageUrl,
                             String category, int stock, double rating,
                             String supplierName, String supplierUrl, BigDecimal costPrice, String supplierSku) {
        Product p = new Product();
        p.setName(name);
        p.setDescription(description);
        p.setPrice(sellingPrice);
        p.setImageUrl(imageUrl);
        p.setCategory(category);
        p.setStockQuantity(stock);
        p.setRating(rating);
        p.setSupplierName(supplierName);
        p.setSupplierUrl(supplierUrl);
        p.setCostPrice(costPrice);
        p.setSupplierSku(supplierSku);
        return p;
    }

    private void seedProducts() {
        if (productRepository.count() > 0) return;

        // ---------- Electronics (sourced from AliExpress / CJ Dropshipping) ----------
        productRepository.save(product("Wireless Earbuds", "Bluetooth 5.3, noise cancelling, 24hr battery",
                new BigDecimal("1499"), "https://via.placeholder.com/300?text=Earbuds", "Electronics", 50, 4.3,
                "AliExpress", "https://www.aliexpress.com/item/example-earbuds", new BigDecimal("620"), "AE-EB-1001"));

        productRepository.save(product("Smart Watch", "Fitness tracker with heart rate monitor",
                new BigDecimal("2299"), "https://via.placeholder.com/300?text=Smart+Watch", "Electronics", 30, 4.1,
                "CJ Dropshipping", "https://cjdropshipping.com/product/example-smartwatch", new BigDecimal("980"), "CJ-SW-2044"));

        productRepository.save(product("Portable Speaker", "Waterproof, 12hr playtime, deep bass",
                new BigDecimal("1299"), "https://via.placeholder.com/300?text=Speaker", "Electronics", 25, 4.0,
                "AliExpress", "https://www.aliexpress.com/item/example-speaker", new BigDecimal("510"), "AE-SP-3092"));

        productRepository.save(product("Power Bank 20000mAh", "Fast charging, dual USB output, LED display",
                new BigDecimal("1199"), "https://via.placeholder.com/300?text=Power+Bank", "Electronics", 45, 4.2,
                "CJ Dropshipping", "https://cjdropshipping.com/product/example-powerbank", new BigDecimal("470"), "CJ-PB-1187"));

        // ---------- Fashion (sourced from Meesho / GlowRoad) ----------
        productRepository.save(product("Cotton T-Shirt", "100% cotton, regular fit, available in multiple colors",
                new BigDecimal("499"), "https://via.placeholder.com/300?text=T-Shirt", "Fashion", 100, 4.5,
                "Meesho", "https://www.meesho.com/product/example-tshirt", new BigDecimal("180"), "MS-TS-5501"));

        productRepository.save(product("Running Shoes", "Lightweight, breathable mesh, cushioned sole",
                new BigDecimal("1799"), "https://via.placeholder.com/300?text=Shoes", "Fashion", 40, 4.4,
                "GlowRoad", "https://glowroad.com/product/example-shoes", new BigDecimal("720"), "GR-SH-6620"));

        productRepository.save(product("Denim Jacket", "Classic fit, durable denim, unisex style",
                new BigDecimal("1999"), "https://via.placeholder.com/300?text=Denim+Jacket", "Fashion", 35, 4.3,
                "Meesho", "https://www.meesho.com/product/example-denim-jacket", new BigDecimal("850"), "MS-DJ-7712"));

        productRepository.save(product("Formal Shirt", "Wrinkle-free cotton blend, slim fit",
                new BigDecimal("899"), "https://via.placeholder.com/300?text=Formal+Shirt", "Fashion", 60, 4.1,
                "GlowRoad", "https://glowroad.com/product/example-formal-shirt", new BigDecimal("340"), "GR-FS-8834"));

        // ---------- Accessories ----------
        productRepository.save(product("Backpack", "Water-resistant, laptop compartment, 30L capacity",
                new BigDecimal("899"), "https://via.placeholder.com/300?text=Backpack", "Accessories", 60, 4.2,
                "AliExpress", "https://www.aliexpress.com/item/example-backpack", new BigDecimal("360"), "AE-BP-2210"));

        productRepository.save(product("Analog Wrist Watch", "Stainless steel strap, water resistant",
                new BigDecimal("1299"), "https://via.placeholder.com/300?text=Wrist+Watch", "Accessories", 30, 4.4,
                "CJ Dropshipping", "https://cjdropshipping.com/product/example-watch", new BigDecimal("510"), "CJ-WW-3341"));

        productRepository.save(product("Sunglasses", "UV400 protection, polarized lens",
                new BigDecimal("699"), "https://via.placeholder.com/300?text=Sunglasses", "Accessories", 50, 4.0,
                "Meesho", "https://www.meesho.com/product/example-sunglasses", new BigDecimal("240"), "MS-SG-4456"));

        productRepository.save(product("Leather Wallet", "Genuine leather, RFID blocking, slim design",
                new BigDecimal("599"), "https://via.placeholder.com/300?text=Wallet", "Accessories", 70, 4.3,
                "GlowRoad", "https://glowroad.com/product/example-wallet", new BigDecimal("210"), "GR-WL-5567"));

        // ---------- Home & Kitchen ----------
        productRepository.save(product("Non-Stick Cookware Set", "5-piece set, induction compatible",
                new BigDecimal("2499"), "https://via.placeholder.com/300?text=Cookware+Set", "Home & Kitchen", 20, 4.5,
                "CJ Dropshipping", "https://cjdropshipping.com/product/example-cookware", new BigDecimal("1050"), "CJ-CW-6678"));

        productRepository.save(product("Electric Kettle", "1.5L capacity, auto shut-off, stainless steel",
                new BigDecimal("799"), "https://via.placeholder.com/300?text=Electric+Kettle", "Home & Kitchen", 40, 4.2,
                "AliExpress", "https://www.aliexpress.com/item/example-kettle", new BigDecimal("320"), "AE-EK-7789"));

        productRepository.save(product("LED Table Lamp", "Adjustable brightness, USB rechargeable",
                new BigDecimal("649"), "https://via.placeholder.com/300?text=Table+Lamp", "Home & Kitchen", 35, 4.1,
                "AliExpress", "https://www.aliexpress.com/item/example-lamp", new BigDecimal("240"), "AE-LP-8890"));

        productRepository.save(product("Cotton Bedsheet Set", "Double bed, 2 pillow covers included",
                new BigDecimal("999"), "https://via.placeholder.com/300?text=Bedsheet", "Home & Kitchen", 25, 4.3,
                "Meesho", "https://www.meesho.com/product/example-bedsheet", new BigDecimal("410"), "MS-BS-9901"));

        // ---------- Books (self-sourced, no dropship supplier) ----------
        productRepository.save(product("The Art of Programming", "Beginner-friendly guide to coding concepts",
                new BigDecimal("399"), "https://via.placeholder.com/300?text=Programming+Book", "Books", 80, 4.6,
                null, null, new BigDecimal("180"), null));

        productRepository.save(product("Mystery Novel Collection", "Set of 3 bestselling thriller novels",
                new BigDecimal("599"), "https://via.placeholder.com/300?text=Novel+Set", "Books", 45, 4.4,
                null, null, new BigDecimal("270"), null));

        productRepository.save(product("Personal Finance 101", "Practical guide to saving and investing",
                new BigDecimal("349"), "https://via.placeholder.com/300?text=Finance+Book", "Books", 55, 4.2,
                null, null, new BigDecimal("150"), null));

        productRepository.save(product("Kids Story Book Set", "10 illustrated story books for children",
                new BigDecimal("449"), "https://via.placeholder.com/300?text=Kids+Books", "Books", 60, 4.5,
                null, null, new BigDecimal("190"), null));

        // ---------- Beauty ----------
        productRepository.save(product("Face Moisturizer", "For all skin types, 24hr hydration, 100g",
                new BigDecimal("349"), "https://via.placeholder.com/300?text=Moisturizer", "Beauty", 90, 4.3,
                "Meesho", "https://www.meesho.com/product/example-moisturizer", new BigDecimal("140"), "MS-FM-1123"));

        productRepository.save(product("Hair Dryer", "1800W, 3 heat settings, cool shot button",
                new BigDecimal("1099"), "https://via.placeholder.com/300?text=Hair+Dryer", "Beauty", 30, 4.1,
                "CJ Dropshipping", "https://cjdropshipping.com/product/example-hairdryer", new BigDecimal("430"), "CJ-HD-2234"));

        productRepository.save(product("Lipstick Combo Pack", "Set of 3 matte shades, long-lasting",
                new BigDecimal("599"), "https://via.placeholder.com/300?text=Lipstick+Combo", "Beauty", 65, 4.4,
                "GlowRoad", "https://glowroad.com/product/example-lipstick", new BigDecimal("220"), "GR-LP-3345"));

        productRepository.save(product("Perfume 100ml", "Long-lasting fragrance, unisex scent",
                new BigDecimal("899"), "https://via.placeholder.com/300?text=Perfume", "Beauty", 40, 4.2,
                "AliExpress", "https://www.aliexpress.com/item/example-perfume", new BigDecimal("340"), "AE-PF-4456"));

        // ---------- Sports ----------
        productRepository.save(product("Yoga Mat", "Non-slip, 6mm thick, carrying strap included",
                new BigDecimal("549"), "https://via.placeholder.com/300?text=Yoga+Mat", "Sports", 55, 4.5,
                "Meesho", "https://www.meesho.com/product/example-yogamat", new BigDecimal("200"), "MS-YM-5567"));

        productRepository.save(product("Adjustable Dumbbells Set", "5kg-25kg pair, space-saving design",
                new BigDecimal("3499"), "https://via.placeholder.com/300?text=Dumbbells", "Sports", 15, 4.6,
                "CJ Dropshipping", "https://cjdropshipping.com/product/example-dumbbells", new BigDecimal("1480"), "CJ-DB-6678"));

        productRepository.save(product("Cricket Bat", "English willow, lightweight, full size",
                new BigDecimal("1899"), "https://via.placeholder.com/300?text=Cricket+Bat", "Sports", 20, 4.3,
                null, null, new BigDecimal("760"), null));

        productRepository.save(product("Football", "Size 5, all-weather synthetic leather",
                new BigDecimal("699"), "https://via.placeholder.com/300?text=Football", "Sports", 50, 4.2,
                "AliExpress", "https://www.aliexpress.com/item/example-football", new BigDecimal("260"), "AE-FB-7789"));

        // ---------- Grocery (self-sourced, no dropship supplier) ----------
        productRepository.save(product("Basmati Rice 5kg", "Premium long-grain aged basmati rice",
                new BigDecimal("649"), "https://via.placeholder.com/300?text=Basmati+Rice", "Grocery", 100, 4.5,
                null, null, new BigDecimal("480"), null));

        productRepository.save(product("Organic Honey 500g", "Pure, unprocessed, raw forest honey",
                new BigDecimal("399"), "https://via.placeholder.com/300?text=Honey", "Grocery", 70, 4.6,
                null, null, new BigDecimal("260"), null));

        productRepository.save(product("Mixed Nuts Pack 1kg", "Almonds, cashews, walnuts, raisins mix",
                new BigDecimal("899"), "https://via.placeholder.com/300?text=Mixed+Nuts", "Grocery", 45, 4.4,
                null, null, new BigDecimal("620"), null));

        productRepository.save(product("Cold Pressed Olive Oil 1L", "Extra virgin, imported, glass bottle",
                new BigDecimal("799"), "https://via.placeholder.com/300?text=Olive+Oil", "Grocery", 35, 4.3,
                null, null, new BigDecimal("540"), null));
    }

    private void seedAdminUser() {
        if (userRepository.existsByEmail("admin@shopease.com")) return;

        User admin = new User();
        admin.setName("Admin");
        admin.setEmail("admin@shopease.com");
        admin.setPassword(passwordEncoder.encode("admin123"));
        admin.setRoles(Set.of("ROLE_ADMIN", "ROLE_USER"));
        userRepository.save(admin);
    }
}
