/*
 *
 *  * Copyright (c) Crio.Do 2019. All rights reserved
 *
 */

package com.crio.qeats.repositories;

import com.crio.qeats.models.RestaurantEntity;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;


public interface RestaurantRepository extends MongoRepository<RestaurantEntity, String> {

  @Query("{'restaurantId': ?0}")
  List<RestaurantEntity> findRestaurantById(String search);
}

