package org.yearup.configurations;

import org.apache.commons.dbcp2.BasicDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.yearup.data.ProductDao;
import org.yearup.data.ShoppingCartDao;
import org.yearup.data.mysql.MySqlProductDao;
import org.yearup.data.mysql.MySqlShoppingcartDAO;

import javax.sql.DataSource;

@Configuration
public class DatabaseConfig
{
    private BasicDataSource basicDataSource;

    @Bean
    public DataSource dataSource()
    {
        return basicDataSource;
    }

    @Autowired
    public DatabaseConfig(@Value("${datasource.url}") String url,
                          @Value("${datasource.username}") String username,
                          @Value("${datasource.password}") String password)
    {
        basicDataSource = new BasicDataSource();
        basicDataSource.setUrl(url);
        basicDataSource.setUsername(username);
        basicDataSource.setPassword(password);
    }

    //Register ProductDao as a Spring Bean
    @Bean
    public ProductDao productDao(DataSource dataSource)
    {
        return new MySqlProductDao(dataSource);
    }

    //Register ShoppingCartDao as a Spring Bean
    @Bean
    public ShoppingCartDao shoppingCartDao(DataSource dataSource, ProductDao productDao)
    {
        return new MySqlShoppingcartDAO(dataSource, productDao);
    }
}
