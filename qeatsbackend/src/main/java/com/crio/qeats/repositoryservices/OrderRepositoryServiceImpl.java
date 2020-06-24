/*
 *
 *  * Copyright (c) Crio.Do 2019. All rights reserved
 *
 */

package com.crio.qeats.repositoryservices;

import com.crio.qeats.dto.Cart;
import com.crio.qeats.dto.Order;
import com.crio.qeats.models.OrderEntity;
import com.crio.qeats.repositories.CartRepository;
import com.crio.qeats.repositories.OrderRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OrderRepositoryServiceImpl implements OrderRepositoryService {

  @Autowired
  OrderRepository orderRepository;

  @Override
  public Order placeOrder(Cart cart) {

    Order order = new Order();
    BeanUtils.copyProperties(cart, order);
    OrderEntity orderEntity = orderRepository.save(order);
    BeanUtils.copyProperties(orderEntity, order);
    return order;
  }
}