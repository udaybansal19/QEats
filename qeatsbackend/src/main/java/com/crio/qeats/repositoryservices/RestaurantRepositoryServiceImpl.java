/*
 *
 *  * Copyright (c) Crio.Do 2019. All rights reserved
 *
 */

package com.crio.qeats.repositoryservices;

import ch.hsr.geohash.GeoHash;
import com.crio.qeats.dto.Restaurant;
import com.crio.qeats.globals.GlobalConstants;
import com.crio.qeats.models.MenuEntity;
import com.crio.qeats.models.RestaurantEntity;
import com.crio.qeats.repositories.MenuRepository;
import com.crio.qeats.repositories.RestaurantRepository;
import com.crio.qeats.utils.GeoLocation;
import com.crio.qeats.utils.GeoUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.inject.Provider;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;


@Service
@Primary
public class RestaurantRepositoryServiceImpl implements RestaurantRepositoryService {


  @Autowired
  private RestaurantRepository restaurantRepository;

  @Autowired
  private MenuRepository menuRepository;

  @Autowired
  private MongoTemplate mongoTemplate;

  @Autowired
  private Provider<ModelMapper> modelMapperProvider;

  public static Double isNearby(Double lat1, Double lon1, Double lat2, Double lon2) {
    // TODO Auto-generated method stub
    final int R = 6371; // Radius of the earth
    Double latDistance = toRad(lat2 - lat1);
    Double lonDistance = toRad(lon2 - lon1);

    Double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
        + Math.cos(toRad(lat1)) * Math.cos(toRad(lat2))
        * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
    Double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    Double distance = R * c;

    return distance;

  }

  private static Double toRad(Double value) {
    return value * Math.PI / 180;
  }

  private boolean isOpenNow(LocalTime time, RestaurantEntity res) {
    LocalTime openingTime = LocalTime.parse(res.getOpensAt());
    LocalTime closingTime = LocalTime.parse(res.getClosesAt());

    return time.isAfter(openingTime) && time.isBefore(closingTime);
  }

  private List<Restaurant> findAllRestaurantsCloseFromDb(Double latitude, Double longitude,
                                                         LocalTime currentTime,
                                                         Double servingRadiusInKms) {
    List<Restaurant> restaurantList = new ArrayList<>();
    for (RestaurantEntity restaurantEntity : restaurantRepository.findAll()) {
      if (isOpenNow(currentTime, restaurantEntity)) {
        if (isNearby(latitude, longitude, restaurantEntity.getLatitude(),
            restaurantEntity.getLongitude()) <= servingRadiusInKms) {
          Restaurant restaurant = new Restaurant();
          restaurant.setRestaurantId(restaurantEntity.getRestaurantId());
          restaurant.setName(restaurantEntity.getName());
          restaurant.setCity(restaurantEntity.getCity());
          restaurant.setImageUrl(restaurantEntity.getImageUrl());
          restaurant.setId(restaurantEntity.getId());
          restaurant.setAttributes(restaurantEntity.getAttributes());
          restaurant.setOpensAt(restaurantEntity.getOpensAt());
          restaurant.setClosesAt(restaurantEntity.getClosesAt());
          restaurant.setLatitude(restaurantEntity.getLatitude());
          restaurant.setLongitude(restaurantEntity.getLongitude());
          restaurantList.add(restaurant);
        }
      }
    }
    String createdJsonString = "";
    GlobalConstants.initCache();
    try {
      createdJsonString = new ObjectMapper().writeValueAsString(restaurantList);
    } catch (IOException e) {
      e.printStackTrace();
    }
    JedisPool jedisPool = GlobalConstants.getJedisPool();
    Jedis jedis = jedisPool.getResource();
    GeoLocation geoLocation = new GeoLocation(latitude, longitude);
    GeoHash geoHash = GeoHash.withCharacterPrecision(geoLocation.getLatitude(),
        geoLocation.getLongitude(), 7);

    jedis.set(geoHash.toBase32(), createdJsonString);
    return restaurantList;
  }

  private List<Restaurant> findAllRestaurantsCloseByFromCache(
      Double latitude, Double longitude, LocalTime currentTime, Double servingRadiusInKms) {

    List<Restaurant> restaurantList = new ArrayList<>();
    JedisPool jedisPool = GlobalConstants.getJedisPool();
    Jedis jedis = jedisPool.getResource();

    GeoLocation geoLocation = new GeoLocation(latitude, longitude);
    GeoHash geoHash = GeoHash.withCharacterPrecision(geoLocation.getLatitude(),
        geoLocation.getLongitude(), 7);
    if (!(jedis.exists(geoHash.toBase32()))) {
      return findAllRestaurantsCloseFromDb(latitude, longitude, currentTime, servingRadiusInKms);
    }
    String jsonStringFromCache = "";
    try {
      jsonStringFromCache = jedis.get(geoHash.toBase32());
      restaurantList = new ObjectMapper().readValue(jsonStringFromCache,
          new TypeReference<List<Restaurant>>() {
          });
    } catch (IOException e) {
      e.printStackTrace();
    }
    return restaurantList;
  }


  // TODO: CRIO_TASK_MODULE_NOSQL
  // Objectives:
  // 1. Implement findAllRestaurantsCloseby.
  // 2. Remember to keep the precision of GeoHash in mind while using it as a key.
  // Check RestaurantRepositoryService.java file for the interface contract.
  public List<Restaurant> findAllRestaurantsCloseBy(Double latitude,
                                                    Double longitude,
                                                    LocalTime currentTime,
                                                    Double servingRadiusInKms) {
    if (GlobalConstants.isCacheAvailable()) {
      return findAllRestaurantsCloseByFromCache(latitude, longitude, currentTime,
          servingRadiusInKms);
    } else {
      return findAllRestaurantsCloseFromDb(latitude, longitude, currentTime, servingRadiusInKms);
    }
  }

  // TODO: CRIO_TASK_MODULE_RESTAURANTSEARCH
  // Objective:
  // Find restaurants whose names have an exact or partial match with the search query.
  @Override
  public List<Restaurant> findRestaurantsByName(Double latitude, Double longitude,
                                                String searchString,
                                                LocalTime currentTime,
                                                Double servingRadiusInKms) {

    Set<String> restaurantSet1 = new HashSet<>();
    List<Restaurant> restaurantListByName = new ArrayList<>();
    for (RestaurantEntity restaurantEntity : restaurantRepository.findAll()) {
      if (isRestaurantCloseByAndOpen(restaurantEntity, currentTime, latitude,
          longitude, servingRadiusInKms)) {
        if (restaurantEntity.getName().equalsIgnoreCase(searchString)) {
          Restaurant restaurant = new Restaurant();
          restaurant.setRestaurantId(restaurantEntity.getRestaurantId());
          restaurant.setName(restaurantEntity.getName());
          restaurant.setCity(restaurantEntity.getCity());
          restaurant.setImageUrl(restaurantEntity.getImageUrl());
          restaurant.setId(restaurantEntity.getId());
          restaurant.setAttributes(restaurantEntity.getAttributes());
          restaurant.setOpensAt(restaurantEntity.getOpensAt());
          restaurant.setClosesAt(restaurantEntity.getClosesAt());
          restaurant.setLatitude(restaurantEntity.getLatitude());
          restaurant.setLongitude(restaurantEntity.getLongitude());
          restaurantListByName.add(restaurant);
          restaurantSet1.add(restaurant.getName());
        }
      }
    }

    for (RestaurantEntity restaurantEntity : restaurantRepository.findAll()) {

      if (isRestaurantCloseByAndOpen(restaurantEntity, currentTime,
          latitude, longitude, servingRadiusInKms)) {
        if (restaurantEntity.getName().toLowerCase()
            .contains(searchString.toLowerCase())) {
          Restaurant restaurant = new Restaurant();
          restaurant.setRestaurantId(restaurantEntity.getRestaurantId());
          restaurant.setName(restaurantEntity.getName());
          restaurant.setCity(restaurantEntity.getCity());
          restaurant.setImageUrl(restaurantEntity.getImageUrl());
          restaurant.setId(restaurantEntity.getId());
          restaurant.setAttributes(restaurantEntity.getAttributes());
          restaurant.setOpensAt(restaurantEntity.getOpensAt());
          restaurant.setClosesAt(restaurantEntity.getClosesAt());
          restaurant.setLatitude(restaurantEntity.getLatitude());
          restaurant.setLongitude(restaurantEntity.getLongitude());
          int x;
          if (restaurantSet1.isEmpty()) {
            x = 0;
          } else {
            x = restaurantSet1.size();
          }
          restaurantSet1.add(restaurant.getName());
          int y = restaurantSet1.size();
          if (x != y) {
            restaurantListByName.add(restaurant);
          }
        }
      }
    }


    return restaurantListByName;
  }

  // TODO: CRIO_TASK_MODULE_RESTAURANTSEARCH
  // Objective:
  // Find restaurants whose attributes (cuisines) intersect with the search query.
  @Override
  public List<Restaurant> findRestaurantsByAttributes(
      Double latitude, Double longitude,
      String searchString, LocalTime currentTime, Double servingRadiusInKms) {


    //Set<Restaurant> restaurantSet2 = new HashSet<>();
    List<Restaurant> restaurantListByAttributes = new ArrayList<>();
    for (RestaurantEntity restaurantEntity : restaurantRepository.findAll()) {
      if (isOpenNow(currentTime, restaurantEntity)) {
        if (isNearby(latitude, longitude, restaurantEntity.getLatitude(),
            restaurantEntity.getLongitude()) <= servingRadiusInKms) {
          for (String tempString : restaurantEntity.getAttributes()) {
            if (tempString.toLowerCase().contains(searchString.toLowerCase())) {
              Restaurant restaurant = new Restaurant();
              restaurant.setRestaurantId(restaurantEntity.getRestaurantId());
              restaurant.setName(restaurantEntity.getName());
              restaurant.setCity(restaurantEntity.getCity());
              restaurant.setImageUrl(restaurantEntity.getImageUrl());
              restaurant.setId(restaurantEntity.getId());
              restaurant.setAttributes(restaurantEntity.getAttributes());
              restaurant.setOpensAt(restaurantEntity.getOpensAt());
              restaurant.setClosesAt(restaurantEntity.getClosesAt());
              restaurant.setLatitude(restaurantEntity.getLatitude());
              restaurant.setLongitude(restaurantEntity.getLongitude());
              restaurantListByAttributes.add(restaurant);
            }
          }
        }
      }
    }
    return restaurantListByAttributes;
  }

  // TODO: CRIO_TASK_MODULE_RESTAURANTSEARCH
  // Objective:
  // Find restaurants which serve food items whose names form a complete or partial match
  // with the search query.
  @Override
  public List<Restaurant> findRestaurantsByItemName(
      Double latitude, Double longitude,
      String searchString, LocalTime currentTime, Double servingRadiusInKms) {

    Set<Restaurant> restaurantSet3 = new HashSet<>();
    List<Restaurant> restaurantListByItem = new ArrayList<>();
    for (MenuEntity menuEntity : menuRepository.findRestaurantsByItemExact(searchString)) {

      RestaurantEntity restaurantEntity = restaurantRepository
          .findRestaurantById(menuEntity.getRestaurantId()).get(0);

      if (isOpenNow(currentTime, restaurantEntity)) {
        if (isNearby(latitude, longitude, restaurantEntity.getLatitude(),
            restaurantEntity.getLongitude()) <= servingRadiusInKms) {
          Restaurant restaurant = new Restaurant();
          restaurant.setRestaurantId(restaurantEntity.getRestaurantId());
          restaurant.setName(restaurantEntity.getName());
          restaurant.setCity(restaurantEntity.getCity());
          restaurant.setImageUrl(restaurantEntity.getImageUrl());
          restaurant.setId(restaurantEntity.getId());
          restaurant.setAttributes(restaurantEntity.getAttributes());
          restaurant.setOpensAt(restaurantEntity.getOpensAt());
          restaurant.setClosesAt(restaurantEntity.getClosesAt());
          restaurant.setLatitude(restaurantEntity.getLatitude());
          restaurant.setLongitude(restaurantEntity.getLongitude());
          restaurantListByItem.add(restaurant);
          restaurantSet3.add(restaurant);

        }
      }
    }
    for (MenuEntity menuEntity1 : menuRepository.findRestaurantsByItem(searchString)) {

      RestaurantEntity restaurantEntity = restaurantRepository
          .findRestaurantById(menuEntity1.getRestaurantId()).get(0);

      if (isOpenNow(currentTime, restaurantEntity)) {
        if (isNearby(latitude, longitude, restaurantEntity.getLatitude(),
            restaurantEntity.getLongitude()) <= servingRadiusInKms) {
          Restaurant restaurant = new Restaurant();
          restaurant.setRestaurantId(restaurantEntity.getRestaurantId());
          restaurant.setName(restaurantEntity.getName());
          restaurant.setCity(restaurantEntity.getCity());
          restaurant.setImageUrl(restaurantEntity.getImageUrl());
          restaurant.setId(restaurantEntity.getId());
          restaurant.setAttributes(restaurantEntity.getAttributes());
          restaurant.setOpensAt(restaurantEntity.getOpensAt());
          restaurant.setClosesAt(restaurantEntity.getClosesAt());
          restaurant.setLatitude(restaurantEntity.getLatitude());
          restaurant.setLongitude(restaurantEntity.getLongitude());
          int x;
          if (restaurantSet3.isEmpty()) {
            x = 0;
          } else {
            x = restaurantSet3.size();
          }
          restaurantSet3.add(restaurant);
          int y = restaurantSet3.size();
          if (x != y) {
            restaurantListByItem.add(restaurant);
          }

        }
      }

    }


    return restaurantListByItem;
  }

  // TODO: CRIO_TASK_MODULE_RESTAURANTSEARCH
  // Objective:
  // Find restaurants which serve food items whose attributes intersect with the search query.
  @Override
  public List<Restaurant> findRestaurantsByItemAttributes(Double latitude, Double longitude,
                                                          String searchString,
                                                          LocalTime currentTime,
                                                          Double servingRadiusInKms) {
    List<Restaurant> restaurantListByItemAttributes = new ArrayList<>();
    for (MenuEntity menuEntity : menuRepository.findRestaurantsByItemAttributes(searchString)) {

      RestaurantEntity restaurantEntity = restaurantRepository
          .findRestaurantById(menuEntity.getRestaurantId()).get(0);
      if (isOpenNow(currentTime, restaurantEntity)) {
        if (isNearby(latitude, longitude, restaurantEntity.getLatitude(),
            restaurantEntity.getLongitude()) <= servingRadiusInKms) {
          Restaurant restaurant = new Restaurant();
          restaurant.setRestaurantId(restaurantEntity.getRestaurantId());
          restaurant.setName(restaurantEntity.getName());
          restaurant.setCity(restaurantEntity.getCity());
          restaurant.setImageUrl(restaurantEntity.getImageUrl());
          restaurant.setId(restaurantEntity.getId());
          restaurant.setAttributes(restaurantEntity.getAttributes());
          restaurant.setOpensAt(restaurantEntity.getOpensAt());
          restaurant.setClosesAt(restaurantEntity.getClosesAt());
          restaurant.setLatitude(restaurantEntity.getLatitude());
          restaurant.setLongitude(restaurantEntity.getLongitude());
          restaurantListByItemAttributes.add(restaurant);
        }
      }
    }


    return restaurantListByItemAttributes;
  }

  // TODO: CRIO_TASK_MODULE_NOSQL
  // Objective:
  // 1. Check if a restaurant is nearby and open. If so, it is a candidate to be returned.
  // NOTE: How far exactly is "nearby"?

  /**
   * Utility method to check if a restaurant is within the serving radius at a given time.
   *
   * @return boolean True if restaurant falls within serving radius and is open, false otherwise
   */
  private boolean isRestaurantCloseByAndOpen(RestaurantEntity restaurantEntity,
                                             LocalTime currentTime,
                                             Double latitude, Double longitude,
                                             Double servingRadiusInKms) {
    if (isOpenNow(currentTime, restaurantEntity)) {
      return GeoUtils.findDistanceInKm(latitude, longitude,
          restaurantEntity.getLatitude(), restaurantEntity.getLongitude())
          < servingRadiusInKms;
    }

    return false;
  }


}
