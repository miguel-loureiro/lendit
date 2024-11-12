package com.ims;

import com.ims.models.Category;
import com.ims.models.Item;
import com.ims.models.Role;
import com.ims.models.User;
import com.ims.repository.ItemRepository;
import com.ims.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;

@Component
@Profile("test")
public class InitialDataSeeder implements ApplicationListener<ContextRefreshedEvent> {

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        addSuperUser();
        addManagerUser();
        addClientUser();
        addClientUser2();
        seedItems();
    }

    private void seedItems() {
        // Avoid duplication by checking if items already exist
        if (itemRepository.count() > 0) {
            return; // Items already seeded
        }

        List<Item> items = generateRandomItems(30);
        itemRepository.saveAll(items);
    }

    private List<Item> generateRandomItems(int count) {
        List<Item> items = new ArrayList<>();
        Set<String> uniqueDesignations = new HashSet<>();
        Random random = new Random();

        String[] brands = {
                "Lenovo", "Logitech", "Dell", "Acer", "HP", "Samsung",
                "TP-Link", "Seagate", "Asus", "Apple", "Microsoft",
                "Razer", "BenQ", "LG", "Corsair", "Netgear", "Synology",
                "Western Digital", "HyperX", "SteelSeries"
        };

        String[] categories = {
                "Laptop", "Mouse", "Monitor", "Router", "Storage",
                "Headphones", "Keyboard", "Gaming", "Smartphone", "Tablet"
        };

        while (items.size() < count) {
            String designation = generateUniqueDesignation(uniqueDesignations);
            String barcode = generateRandomBarcode();
            String brand = brands[random.nextInt(brands.length)];
            String category = categories[random.nextInt(categories.length)];
            BigDecimal purchasePrice = generateRandomPrice(random);
           // int stockQuantity = 10 + random.nextInt(91);
            int stockQuantity = 10;

            Item item = new Item(designation, barcode, brand, category, purchasePrice, stockQuantity);
            items.add(item);
        }

        return items;
    }

    private String generateUniqueDesignation(Set<String> uniqueDesignations) {
        Random random = new Random();
        String[] designations = {
                "ThinkPad X1 Carbon Gen 9", "MX Master 3", "Dell XPS 13",
                "Acer Predator Helios 300", "HP Spectre x360", "Logitech G502",
                "Samsung Odyssey G7", "TP-Link Archer AX6000", "Seagate Expansion 2TB",
                "Dell UltraSharp U2720Q", "Asus ROG Zephyrus G14", "Apple MacBook Air",
                "Microsoft Surface Laptop 4", "Logitech K810", "Razer Huntsman Elite",
                "BenQ PD3220U", "LG UltraFine 5K", "Corsair K95 RGB",
                "Netgear Nighthawk AX12", "Synology DS220+", "Western Digital My Passport",
                "Acer Aspire 5", "Lenovo Yoga 7i", "HP Omen 15",
                "Dell Inspiron 15", "Logitech C920", "Samsung Galaxy Book",
                "Apple iPad Pro", "HyperX Cloud II", "SteelSeries Arctis 7"
        };

        String designation;
        do {
            designation = designations[random.nextInt(designations.length)];
        } while (!uniqueDesignations.add(designation));

        return designation;
    }

    private String generateRandomBarcode() {
        Random random = new Random();
        StringBuilder barcode = new StringBuilder("590");
        for (int i = 0; i < 10; i++) {
            barcode.append(random.nextInt(10));
        }
        return barcode.toString();
    }

    private BigDecimal generateRandomPrice(Random random) {
        return BigDecimal.valueOf(50 + (random.nextDouble() * 950));
    }

    private void addSuperUser() {
        String encodedPassword = passwordEncoder.encode("superpassword");
        userRepository.save(new User("superuser", "publixoapagar@gmail.com", encodedPassword, Role.SUPER));
    }

    private void addManagerUser() {
        String encodedPassword = passwordEncoder.encode("managerpassword");
        userRepository.save(new User("manageruser", "msloureiro2022@gmail.com", encodedPassword, Role.MANAGER));
    }

    private void addClientUser() {
        String encodedPassword = passwordEncoder.encode("clientpassword");
        userRepository.save(new User("clientuser", "miguel.silvaloureiro@gmail.com", encodedPassword, Role.CLIENT));
    }

    private void addClientUser2() {
        String encodedPassword = passwordEncoder.encode("client2password");
        userRepository.save(new User("client2user", "marta.manuel.ferreira@gmail.com", encodedPassword, Role.CLIENT));
    }
}