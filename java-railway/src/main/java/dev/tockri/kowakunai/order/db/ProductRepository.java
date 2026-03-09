package dev.tockri.kowakunai.order.db;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class ProductRepository {
  private final List<Product> products =
      List.of(
          new Product(1L, "りんご"),
          new Product(2L, "みかん"),
          new Product(3L, "バナナ"),
          new Product(4L, "ぶどう"));

  public Optional<Product> findByName(String name) {
    return products.stream().filter(p -> p.name().equals(name)).findFirst();
  }
}
