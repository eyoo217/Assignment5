package org.example.Amazon;

import org.example.Amazon.Cost.*;
import org.junit.jupiter.api.Test;

import java.sql.*;
import java.sql.PreparedStatement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AmazonUnitTest {
    @Test
    void testNoRulesNoCart() {
        ShoppingCart cart = mock(ShoppingCart.class);
        Amazon amazon = new Amazon(cart, List.of());
        assertEquals(0.0, amazon.calculate());
        verifyNoInteractions(cart);
    }

    @Test
    void testRulesCart() {
        ShoppingCart cart = mock(ShoppingCart.class);
        List<Item> items = (List<Item>) mock(List.class);
        when(cart.getItems()).thenReturn(items);

        PriceRule rule = mock(PriceRule.class);
        when(rule.priceToAggregate(items)).thenReturn(12.5);

        Amazon amazon = new Amazon(cart, List.of(rule));

        assertEquals(12.5, amazon.calculate());
        verify(cart, times(1)).getItems();
        verify(rule, times(1)).priceToAggregate(items);
    }

    @Test
    void calculate_multipleRules_sumsContributions() {
        ShoppingCart cart = mock(ShoppingCart.class);
        List<Item> items = List.of(
                new Item(ItemType.OTHER, "apples", 2, 3.00),
                new Item(ItemType.ELECTRONIC, "phone", 1, 10.00)
        );
        when(cart.getItems()).thenReturn(items);

        PriceRule r1 = mock(PriceRule.class);
        PriceRule r2 = mock(PriceRule.class);
        PriceRule r3 = mock(PriceRule.class);
        when(r1.priceToAggregate(items)).thenReturn(5.00);
        when(r2.priceToAggregate(items)).thenReturn(7.50);
        when(r3.priceToAggregate(items)).thenReturn(0.25);

        Amazon amazon = new Amazon(cart, List.of(r1, r2, r3));

        double total = amazon.calculate();

        assertEquals(12.75, total, 1e-9);
        verify(cart, times(3)).getItems(); // once per rule
    }

    @Test
    void addToCart_forwardsToCart() {
        ShoppingCart cart = mock(ShoppingCart.class);
        Amazon amazon = new Amazon(cart, List.of());
        Item item = new Item(ItemType.OTHER, "milk", 1, 2.50);

        amazon.addToCart(item);

        verify(cart).add(item);
        verifyNoMoreInteractions(cart);
    }

    @Test
    void testVals() {
        Item item = new Item(ItemType.ELECTRONIC, "phone", 2, 100.0);
        assertEquals(ItemType.ELECTRONIC, item.getType());
        assertEquals("phone", item.getName());
        assertEquals(2, item.getQuantity());
        assertEquals(100.0, item.getPricePerUnit());
    }

    @Test
    void testAddCart() throws SQLException {
        Database db   = mock(Database.class);
        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);

        when(db.withSql(any())).thenAnswer(inv -> {
            Database.SqlSupplier<?> supplier = inv.getArgument(0);
            return supplier.doSql();
        });

        when(db.getConnection()).thenReturn(conn);

        ShoppingCart adaptor = new ShoppingCartAdaptor(db);
        Item item = new Item(ItemType.ELECTRONIC, "phone", 1, 10.0);

        when(conn.prepareStatement(
                "insert into shoppingcart (name, type, quantity, priceperunit) values (?,?,?,?)"
        )).thenReturn(ps);

        adaptor.add(item);

        verify(conn).prepareStatement(
                "insert into shoppingcart (name, type, quantity, priceperunit) values (?,?,?,?)"
        );
        verify(ps).setString(1, "phone");
        verify(ps).setString(2, "ELECTRONIC");
        verify(ps).setInt(3, 1);
        verify(ps).setDouble(4, 10.0);
        verify(ps).execute();
        verify(conn, atLeastOnce()).commit(); // adaptor commits after insert
        verify(ps).close();
    }

    @Test
    void getItems_mapsResultSet() throws Exception {
        Database db   = mock(Database.class);
        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);

        when(db.withSql(any())).thenAnswer(inv -> {
            Database.SqlSupplier<?> supplier = inv.getArgument(0);
            return supplier.doSql();
        });

        when(db.getConnection()).thenReturn(conn);

        ShoppingCart adaptor = new ShoppingCartAdaptor(db);
        when(conn.prepareStatement("select * from shoppingcart")).thenReturn(ps);
        when(ps.executeQuery()).thenReturn(rs);

        // Two rows, then end.
        when(rs.next()).thenReturn(true, true, false);
        // Row 1
        when(rs.getString("type")).thenReturn("ELECTRONIC", "OTHER");
        when(rs.getString("name")).thenReturn("phone", "apples");
        when(rs.getInt("quantity")).thenReturn(1, 2);
        when(rs.getDouble("priceperunit")).thenReturn(10.0, 3.0);

        var items = adaptor.getItems();

        assertEquals(2, items.size());
        assertEquals(ItemType.ELECTRONIC, items.getFirst().getType());
        assertEquals("phone", items.getFirst().getName());
        assertEquals(1, items.get(0).getQuantity());
        assertEquals(10.0, items.get(0).getPricePerUnit(), 1e-9);

        assertEquals(ItemType.OTHER, items.get(1).getType());
        assertEquals("apples", items.get(1).getName());
        assertEquals(2, items.get(1).getQuantity());
        assertEquals(3.0, items.get(1).getPricePerUnit(), 1e-9);

        verify(ps).close();
    }

    @Test
    void getItems_empty() throws Exception {
        Database db   = mock(Database.class);
        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);

        when(db.withSql(any())).thenAnswer(inv -> {
            Database.SqlSupplier<?> supplier = inv.getArgument(0);
            return supplier.doSql();
        });

        when(db.getConnection()).thenReturn(conn);

        ShoppingCart adaptor = new ShoppingCartAdaptor(db);
        when(conn.prepareStatement("select * from shoppingcart")).thenReturn(ps);
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(false);

        var items = adaptor.getItems();

        assertTrue(items.isEmpty());
    }
}