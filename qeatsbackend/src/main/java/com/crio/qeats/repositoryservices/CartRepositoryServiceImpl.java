/*
 *
 *  * Copyright (c) Crio.Do 2019. All rights reserved
 *
 */

package com.crio.qeats.repositoryservices;

import com.crio.qeats.dto.Cart;
import com.crio.qeats.dto.Item;
import com.crio.qeats.exceptions.CartNotFoundException;
import com.crio.qeats.models.CartEntity;
import com.crio.qeats.repositories.CartRepository;

import java.util.Optional;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class CartRepositoryServiceImpl implements CartRepositoryService {

  @Autowired
  CartRepository cartRepository;

  @Override
  public String createCart(Cart cart) {

    CartEntity cartEntity = new CartEntity();
    BeanUtils.copyProperties(cart, cartEntity);
    return cartRepository.save(cartEntity).getId();

  }

  @Override
  public Optional<Cart> findCartByUserId(String userId) {

    Cart cart = new Cart();
    CartEntity cartEntity = cartRepository.findByUserId(userId);
    cartRepository.save(cartEntity);
    BeanUtils.copyProperties(cartEntity, cart);
    return Optional.of(cart);
  }

  @Override
  public Cart findCartByCartId(String cartId) throws CartNotFoundException {
    try {
      Cart cart = new Cart();
      BeanUtils.copyProperties(cartRepository.findCartById(cartId), cart);
      return cart;
    } catch (NullPointerException e) {
      throw new CartNotFoundException();
    }
  }

  @Override
  public Cart addItem(Item item, String cartId, String restaurantId)
      throws CartNotFoundException {
    try {
      CartEntity cartEntity = cartRepository.findCartById(cartId);
      cartEntity.addItem(item);
      Cart cart = new Cart();
      if (cartEntity.getRestaurantId() == null || cartEntity.getRestaurantId().isEmpty()) {
        cartEntity.setRestaurantId(restaurantId);
      }
      cartRepository.save(cartEntity);
      BeanUtils.copyProperties(cartEntity, cart);
      return cart;
    } catch (NullPointerException e) {
      throw new CartNotFoundException();
    }
  }

  @Override
  public Cart removeItem(Item item, String cartId, String restaurantId)
      throws CartNotFoundException {
    try {
      CartEntity cartEntity = cartRepository.findCartById(cartId);
      cartEntity.removeItem(item);
      Cart cart = new Cart();
      if (cartEntity.getItems().isEmpty()) {
        cartEntity.setRestaurantId("");
      }
      cartRepository.save(cartEntity);
      BeanUtils.copyProperties(cartEntity, cart);
      //cart.setCartResponseType(0);
      return cart;
    } catch (NullPointerException e) {
      throw new CartNotFoundException();
    }
  }
}