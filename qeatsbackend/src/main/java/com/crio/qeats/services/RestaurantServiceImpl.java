/*
 *
 *  * Copyright (c) Crio.Do 2019. All rights reserved
 *
 */

package com.crio.qeats.services;

import com.crio.qeats.dto.Restaurant;
import com.crio.qeats.exchanges.GetRestaurantsRequest;
import com.crio.qeats.exchanges.GetRestaurantsResponse;
import com.crio.qeats.repositoryservices.RestaurantRepositoryService;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Log4j2
public class RestaurantServiceImpl implements RestaurantService {

  private final Double peakHoursServingRadiusInKms = 3.0;
  private final Double normalHoursServingRadiusInKms = 5.0;
  @Autowired
  private RestaurantRepositoryService restaurantRepositoryService;

  public double servingradius(LocalTime currentTime) {
    if (currentTime.isAfter(LocalTime.of(7, 59)) && currentTime
        .isBefore(LocalTime.of(10, 1))) {
      return peakHoursServingRadiusInKms;
    } else if ((currentTime.isAfter(LocalTime.of(13, 0)) && currentTime
        .isBefore(LocalTime.of(14, 1))) || (currentTime
        .equals(LocalTime.parse("13:00:00")))) {
      return peakHoursServingRadiusInKms;
    } else if (currentTime.isAfter(LocalTime.parse("18:59:59")) && currentTime
        .isBefore(LocalTime.parse("21:01:00"))) {
      return peakHoursServingRadiusInKms;
    } else {
      return normalHoursServingRadiusInKms;
    }
  }

  // TODO: CRIO_TASK_MODULE_RESTAURANTSAPI - Implement findAllRestaurantsCloseby.
  // Check RestaurantService.java file for the interface contract.
  @Override
  public GetRestaurantsResponse findAllRestaurantsCloseBy(
      GetRestaurantsRequest getRestaurantsRequest, LocalTime currentTime) {
    GetRestaurantsResponse getRestaurantsResponse = new GetRestaurantsResponse();
    if (currentTime.isAfter(LocalTime.of(7, 59)) && currentTime
        .isBefore(LocalTime.of(10, 1))) {
      getRestaurantsResponse.setRestaurants(
          restaurantRepositoryService.findAllRestaurantsCloseBy(
              getRestaurantsRequest.getLatitude(),
              getRestaurantsRequest.getLongitude(),
              currentTime, peakHoursServingRadiusInKms));
    } else if ((currentTime.isAfter(LocalTime.of(13, 0)) && currentTime
        .isBefore(LocalTime.of(14, 1))) || (currentTime
        .equals(LocalTime.parse("13:00:00")))) {
      getRestaurantsResponse.setRestaurants(
          restaurantRepositoryService.findAllRestaurantsCloseBy(
              getRestaurantsRequest.getLatitude(),
              getRestaurantsRequest.getLongitude(),
              currentTime, peakHoursServingRadiusInKms));
    } else if (currentTime.isAfter(LocalTime.parse("18:59:59")) && currentTime
        .isBefore(LocalTime.parse("21:01:00"))) {
      getRestaurantsResponse.setRestaurants(
          restaurantRepositoryService.findAllRestaurantsCloseBy(
              getRestaurantsRequest.getLatitude(),
              getRestaurantsRequest.getLongitude(),
              currentTime, peakHoursServingRadiusInKms));
    } else {
      getRestaurantsResponse.setRestaurants(
          restaurantRepositoryService.findAllRestaurantsCloseBy(
              getRestaurantsRequest.getLatitude(),
              getRestaurantsRequest.getLongitude(),
              currentTime, normalHoursServingRadiusInKms));
    }

    return getRestaurantsResponse;
  }

  // TODO: CRIO_TASK_MODULE_RESTAURANTSEARCH
  // Implement findRestaurantsBySearchQuery. The request object has the search string.
  // We have to combine results from multiple sources:
  // 1. Restaurants by name (exact and inexact)
  // 2. Restaurants by cuisines (also called attributes)
  // 3. Restaurants by food items it serves
  // 4. Restaurants by food item attributes (spicy, sweet, etc)
  // Remember, a restaurant must be present only once in the resulting list.
  // Check RestaurantService.java file for the interface contract.
  @Override
  public GetRestaurantsResponse findRestaurantsBySearchQuery(
      GetRestaurantsRequest getRestaurantsRequest, LocalTime currentTime) {

    List<Restaurant> restaurantssearchresults = new ArrayList<>();

    GetRestaurantsResponse getRestaurantsResponse = new GetRestaurantsResponse();
    if (getRestaurantsRequest.getSearchFor().equals("")) {
      return getRestaurantsResponse;
    }
    List<Restaurant> restaurantlistByName = restaurantRepositoryService.findRestaurantsByName(
        getRestaurantsRequest.getLatitude(),
        getRestaurantsRequest.getLongitude(), getRestaurantsRequest.getSearchFor(),
        currentTime, servingradius(currentTime));

    List<Restaurant> restaurantListByAttributes = restaurantRepositoryService
        .findRestaurantsByAttributes(
            getRestaurantsRequest.getLatitude(),
            getRestaurantsRequest.getLongitude(), getRestaurantsRequest.getSearchFor(),
            currentTime, servingradius(currentTime));

    List<Restaurant> restaurantlistByItem = restaurantRepositoryService.findRestaurantsByItemName(
        getRestaurantsRequest.getLatitude(),
        getRestaurantsRequest.getLongitude(), getRestaurantsRequest.getSearchFor(),
        currentTime, servingradius(currentTime));

    Set<Restaurant> restaurantSet = new HashSet<>();


    for (Restaurant restauranttemp : restaurantlistByName) {

      int x;
      if (restaurantSet.isEmpty()) {
        x = 0;
      } else {
        x = restaurantSet.size();
      }
      restaurantSet.add(restauranttemp);
      int y = restaurantSet.size();
      if (x != y) {
        restaurantssearchresults.add(restauranttemp);
      }
    }

    for (Restaurant restauranttemp : restaurantListByAttributes) {

      int x;
      if (restaurantSet.isEmpty()) {
        x = 0;
      } else {
        x = restaurantSet.size();
      }
      restaurantSet.add(restauranttemp);
      int y = restaurantSet.size();
      if (x != y) {
        restaurantssearchresults.add(restauranttemp);
      }
    }
    for (Restaurant restauranttemp : restaurantlistByItem) {

      int x;
      if (restaurantSet.isEmpty()) {
        x = 0;
      } else {
        x = restaurantSet.size();
      }
      restaurantSet.add(restauranttemp);
      int y = restaurantSet.size();
      if (x != y) {
        restaurantssearchresults.add(restauranttemp);
      }
    }
    List<Restaurant> restaurantlistByItemAttributes = restaurantRepositoryService
        .findRestaurantsByItemAttributes(
            getRestaurantsRequest.getLatitude(),
            getRestaurantsRequest.getLongitude(), getRestaurantsRequest.getSearchFor(),
            currentTime, servingradius(currentTime));
    for (Restaurant restauranttemp : restaurantlistByItemAttributes) {

      int x;
      if (restaurantSet.isEmpty()) {
        x = 0;
      } else {
        x = restaurantSet.size();
      }
      restaurantSet.add(restauranttemp);
      int y = restaurantSet.size();
      if (x != y) {
        restaurantssearchresults.add(restauranttemp);
      }
    }


    getRestaurantsResponse.setRestaurants(restaurantssearchresults);

    return getRestaurantsResponse;
  }

  // TODO: CRIO_TASK_MODULE_MULTITHREADING: Implement multi-threaded version of RestaurantSearch.
  // Implement variant of findRestaurantsBySearchQuery which is at least 1.5x time faster than
  // findRestaurantsBySearchQuery.
  @Override
  public GetRestaurantsResponse findRestaurantsBySearchQueryMt(
      GetRestaurantsRequest getRestaurantsRequest, LocalTime currentTime) {

    return null;
  }
}
