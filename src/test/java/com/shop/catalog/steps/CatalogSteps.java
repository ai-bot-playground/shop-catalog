package com.shop.catalog.steps;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Value;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.assertj.core.api.Assertions.assertThat;

public class CatalogSteps {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final HttpClient http = HttpClient.newHttpClient();

    @Value("${local.server.port}")
    private int port;

    private int status;
    private JsonNode body;

    @Given("the catalog contains the seeded products")
    public void theCatalogContainsTheSeededProducts() {
        get("/products?page=0&size=50");
        assertThat(status).isEqualTo(200);
        assertThat(body.path("content")).isNotEmpty();
    }

    @When("I request page {int} of products with size {int}")
    public void iRequestPageOfProducts(int page, int size) {
        get("/products?page=" + page + "&size=" + size);
    }

    @When("I request product {int}")
    public void iRequestProduct(int id) {
        get("/products/" + id);
    }

    @When("I search products for {string}")
    public void iSearchProductsFor(String q) {
        get("/products/search?q=" + q);
    }

    @Then("the response status is {int}")
    public void theResponseStatusIs(int code) {
        assertThat(status).isEqualTo(code);
    }

    @Then("the page contains {int} products")
    public void thePageContainsProducts(int n) {
        assertThat(body.path("content")).hasSize(n);
    }

    @Then("the page reports the total number of products")
    public void thePageReportsTheTotal() {
        boolean hasTotal = body.path("page").has("totalElements") || body.has("totalElements");
        assertThat(hasTotal).as("page total present").isTrue();
    }

    @Then("the product exposes id, name, description, price, imageUrl and category")
    public void theProductExposesFields() {
        assertThat(body.has("id") && body.has("name") && body.has("description")
                && body.has("price") && body.has("imageUrl") && body.has("category"))
                .as("product fields present").isTrue();
    }

    @Then("every returned product name contains {string} ignoring case")
    public void everyReturnedProductNameContains(String q) {
        for (JsonNode p : body.path("content")) {
            assertThat(p.path("name").asText().toLowerCase()).contains(q.toLowerCase());
        }
    }

    @Then("the product has no stock or availability field")
    public void theProductHasNoStockField() {
        assertThat(body.has("stock") || body.has("available") || body.has("availability"))
                .as("catalog must not expose stock").isFalse();
    }

    private void get(String path) {
        try {
            HttpResponse<String> r = http.send(
                    HttpRequest.newBuilder(URI.create("http://localhost:" + port + path)).GET().build(),
                    HttpResponse.BodyHandlers.ofString());
            status = r.statusCode();
            body = (r.body() == null || r.body().isBlank())
                    ? MAPPER.createObjectNode()
                    : MAPPER.readTree(r.body());
        } catch (Exception e) {
            throw new RuntimeException("GET " + path + " failed", e);
        }
    }
}
