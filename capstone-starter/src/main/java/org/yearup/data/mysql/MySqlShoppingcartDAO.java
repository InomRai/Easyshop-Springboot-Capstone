package org.yearup.data.mysql;

import org.yearup.data.ProductDao;
import org.yearup.data.ShoppingCartDao;
import org.yearup.models.Product;
import org.yearup.models.ShoppingCart;
import org.yearup.models.ShoppingCartItem;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MySqlShoppingcartDAO extends MySqlDaoBase implements ShoppingCartDao {

    private final ProductDao productDao;

    public MySqlShoppingcartDAO(DataSource dataSource, ProductDao productDao) {
        super(dataSource);
        this.productDao = productDao;
    }

    @Override
    public ShoppingCart getByUserId(int userId) {
        ShoppingCart cart = new ShoppingCart();

        String sql = "SELECT product_id, quantity FROM cart_items WHERE user_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int productId = rs.getInt("product_id");
                int quantity = rs.getInt("quantity");

                Product product = productDao.getById(productId);
                if (product != null) {
                    ShoppingCartItem item = new ShoppingCartItem();
                    item.setProduct(product);
                    item.setQuantity(quantity);
                    cart.add(item);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Replace with proper logging in production
        }

        return cart;
    }

    @Override
    public ShoppingCart addProduct(int userId, int productId) {
        String sql = """
            INSERT INTO cart_items (user_id, product_id, quantity)
            VALUES (?, ?, 1)
            ON DUPLICATE KEY UPDATE quantity = quantity + 1;
        """;

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.setInt(2, productId);
            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return getByUserId(userId);
    }

    @Override
    public ShoppingCart updateProductQuantity(int userId, int productId, int quantity) {
        String sql;

        if (quantity <= 0) {
            sql = "DELETE FROM cart_items WHERE user_id = ? AND product_id = ?";
        } else {
            sql = "UPDATE cart_items SET quantity = ? WHERE user_id = ? AND product_id = ?";
        }

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            if (quantity <= 0) {
                stmt.setInt(1, userId);
                stmt.setInt(2, productId);
            } else {
                stmt.setInt(1, quantity);
                stmt.setInt(2, userId);
                stmt.setInt(3, productId);
            }

            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return getByUserId(userId);
    }

    @Override
    public ShoppingCart clearCart(int userId) {
        String sql = "DELETE FROM cart_items WHERE user_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return new ShoppingCart(); // Empty cart
    }
}
