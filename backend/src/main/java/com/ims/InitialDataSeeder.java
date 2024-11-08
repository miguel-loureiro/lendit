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

    private static final String[] DESIGNATIONS = {
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

    private static final String[] BRANDS = {
            "Lenovo", "Logitech", "Dell", "Acer", "HP", "Samsung",
            "TP-Link", "Seagate", "Asus", "Apple", "Microsoft",
            "Razer", "BenQ", "LG", "Corsair", "Netgear", "Synology",
            "Western Digital", "HyperX", "SteelSeries"
    };

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        addSuperUser();
        addManagerUser();
        addClientUser();
        seedItems();
    }

    private void addSuperUser() {

        String encodedPassword = passwordEncoder.encode("superpassword");
        //String encodedPassword = "superpassword";
        userRepository.save(new User("superuser", "publixoapagar@gmail.com", encodedPassword, Role.SUPER));
    }

    private void addManagerUser() {

        String encodedPassword = passwordEncoder.encode("managerpassword");
        //String encodedPassword = "superpassword";
        userRepository.save(new User("manageruser", "msloureiro2022@gmail.com", encodedPassword, Role.MANAGER));
    }

    private void addClientUser() {

        String encodedPassword = passwordEncoder.encode("clientpassword");
        //String encodedPassword = "superpassword";
        userRepository.save(new User("clientuser", "miguel.silvaloureiro@gmail.com", encodedPassword, Role.CLIENT));
    }

    private void seedItems() {
        // Avoid duplication by checking if items already exist
        if (itemRepository.count() > 0) {
            return; // Items already seeded
        }

        List<Item> items = generateRandomItems(30);
        for (Item item : items) {
            try {
                itemRepository.save(item);
            } catch (DataIntegrityViolationException e) {
                // Log the exception and continue
                System.err.println("Item with designation '" + item.getDesignation() + "' already exists. Skipping.");
            }
        }
    }

    private List<Item> generateRandomItems(int count) {
        List<Item> items = new ArrayList<>();
        Random random = new Random();

        // Shuffle the designations to ensure uniqueness
        List<String> shuffledDesignations = Arrays.asList(DESIGNATIONS.clone());
        Collections.shuffle(shuffledDesignations, random);

        for (int i = 0; i < Math.min(count, shuffledDesignations.size()); i++) {
            items.add(createRandomItem(shuffledDesignations.get(i), random));
        }

        return items;
    }

    private Item createRandomItem(String designation, Random random) {
        String barcode = generateRandomBarcode();
        String brand = getRandomElement(BRANDS, random);
        Category category = getRandomElement(Category.values(), random);
        BigDecimal purchasePrice = generateRandomPrice(random);
        int stockQuantity = random.nextInt(100); // Random stock quantity between 0 and 99

        return new Item(designation, barcode, brand, category, purchasePrice, stockQuantity);
    }

    private String generateRandomBarcode() {
        Random random = new Random();
        StringBuilder barcode = new StringBuilder("590");
        for (int i = 0; i < 10; i++) {
            barcode.append(random.nextInt(10)); // Generate 10 random digits
        }
        return barcode.toString();
    }

    private BigDecimal generateRandomPrice(Random random) {
        return BigDecimal.valueOf(50 + (random.nextDouble() * 950)); // Random price between 50 and 1000
    }

    private <T> T getRandomElement(T[] array, Random random) {
        return array[random.nextInt(array.length)];
    }
}
