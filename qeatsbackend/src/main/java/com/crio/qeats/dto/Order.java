package com.crio.qeats.dto;

import java.util.ArrayList;
import java.util.List;
import javax.validation.constraints.NotNull;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Order {

  @NotNull
  private String id;

  @NotNull
  private String restaurantId;

  @NotNull
  private String userId;

  @NotNull
  private List<Item> items = new ArrayList();

  @NotNull
  private int total;

  @NotNull
  private String timePlaced;

}