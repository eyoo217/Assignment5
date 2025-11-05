package org.example.Amazon;

import org.example.Amazon.Cost.*;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class AmazonIntegrationTest {
    private Database db;
    private Amazon amazon;
    private ShoppingCartAdaptor cart;

    @BeforeEach
    void setUp() {
        db = new Database();
        cart = new ShoppingCartAdaptor(db);
        List<PriceRule> rules = List.of(
                new DeliveryPrice(), new ExtraCostForElectronics(), new RegularCost()
        );
        amazon = new Amazon(cart, rules);
    }

    @AfterEach
    void tearDown(){
        db.close();
    }


    @Test
    @DisplayName("specification-based")
    void testAddToCartPriceNothing() {
        Amazon amazon = mock(Amazon.class);
        assertEquals(0, amazon.calculate());
    }

    @Test
    @DisplayName("specification-based")
    void testAddToCartPriceItem() {
        Item item1 = new Item(ItemType.ELECTRONIC, "phone", 1, 10.0);
        amazon.addToCart(item1);
        assertEquals(22.5, amazon.calculate());
    }

    @Test
    @DisplayName("structural-based")
    void testCartAddition() {
        Item item = new Item(ItemType.ELECTRONIC, "phone", 1, 10.0);
        assertEquals(List.of(), cart.getItems());
        amazon.addToCart(item);
        List<Item> itemsInCart = cart.getItems();
        assertEquals(itemsInCart.getFirst().getName(), item.getName());
    }


    @Test
    @DisplayName("structural-based")
    void test() {
        Item item = new Item(ItemType.ELECTRONIC, "phone", 1, 10.0);
        assertEquals(List.of(), cart.getItems());
        amazon.addToCart(item);
        List<Item> itemsInCart = cart.getItems();
        assertEquals(itemsInCart.getFirst().getName(), item.getName());
    }
}