package org.example.Barnes;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfSystemProperties;
import org.mockito.ArgumentCaptor;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BarnesAndNobleTest {

    @Test
    @DisplayName("specification-based")
    void getPriceCartNullTest() {
        BookDatabase bdb = mock(BookDatabase.class);
        BuyBookProcess bbp = mock(BuyBookProcess.class);
        BarnesAndNoble bn = new BarnesAndNoble(bdb, bbp);
        PurchaseSummary summary = bn.getPriceForCart(null);
        assertNull(summary);
    }

    @Test
    @DisplayName("specification-based")
    void getPriceForCart() {
        BookDatabase bdb = mock(BookDatabase.class);
        BuyBookProcess bbp = mock(BuyBookProcess.class);
        Book book = new Book("1234", 2,2);
        BarnesAndNoble bn = new BarnesAndNoble(bdb, bbp);

        HashMap<String, Integer> order = new HashMap<>();
        order.put("1234", 2);

        when(bdb.findByISBN("1234")).thenReturn(book);
        PurchaseSummary summary = bn.getPriceForCart(order);
        assertEquals(4, summary.getTotalPrice());
    }

    @Test
    @DisplayName("specification-based")
    void getPriceCartNonRetrievable() {
        BookDatabase bdb = mock(BookDatabase.class);
        BuyBookProcess bbp = mock(BuyBookProcess.class);
        Book book = new Book("1234", 2,2);
        BarnesAndNoble bn = new BarnesAndNoble(bdb, bbp);

        HashMap<String, Integer> order = new HashMap<>();
        order.put("1234", 3);

        when(bdb.findByISBN("1234")).thenReturn(book);
        PurchaseSummary summary = bn.getPriceForCart(order);

        assertTrue(summary.getUnavailable().containsKey(book));
        assertTrue(summary.getUnavailable().containsValue(1));
    }

    @Test
    @DisplayName("structural-based")
    void ISBNTest() {
        BookDatabase bdb = mock(BookDatabase.class);
        BuyBookProcess bbp = mock(BuyBookProcess.class);
        Book book = new Book("1234", 2,2);
        BarnesAndNoble bn = new BarnesAndNoble(bdb, bbp);

        HashMap<String, Integer> order = new HashMap<>();
        order.put("1234", 3);

        when(bdb.findByISBN("1234")).thenReturn(book);
        PurchaseSummary summary = bn.getPriceForCart(order);

        verify(bdb,times(1)).findByISBN("1234");
    }

    @Test
    @DisplayName("structural-based")
    void buyBookTest() {
        BookDatabase bdb = mock(BookDatabase.class);
        BuyBookProcess bbp = mock(BuyBookProcess.class);
        Book book = new Book("1234", 2,2);
        BarnesAndNoble bn = new BarnesAndNoble(bdb, bbp);

        HashMap<String, Integer> order = new HashMap<>();
        order.put("1234", 3);

        when(bdb.findByISBN("1234")).thenReturn(book);
        PurchaseSummary summary = bn.getPriceForCart(order);

        verify(bbp, times(1)).buyBook(book, 2);
    }
}