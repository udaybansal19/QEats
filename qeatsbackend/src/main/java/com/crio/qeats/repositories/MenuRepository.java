/*
 *
 *  * Copyright (c) Crio.Do 2019. All rights reserved
 *
 */

package com.crio.qeats.repositories;

import com.crio.qeats.models.MenuEntity;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;


public interface MenuRepository extends MongoRepository<MenuEntity, String> {

  @Query("{'items.name': {$regex: '^?0$', $options: 'i'}}")
  List<MenuEntity> findRestaurantsByItemExact(String name);

  @Query("{'items.name': {$regex: '.*?0.*', $options: 'i'}}")
  List<MenuEntity> findRestaurantsByItem(String name);

  @Query("{'items.attributes' : {$regex: '.*?0.*', $options: 'i'}}")
  List<MenuEntity> findRestaurantsByItemAttributes(String name);

  Optional<MenuEntity> findMenuByRestaurantId(String restaurantId);

  Optional<List<MenuEntity>> findMenusByItemsItemIdIn(List<String> itemIdList);

}
