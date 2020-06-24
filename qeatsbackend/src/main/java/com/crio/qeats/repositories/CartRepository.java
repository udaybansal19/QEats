/*
 *
 *  * Copyright (c) Crio.Do 2019. All rights reserved
 *
 */

package com.crio.qeats.repositories;

import com.crio.qeats.dto.Cart;
import com.crio.qeats.models.CartEntity;
import com.crio.qeats.models.RestaurantEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface CartRepository extends MongoRepository<CartEntity, String> {

  CartEntity save(CartEntity saved);

  @Query("{'userId' : '?0'}")
  CartEntity findByUserId(String search);

  @Query("{'id' : '?0'}")
  CartEntity findCartById(String cartId);
}
