Feature: Product catalog browsing
  As a shopper during a flash sale
  I want to browse and search the product catalog
  So that I can choose what to buy

  Background:
    Given the catalog contains the seeded products

  Scenario: List products with pagination
    When I request page 0 of products with size 2
    Then the response status is 200
    And the page contains 2 products
    And the page reports the total number of products

  Scenario: Get product details by id
    When I request product 1
    Then the response status is 200
    And the product exposes id, name, description, price, imageUrl and category

  Scenario: Requesting a non-existent product returns 404
    When I request product 999999
    Then the response status is 404

  Scenario: Search products by name (case-insensitive)
    When I search products for "keyboard"
    Then the response status is 200
    And every returned product name contains "keyboard" ignoring case

  Scenario: Catalog never exposes stock availability
    When I request product 1
    Then the product has no stock or availability field
