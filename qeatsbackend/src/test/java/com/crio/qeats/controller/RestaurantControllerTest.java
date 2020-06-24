/*
 *
 *  * Copyright (c) Crio.Do 2019. All rights reserved
 *
 */

package com.crio.qeats.controller;

import static com.crio.qeats.controller.RestaurantController.CART_API;
import static com.crio.qeats.controller.RestaurantController.CART_CLEAR_API;
import static com.crio.qeats.controller.RestaurantController.CART_ITEM_API;
import static com.crio.qeats.controller.RestaurantController.GET_ORDERS_API;
import static com.crio.qeats.controller.RestaurantController.MENU_API;
import static com.crio.qeats.controller.RestaurantController.POST_ORDER_API;
import static com.crio.qeats.controller.RestaurantController.RESTAURANTS_API;
import static com.crio.qeats.controller.RestaurantController.RESTAURANT_API_ENDPOINT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.crio.qeats.QEatsApplication;
import com.crio.qeats.dto.Cart;
import com.crio.qeats.dto.Order;
import com.crio.qeats.exchanges.GetMenuResponse;
import com.crio.qeats.exchanges.GetRestaurantsRequest;
import com.crio.qeats.exchanges.GetRestaurantsResponse;
import com.crio.qeats.services.CartAndOrderService;
import com.crio.qeats.services.MenuService;
import com.crio.qeats.services.RestaurantService;
import com.crio.qeats.utils.FixtureHelpers;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.util.UriComponentsBuilder;

// TODO: CRIO_TASK_MODULE_RESTAURANTSAPI - Pass all the RestaurantController test cases.
// Make modifications to the tests if necessary.
// Test RestaurantController by mocking RestaurantService.
@SpringBootTest(classes = {QEatsApplication.class})
@MockitoSettings(strictness = Strictness.STRICT_STUBS)
@AutoConfigureMockMvc
public class RestaurantControllerTest {

  //FIXME: REVIEW the api names
  private static final String RESTAURANT_API_URI = RESTAURANT_API_ENDPOINT + RESTAURANTS_API;
  private static final String MENU_API_URI = RESTAURANT_API_ENDPOINT + MENU_API;
  private static final String CART_API_URI = RESTAURANT_API_ENDPOINT + CART_API;
  private static final String ADD_REMOVE_CART_API_URI = RESTAURANT_API_ENDPOINT + CART_ITEM_API;
  private static final String CLEAR_CART_API_URI = RESTAURANT_API_ENDPOINT + CART_CLEAR_API;
  private static final String POST_ORDER_API_URI = RESTAURANT_API_ENDPOINT + POST_ORDER_API;
  private static final String LIST_ORDERS_API_URI = RESTAURANT_API_ENDPOINT + GET_ORDERS_API;

  private static final String FIXTURES = "fixtures/exchanges";
  private ObjectMapper objectMapper;

  private MockMvc mvc;

  @MockBean
  private RestaurantService restaurantService;

  @MockBean
  private MenuService menuService;

  @MockBean
  private CartAndOrderService cartAndOrderService;

  @InjectMocks
  private RestaurantController restaurantController;

  @BeforeEach
  public void setup() {
    objectMapper = new ObjectMapper();

    MockitoAnnotations.initMocks(this);

    mvc = MockMvcBuilders.standaloneSetup(restaurantController).build();
  }

  @Test
  public void correctQueryReturnsOkResponseAndListOfRestaurants() throws Exception {
    // Sample response
    GetRestaurantsResponse sampleResponse = loadSampleResponseList();
    assertNotNull(sampleResponse);

    when(restaurantService
            .findAllRestaurantsCloseBy(any(GetRestaurantsRequest.class), any(LocalTime.class)))
            .thenReturn(sampleResponse);

    ArgumentCaptor<GetRestaurantsRequest> argumentCaptor = ArgumentCaptor
            .forClass(GetRestaurantsRequest.class);

    URI uri = UriComponentsBuilder
            .fromPath(RESTAURANT_API_URI)
            .queryParam("latitude", "20.21")
            .queryParam("longitude", "30.31")
            .build().toUri();

    assertEquals(RESTAURANT_API_URI + "?latitude=20.21&longitude=30.31", uri.toString());

    MockHttpServletResponse response = mvc.perform(
            get(uri.toString()).accept(APPLICATION_JSON_UTF8)
    ).andReturn().getResponse();

    assertEquals(HttpStatus.OK.value(), response.getStatus());

    verify(restaurantService, times(1))
            .findAllRestaurantsCloseBy(argumentCaptor.capture(), any(LocalTime.class));

    assertEquals("20.21", argumentCaptor.getValue().getLatitude().toString());

    assertEquals("30.31", argumentCaptor.getValue().getLongitude().toString());

  }

  @Test
  public void getRestaurantsBySearchStringAndLatLong() throws Exception {
    // Sample response
    GetRestaurantsResponse sampleResponse = loadSampleResponseList();
    assertNotNull(sampleResponse);

    when(restaurantService
            .findAllRestaurantsCloseBy(any(GetRestaurantsRequest.class), any(LocalTime.class)))
            .thenReturn(sampleResponse);

    ArgumentCaptor<GetRestaurantsRequest> argumentCaptor = ArgumentCaptor
            .forClass(GetRestaurantsRequest.class);

    URI uri = UriComponentsBuilder
            .fromPath(RESTAURANT_API_URI)
            .queryParam("latitude", "20.21")
            .queryParam("longitude", "30.31")
            .queryParam("searchFor", "Briyani")
            .build().toUri();

    assertEquals(RESTAURANT_API_URI + "?latitude=20.21&longitude=30.31&searchFor=Briyani",
            uri.toString());

    MockHttpServletResponse response = mvc.perform(
            get(uri.toString()).accept(APPLICATION_JSON_UTF8)
    ).andReturn().getResponse();

    assertEquals(HttpStatus.OK.value(), response.getStatus());

    verify(restaurantService, times(1))
            .findRestaurantsBySearchQuery(argumentCaptor.capture(), any(LocalTime.class));

    assertEquals("20.21", argumentCaptor.getValue().getLatitude().toString());

    assertEquals("30.31", argumentCaptor.getValue().getLongitude().toString());

    assertEquals("Briyani", argumentCaptor.getValue().getSearchFor());

  }

  @Test
  public void invalidLatitudeResultsInBadHttpRequest() throws Exception {
    URI uri = UriComponentsBuilder
            .fromPath(RESTAURANT_API_URI)
            .queryParam("latitude", "91")
            .queryParam("longitude", "20")
            .build().toUri();

    assertEquals(RESTAURANT_API_URI + "?latitude=91&longitude=20", uri.toString());

    // calling api without latitude and longitude
    MockHttpServletResponse response = mvc.perform(
            get(uri.toString()).accept(APPLICATION_JSON_UTF8)
    ).andReturn().getResponse();

    assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatus());

    uri = UriComponentsBuilder
            .fromPath(RESTAURANT_API_URI)
            .queryParam("latitude", "-91")
            .queryParam("longitude", "20")
            .build().toUri();

    assertEquals(RESTAURANT_API_URI + "?latitude=-91&longitude=20", uri.toString());

    // calling api without latitude and longitude
    response = mvc.perform(
            get(uri.toString()).accept(APPLICATION_JSON_UTF8)
    ).andReturn().getResponse();

    assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatus());
  }

  //-90 TO 90 latitude
  @Test
  public void invalidLongitudeResultsInBadHttpRequest() throws Exception {
    URI uri = UriComponentsBuilder
            .fromPath(RESTAURANT_API_URI)
            .queryParam("latitude", "10")
            .queryParam("longitude", "181")
            .build().toUri();

    assertEquals(RESTAURANT_API_URI + "?latitude=10&longitude=181", uri.toString());

    // calling api without latitude and longitude
    MockHttpServletResponse response = mvc.perform(
            get(uri.toString()).accept(APPLICATION_JSON_UTF8)
    ).andReturn().getResponse();

    assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatus());

    uri = UriComponentsBuilder
            .fromPath(RESTAURANT_API_URI)
            .queryParam("latitude", "10")
            .queryParam("longitude", "-181")
            .build().toUri();

    assertEquals(RESTAURANT_API_URI + "?latitude=10&longitude=-181", uri.toString());

    // calling api without latitude and longitude
    response = mvc.perform(
            get(uri.toString()).accept(APPLICATION_JSON_UTF8)
    ).andReturn().getResponse();

    assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatus());
  }

  @Test
  public void incorrectlySpelledLongitudeParamResultsInBadHttpRequest() throws Exception {
    // mocks not required, since validation will fail before that.
    URI uri = UriComponentsBuilder
            .fromPath(RESTAURANT_API_URI)
            .queryParam("latitude", "10")
            .queryParam("longitue", "20")
            .build().toUri();

    assertEquals(RESTAURANT_API_URI + "?latitude=10&longitue=20", uri.toString());

    // calling api without latitude and longitude
    MockHttpServletResponse response = mvc.perform(
            get(uri.toString()).accept(APPLICATION_JSON_UTF8)
    ).andReturn().getResponse();

    assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatus());
  }

  @Test
  public void incorrectlySpelledLatitudeParamResultsInBadHttpRequest() throws Exception {
    // mocks not required, since validation will fail before that.
    URI uri = UriComponentsBuilder
            .fromPath(RESTAURANT_API_URI)
            .queryParam("laitude", "10")
            .queryParam("longitude", "20")
            .build().toUri();

    assertEquals(RESTAURANT_API_URI + "?laitude=10&longitude=20", uri.toString());

    // calling api without latitude and longitude
    MockHttpServletResponse response = mvc.perform(
            get(uri.toString()).accept(APPLICATION_JSON_UTF8)
    ).andReturn().getResponse();

    assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatus());
  }

  @Test
  public void noRequestParamResultsInBadHttpReuest() throws Exception {
    // mocks not required, since validation will fail before that.
    URI uri = UriComponentsBuilder
            .fromPath(RESTAURANT_API_URI)
            .build().toUri();

    assertEquals(RESTAURANT_API_URI, uri.toString());

    // calling api without latitude and longitude
    MockHttpServletResponse response = mvc.perform(
            get(uri.toString()).accept(APPLICATION_JSON_UTF8)
    ).andReturn().getResponse();

    assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatus());
  }

  @Test
  public void missingLongitudeParamResultsInBadHttpRequest() throws Exception {
    // calling api without latitude
    URI uri = UriComponentsBuilder
            .fromPath(RESTAURANT_API_URI)
            .queryParam("latitude", "20.21")
            .build().toUri();

    assertEquals(RESTAURANT_API_URI + "?latitude=20.21", uri.toString());

    MockHttpServletResponse response = mvc.perform(
            get(uri.toString()).accept(APPLICATION_JSON_UTF8)
    ).andReturn().getResponse();

    assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatus());
  }

  @Test
  public void missingLatitudeParamResultsInBadHttpRequest() throws Exception {
    // calling api without longitude
    URI uri = UriComponentsBuilder
            .fromPath(RESTAURANT_API_URI)
            .queryParam("longitude", "30.31")
            .build().toUri();

    assertEquals(RESTAURANT_API_URI + "?longitude=30.31", uri.toString());

    MockHttpServletResponse response = mvc.perform(
            get(uri.toString()).accept(APPLICATION_JSON_UTF8)
    ).andReturn().getResponse();

    assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatus());
  }

  @Test
  public void missingRestaurantIdParamResultsInBadHttpRequest() throws Exception {
    // calling api without longitude
    URI uri = UriComponentsBuilder
            .fromPath(MENU_API_URI)
            .build().toUri();

    assertEquals(MENU_API_URI, uri.toString());

    MockHttpServletResponse response = mvc.perform(
            get(uri.toString()).accept(APPLICATION_JSON_UTF8)
    ).andReturn().getResponse();

    assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatus());
  }


  @Test
  public void getMenuResponseTest() throws Exception {
    GetMenuResponse expectedMenuResponse = loadMenuResponse();

    URI uri = UriComponentsBuilder
            .fromPath(MENU_API_URI)
            .queryParam("restaurantId", "11")
            .build().toUri();

    when(menuService.findMenu(any(String.class))).thenReturn(expectedMenuResponse);

    assertEquals(MENU_API_URI + "?restaurantId=11", uri.toString());

    MockHttpServletResponse response = mvc.perform(
            get(uri.toString()).accept(APPLICATION_JSON_UTF8)
    ).andReturn().getResponse();

    GetMenuResponse actualResponse = objectMapper
            .readValue(response.getContentAsString(), GetMenuResponse.class);

    assertEquals(expectedMenuResponse.toString(), actualResponse.toString());
    assertEquals(HttpStatus.OK.value(), response.getStatus());

    ArgumentCaptor<String> argumentCaptor = ArgumentCaptor.forClass(String.class);
    verify(menuService, times(1)).findMenu(argumentCaptor.capture());
    assertEquals("11", argumentCaptor.getValue().toString());
  }


  @Test
  public void getCartResponseTest() throws Exception {
    Cart expectedCart = loadSampleCarts();

    URI uri = UriComponentsBuilder
            .fromPath(CART_API_URI)
            .queryParam("userId", "arun")
            .build().toUri();
    assertEquals(CART_API_URI + "?userId=arun", uri.toString());

    when(cartAndOrderService.findOrCreateCart(any(String.class))).thenReturn(expectedCart);

    MockHttpServletResponse response = mvc.perform(
            get(uri.toString()).accept(APPLICATION_JSON_UTF8)
    ).andReturn().getResponse();

    assertEquals(HttpStatus.OK.value(), response.getStatus());

    Cart actualCart = objectMapper.readValue(response.getContentAsString(), Cart.class);
    assertEquals(expectedCart.toString(), actualCart.toString());

    ArgumentCaptor<String> userId = ArgumentCaptor.forClass(String.class);

    verify(cartAndOrderService, times(1)).findOrCreateCart(userId.capture());

    assertEquals("arun", userId.getValue());
  }


  private Cart parse(String cartString) throws IOException {
    return objectMapper.readValue(cartString, Cart.class);
  }

  @Test
  public void getCartReturnsTheCartIfExists() throws Exception {
    Cart expectedCart = loadSampleCarts();

    URI uri = UriComponentsBuilder
            .fromPath(CART_API_URI)
            .queryParam("userId", "Bunny")
            .build().toUri();
    assertEquals(CART_API_URI + "?userId=Bunny", uri.toString());

    when(cartAndOrderService.findOrCreateCart(any(String.class))).thenReturn(expectedCart);

    MockHttpServletResponse response = mvc.perform(
            get(uri.toString()).accept(APPLICATION_JSON_UTF8)
    ).andReturn().getResponse();

    assertEquals(HttpStatus.OK.value(), response.getStatus());
    assertEquals(expectedCart.toString(), parse(response.getContentAsString()).toString());
  }

  @Test
  public void testAddToCartApi() throws Exception {

    Cart expectedCart = new Cart();
    expectedCart.setId("1");
    expectedCart.setUserId("arun");
    expectedCart.setRestaurantId("10");

    URI uri = UriComponentsBuilder
            .fromPath(ADD_REMOVE_CART_API_URI)
            .build().toUri();

    assertEquals("/qeats/v1/cart/item", uri.toString());

    String addCartItemBody = loadJsonBody("cart_add_or_remove_item_body.json");

    final ResultActions resultActions = mvc.perform(post(ADD_REMOVE_CART_API_URI)
            .contentType(APPLICATION_JSON_UTF8)
            .content(addCartItemBody)).andExpect(status().isOk());

    ArgumentCaptor<String> itemId = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<String> cartId = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<String> restaurantId = ArgumentCaptor.forClass(String.class);

    verify(cartAndOrderService, times(1)).addItemToCart(
            itemId.capture(), cartId.capture(), restaurantId.capture());

    assertEquals(itemId.getValue(), "1");
    assertEquals(cartId.getValue(), "1");
    assertEquals(restaurantId.getValue(), "10");
  }


  @Test
  public void testRemoveFromCartApi() throws Exception {
    URI uri = UriComponentsBuilder
            .fromPath(ADD_REMOVE_CART_API_URI)
            .build().toUri();

    assertEquals("/qeats/v1/cart/item", uri.toString());

    String removeCartItemBody = loadJsonBody("cart_add_or_remove_item_body.json");

    final ResultActions resultActions = mvc.perform(delete(ADD_REMOVE_CART_API_URI)
            .contentType(APPLICATION_JSON_UTF8)
            .content(removeCartItemBody)).andExpect(status().isOk());

    ArgumentCaptor<String> itemId = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<String> cartId = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<String> restaurantId = ArgumentCaptor.forClass(String.class);

    verify(cartAndOrderService, times(1)).removeItemFromCart(
            itemId.capture(), cartId.capture(), restaurantId.capture());

    assertEquals(itemId.getValue(), "1");
    assertEquals(cartId.getValue(), "1");
    assertEquals(restaurantId.getValue(), "10");
  }

  //  @Test
  //  public void testClearCartApi() throws Exception {
  //    URI uri = UriComponentsBuilder
  //        .fromPath(CLEAR_CART_API_URI)
  //        .build().toUri();
  //
  //    assertEquals("/qeats/v1/cart/clear", uri.toString());
  //
  //    String removeCartItemBody = loadJsonBody("post_order_or_clear_cart_body.json");
  //
  //    final ResultActions resultActions = mvc.perform(put(CLEAR_CART_API_URI)
  //        .contentType(APPLICATION_JSON_UTF8)
  //        .content(removeCartItemBody)).andExpect(status().isOk());
  //
  //    ArgumentCaptor<String> cartId = ArgumentCaptor.forClass(String.class);
  //
  //    verify(cartAndOrderService, times(1)).clearCart(cartId.capture());
  //    assertEquals(cartId.getValue(), "1");
  //  }

  @Test
  public void postOrderReturnsOrderOnSuccess() throws Exception {

    Cart expectedCart = new Cart();
    expectedCart.setId("1");
    expectedCart.setUserId("arun");
    expectedCart.setRestaurantId("10");

    URI uri = UriComponentsBuilder
            .fromPath(POST_ORDER_API_URI)
            .build().toUri();

    assertEquals("/qeats/v1/order", uri.toString());

    String postCartItemBody = loadJsonBody("post_order_or_clear_cart_body.json");

    final ResultActions resultActions = mvc.perform(post(POST_ORDER_API_URI)
            .contentType(APPLICATION_JSON_UTF8)
            .content(postCartItemBody)).andExpect(status().isOk());

    ArgumentCaptor<String> cartId = ArgumentCaptor.forClass(String.class);

    verify(cartAndOrderService, times(1)).postOrder(
            cartId.capture());

    assertEquals(cartId.getValue(), "1");
  }


  //  @Test
  //  public void testGetAllUserOrders() throws Exception {
  //    List<Order> ordersList = loadSampleOrdersList();
  //
  //    when(cartAndOrderService.getAllUserOrders(any(String.class))).thenReturn(ordersList);
  //
  //    URI uri = UriComponentsBuilder
  //        .fromPath(LIST_ORDERS_API_URI)
  //        .queryParam("userId", "Bunny")
  //        .build().toUri();
  //
  //    assertEquals("/qeats/v1/orders?userId=Bunny", uri.toString());
  //
  //    MockHttpServletResponse response = mvc.perform(
  //        get(uri.toString()).accept(APPLICATION_JSON_UTF8)
  //    ).andReturn().getResponse();
  //
  //    assertEquals(HttpStatus.OK.value(), response.getStatus());
  //
  //    List<Order> actualListOfOrders = objectMapper.readValue(response.getContentAsString(),
  //        new TypeReference<List<Order>>() {
  //        });
  //
  //    assertEquals(ordersList.toString(), actualListOfOrders.toString());
  //
  //    ArgumentCaptor<String> userId = ArgumentCaptor.forClass(String.class);
  //
  //    verify(cartAndOrderService, times(1))
  //        .getAllUserOrders(userId.capture());
  //
  //    assertEquals("Bunny", userId.getValue());
  //  }
  //
  //  @Test
  //  public void getCartReturnEmptyCartIfUserHasNoActiveCart() throws Exception {
  //    assert (false);
  //  }
  //
  //  @Test
  //  public void addItemToCartReturnsUpdatedCart() {
  //    assert(false);
  //  }
  //
  //  @Test
  //  public void addItemToCartWithMisSpelledRequestBodyResultsInBadHttpRequest() {
  //    assert(false);
  //  }
  //
  //  //
  //  @Test
  //  public void addItemToCartWithMissingElementsInRequestBodyResultsInBadHttpRequest() {
  //    assert(false);
  //  }
  //
  //  @Test
  //  public void deleteItemFromCartReturnsUpdatedCart() {
  //    assert(false);
  //  }
  //
  //  @Test
  //  public void deleteItemToCartWithMisSpelledRequestBodyResultsInBadHttpRequest() {
  //    assert(false);
  //  }
  //
  //  @Test
  //  public void deleteItemToCartWithMissingElementsInRequestBodyResultsInBadHttpRequest() {
  //    assert(false);
  //  }
  //
  //  @Test
  //  public void clearCartReturnsEmptyCart() {
  //    assert(false);
  //  }

  //  @Test
  //  public void postOrderReturnsOrderDetails() {
  //    assert(false);
  //  }
  //
  //  @Test
  //  public void postOrderWithInvalidCartIdResultsBadHttpRequest() {
  //    assert(false);
  //  }
  //
  //  @Test
  //  public void postOrderWithMissingCartIdInRequestBodyResultsBadHttpRequest() {
  //    assert(false);
  //  }
  //
  //  @Test
  //  public void postOrderWithMisSpelledCartIdInRequestBodyResultsBadHttpRequest() {
  //    assert(false);
  //  }
  //
  //  @Test
  //  public void getOrdersReturnsListOfOrders() {
  //    assert(false);
  //  }
  //
  //  @Test
  //  public void getOrderReturnsEmptyListIfUserHasNoOrders() {
  //    assert(false);
  //  }
  //

  private String loadJsonBody(String fileName) {
    return FixtureHelpers.fixture(FIXTURES + "/" + fileName);
  }

  private Cart loadSampleCarts() throws IOException {
    String fixture =
            FixtureHelpers.fixture(FIXTURES + "/get_cart_response.json");

    return objectMapper.readValue(fixture, Cart.class);
  }

  private List<Order> loadSampleOrdersList() throws IOException {
    String fixture =
            FixtureHelpers.fixture(FIXTURES + "/initial_data_set_orders.json");

    return objectMapper.readValue(fixture,
            new TypeReference<List<Order>>() {
        });
  }

  private GetRestaurantsResponse loadSampleResponseList() throws IOException {
    String fixture =
            FixtureHelpers.fixture(FIXTURES + "/list_restaurant_response.json");

    return objectMapper.readValue(fixture,
            new TypeReference<GetRestaurantsResponse>() {
      });
  }

  private GetRestaurantsResponse loadSampleRequest() throws IOException {
    String fixture =
            FixtureHelpers.fixture(FIXTURES + "/create_restaurant_request.json");

    return objectMapper.readValue(fixture, GetRestaurantsResponse.class);
  }

  private GetMenuResponse loadMenuResponse() throws IOException {
    String fixture = FixtureHelpers.fixture(FIXTURES + "/get_menu_response.json");

    return objectMapper.readValue(fixture, GetMenuResponse.class);
  }

}
