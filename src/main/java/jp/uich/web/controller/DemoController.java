package jp.uich.web.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import jp.uich.web.controler.annotation.ApiVersion;
import lombok.Builder;
import lombok.Value;

@RestController
public class DemoController {

  @Builder
  @Value
  static class Item1 {
    private Integer id;
    private String name;
  }

  @Builder
  @Value
  static class Item2 {
    private Long id;
    private String name;
    private Double price;
  }

  @Builder
  @Value
  static class User {
    private Long id;
    private String name;
  }

  @GetMapping("/items/{id}")
  @ApiVersion("1.0")
  public Item1 getItem(@PathVariable Integer id) {
    return Item1.builder()
      .id(id)
      .name("バッグ")
      .build();
  }

  @GetMapping("/items/{id}")
  @ApiVersion(greaterThan = "1.0")
  public Item2 getItem(@PathVariable Long id) {
    return Item2.builder()
      .id(id)
      .name("バッグ")
      .price(10.9)
      .build();
  }

  @GetMapping("/users/{id}")
  @ApiVersion(supported = { "1.0", "3.1" })
  public User getUser(@PathVariable Long id) {
    return User.builder()
      .id(id)
      .name("Kenny")
      .build();
  }

}
