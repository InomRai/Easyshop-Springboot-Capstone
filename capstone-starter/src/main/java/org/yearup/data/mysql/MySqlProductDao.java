package org.yearup.data.mysql;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.yearup.data.ProductDao;
import org.yearup.models.Product;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal;

@Repository
public class MySqlProductDao implements ProductDao {
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public MySqlProductDao(DataSource ds) {
        this.jdbcTemplate = new JdbcTemplate(ds);
    }

    @Override
    public List<Product> search(Integer categoryId, BigDecimal minPrice, BigDecimal maxPrice, String color) {
        StringBuilder sql = new StringBuilder("SELECT * FROM products WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (categoryId != null) {
            sql.append(" AND category_id = ?");
            params.add(categoryId);
        }
        if (minPrice != null) {
            sql.append(" AND price >= ?");
            params.add(minPrice);
        }
        if (maxPrice != null) {
            sql.append(" AND price <= ?");
            params.add(maxPrice);
        }
        if (color != null && !color.isBlank()) {
            sql.append(" AND color = ?");
            params.add(color);
        }

        return jdbcTemplate.query(sql.toString(), params.toArray(), (rs, rn) -> mapRow(rs));
    }

    @Override
    public List<Product> listByCategoryId(int categoryId) {
        return List.of();
    }

    @Override
    public Product getById(int id) {
        String sql = "SELECT * FROM products WHERE product_id = ?";
        return jdbcTemplate.query(sql, new Object[]{id}, rs -> {
            if (rs.next()) return mapRow(rs);
            return null;
        });
    }

    @Override
    public Product create(Product product) {
        String sql = "INSERT INTO products (name, description, price, category_id, color) VALUES (?, ?, ?, ?, ?)";
        KeyHolder kh = new GeneratedKeyHolder();
        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, product.getName());
            ps.setString(2, product.getDescription());
            ps.setBigDecimal(3, product.getPrice());
            ps.setInt(4, product.getCategoryId());
            ps.setString(5, product.getColor());
            return ps;
        }, kh);
        product.setProductId(kh.getKey().intValue());
        return product;
    }

    @Override
    public void update(int id, Product product) {
        String sql = """
            UPDATE products
            SET name = ?, description = ?, price = ?, category_id = ?, color = ?
            WHERE product_id = ?
        """;
        jdbcTemplate.update(sql,
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getCategoryId(),
                product.getColor(),
                id
        );
    }

    @Override
    public void delete(int id) {
        String sql = "DELETE FROM products WHERE product_id = ?";
        jdbcTemplate.update(sql, id);
    }

    private Product mapRow(java.sql.ResultSet rs) throws java.sql.SQLException {
        var p = new Product();
        p.setProductId(rs.getInt("product_id"));
        p.setName(rs.getString("name"));
        p.setDescription(rs.getString("description"));
        p.setPrice(rs.getBigDecimal("price"));
        p.setCategoryId(rs.getInt("category_id"));
        p.setColor(rs.getString("color"));
        return p;
    }
}