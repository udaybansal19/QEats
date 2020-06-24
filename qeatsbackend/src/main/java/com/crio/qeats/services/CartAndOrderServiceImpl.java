package com.crio.qeats.services;

import com.crio.qeats.dto.Cart;
import com.crio.qeats.dto.Item;
import com.crio.qeats.dto.Order;
import com.crio.qeats.exceptions.CartNotFoundException;
import com.crio.qeats.exceptions.EmptyCartException;
import com.crio.qeats.exceptions.ItemNotFoundInRestaurantMenuException;
import com.crio.qeats.exceptions.ItemNotFromSameRestaurantException;
import com.crio.qeats.exchanges.CartModifiedResponse;
import com.crio.qeats.repositoryservices.CartRepositoryService;
import com.crio.qeats.repositoryservices.MenuRepositoryService;
import com.crio.qeats.repositoryservices.OrderRepositoryService;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class CartAndOrderServiceImpl implements CartAndOrderService {

  @Autowired
  private CartRepositoryService cartRepositoryService;

  @Autowired
  private OrderRepositoryService orderRepositoryService;

  @Autowired
  private MenuRepositoryService menuRepositoryService;

  @Autowired
  private MenuService menuService;

  @Override
  public Cart findOrCreateCart(String userId) {

    Optional<Cart> cart = cartRepositoryService.findCartByUserId(userId);
    if (cart.isPresent()) {
      return cart.get();
    } else {
      Cart cart1 = new Cart();
      cart1.setUserId(userId);
      cartRepositoryService.createCart(cart1);
      cart1.setRestaurantId("");
      return cartRepositoryService.findCartByCartId(cart1.getId());
    }
  }

  @Override
  public CartModifiedResponse addItemToCart(String itemId, String cartId,
                                            String restaurantId) throws
      ItemNotFromSameRestaurantException {
    CartModifiedResponse cartModifiedResponse = new CartModifiedResponse();
    Cart cart;
    try {
      cart = cartRepositoryService.findCartByCartId(cartId);
      if (!cart.getRestaurantId().equals(restaurantId)) {
        cartModifiedResponse.setCart(cart);
        cartModifiedResponse.setCartResponseType(102);
        return cartModifiedResponse;
      }
    } catch (CartNotFoundException e) {
      throw new CartNotFoundException();
    }
    Item item;
    try {
      item = menuService.findItem(itemId, restaurantId);
    } catch (ItemNotFromSameRestaurantException e) {
      cartModifiedResponse.setCart(cart);
      cartModifiedResponse.setCartResponseType(102);
      return cartModifiedResponse;
    }
    try {
      cart = cartRepositoryService.addItem(item, cartId, restaurantId);
      cartModifiedResponse.setCart(cart);
      cartModifiedResponse.setCartResponseType(0);
      return cartModifiedResponse;
    } catch (CartNotFoundException e) {
      cartModifiedResponse.setCart(new Cart());
      cartModifiedResponse.setCartResponseType(102);
      return cartModifiedResponse;
    }
  }

  @Override
  public CartModifiedResponse removeItemFromCart(String itemId,
                                                 String cartId,
                                                 String restaurantId) {
    CartModifiedResponse cartModifiedResponse = new CartModifiedResponse();
    Cart cart = new Cart();
    try {
      Item item = menuService.findItem(itemId, restaurantId);
      cart = cartRepositoryService.removeItem(item, cartId, restaurantId);
      cartModifiedResponse.setCart(cart);
      cartModifiedResponse.setCartResponseType(0);
      return cartModifiedResponse;
    } catch (Exception e) {
      cartModifiedResponse.setCart(cart);
      cartModifiedResponse.setCartResponseType(0);
      return cartModifiedResponse;
    }
  }

  @Override
  public Order postOrder(String cartId) throws EmptyCartException {
    try {
      Cart cart = cartRepositoryService.findCartByCartId(cartId);
      return orderRepositoryService.placeOrder(cart);
    } catch (CartNotFoundException e) {
      throw new EmptyCartException("Empty");
    }
  }
}
