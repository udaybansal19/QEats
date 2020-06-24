package com.crio.qeats.exchanges;

import com.crio.qeats.dto.Cart;

import javax.validation.constraints.NotNull;

public class CartModifiedResponse {

  Cart cart;
  int cartResponseType;

  public Cart getCart() {
    return cart;
  }

  public void setCart(Cart cart) {
    this.cart = cart;
  }

  public int getCartResponseType() {
    return cartResponseType;
  }

  public void setCartResponseType(int cartResponseType) {
    this.cartResponseType = cartResponseType;
  }
}

