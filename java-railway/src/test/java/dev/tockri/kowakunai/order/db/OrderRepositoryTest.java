package dev.tockri.kowakunai.order.db;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jdbc.test.autoconfigure.DataJdbcTest;
import org.springframework.test.context.jdbc.Sql;

@DataJdbcTest
class OrderRepositoryTest {

  @Autowired OrderRepository orderRepository;

  @Test
  @Sql("classpath:order/OrderRepositoryTest/setup.sql")
  @DisplayName("注文を明細付きで読み出せる")
  void canFetch() {
    var order = orderRepository.findById(1L).orElseThrow();

    assertThat(order.customerName()).isEqualTo("山田太郎");
    assertThat(order.totalAmount()).isEqualByComparingTo(1980L);
    assertThat(order.details()).hasSize(2);
    assertThat(order.details().get(0).productName()).isEqualTo("コーヒー豆 200g");
    assertThat(order.details().get(1).productName()).isEqualTo("ドリップフィルター");
  }

  @Test
  @DisplayName("注文を明細付きで保存できる")
  void canSave() {
    var details =
        List.of(new OrderDetail(null, "紅茶 50g", 3, 600), new OrderDetail(null, "クッキー", 1, 450));
    var order = new Order(null, "佐藤次郎", LocalDateTime.of(2026, 3, 7, 9, 0), 2250L, details);

    var saved = orderRepository.save(order);

    assertThat(saved.id()).isNotNull();

    var found = orderRepository.findById(saved.id()).orElseThrow();
    assertThat(found.customerName()).isEqualTo("佐藤次郎");
    assertThat(found.details()).hasSize(2);
    assertThat(found.details().get(0).productName()).isEqualTo("紅茶 50g");
    assertThat(found.details().get(0).quantity()).isEqualTo(3);
  }
}
