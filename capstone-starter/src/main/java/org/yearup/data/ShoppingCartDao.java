package org.yearup.data;

import org.yearup.models.ShoppingCart;

public interface ShoppingCartDao
{
    ShoppingCart getByUserId(int userId);

    //  Add a product to the cart for the specified user
    ShoppingCart addProduct(int userId, int productId);

    ShoppingCart updateProductQuantity(int userId, int productId, int quantity);

    ShoppingCart clearCart(int userId);


}
