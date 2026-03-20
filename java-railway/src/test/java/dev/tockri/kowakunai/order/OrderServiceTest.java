package dev.tockri.kowakunai.order;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.tuple;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import dev.tockri.kowakunai.order.db.*;
import dev.tockri.kowakunai.order.dto.OrderRequest;
import dev.tockri.kowakunai.order.dto.OrderRequestDetail;
import dev.tockri.kowakunai.util.Failure;
import dev.tockri.kowakunai.util.Success;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

  @Mock Clock clock;

  @Mock OrderRepository orderRepository;

  @Mock ProductRepository productRepository;

  @Mock StockRepository stockRepository;

  @Spy @InjectMocks OrderService sut;

  @Captor ArgumentCaptor<Order> orderCaptor;

  private static final LocalDateTime TIME_1000 = LocalDateTime.of(2026, 3, 8, 10, 0);
  private static final LocalDateTime TIME_1005 = LocalDateTime.of(2026, 3, 8, 10, 5);
  private static final LocalDateTime TIME_1010 = LocalDateTime.of(2026, 3, 8, 10, 10);

  private void mockNow(LocalDateTime now) {
    var instant = now.atZone(ZoneId.systemDefault()).toInstant();
    var zone = ZoneId.systemDefault();
    when(clock.instant()).thenReturn(instant);
    when(clock.getZone()).thenReturn(zone);
  }

  @Nested
  @DisplayName("placeOrder")
  class PlaceOrderTest {
    @Test
    @DisplayName("各メソッドを順番に呼ぶ")
    void shouldReturnSuccessResponse() {
      // Arrange
      mockNow(TIME_1005);
      when(productRepository.findByName(any())).thenReturn(Optional.of(new Product(1L, "りんご")));
      when(stockRepository.findByProductId(any())).thenReturn(Optional.of(new Stock(1L, 2)));
      var sampleOrder = new Order(null, null, null, 0, null);
      when(orderRepository.save(any(Order.class))).thenReturn(sampleOrder);

      var request = createSampleOrderRequest("田中花子", TIME_1000, "1:りんご:2:500");

      // Act
      var actual = sut.placeOrder(request);

      // Assert
      // 順番に呼ばれていることだけ確認
      var inOrder = Mockito.inOrder(sut);
      inOrder.verify(sut).validateOrderTime(any());
      inOrder.verify(sut).validateProductName(any());
      inOrder.verify(sut).checkStock(any());
      inOrder.verify(sut).saveOrder(any());
      inOrder.verify(sut).sendEmail(any());
      // 最後の戻り値=saveの戻り値
      if (actual instanceof Success<Order>(var value)) {
        assertThat(value).isSameAs(sampleOrder);
      } else {
        fail();
      }
    }
  }

  @Nested
  @DisplayName("validateOrderTime")
  class ValidateOrderTimeTest {
    @Test
    @DisplayName("日付が妥当であれば成功して、引数で渡したオブジェクトを返す")
    void shouldReturnValidOrder() {
      // Arrange
      mockNow(TIME_1005);
      var request = createSampleOrderRequest("Test Bob", TIME_1000, "1:りんご:2:500", "2:みかん:1:300");

      // Act
      var result = sut.validateOrderTime(request);

      // Assert
      if (result instanceof Success<OrderRequest>(OrderRequest validOrder)) {
        assertThat(validOrder).isSameAs(request);
      } else {
        fail();
      }
    }

    @Test
    @DisplayName("将来の日付の場合、失敗を返す")
    void shouldFailOnFutureOrderDate() {
      // Arrange
      mockNow(TIME_1000);
      var request = createSampleOrderRequest("田中花子", TIME_1010, "1:りんご:1:500");

      // Act
      var result = sut.validateOrderTime(request);

      // Assert
      if (result instanceof Failure<OrderRequest>(var errors)) {
        assertThat(errors).containsExactly("注文日時が不正です");
      } else {
        fail();
      }
    }
  }

  @Nested
  @DisplayName("validateDetail")
  class ValidateDetailTest {
    @Test
    @DisplayName("存在する商品が指定された場合、成功してValidOrderDetailを返す")
    void shouldSucceedOnExistingProduct() {
      // Arrange
      var requestDetail = new OrderRequestDetail(1, "りんご", 2, 500);
      when(productRepository.findByName("りんご")).thenReturn(Optional.of(new Product(1L, "りんご")));

      // Act
      var result = sut.validateDetailProductName(requestDetail);

      // Assert
      if (result instanceof Success<OrderService.ValidOrderDetail>(var validDetail)) {
        assertEquals(1, validDetail.index());
        assertEquals(1L, validDetail.product().id());
        assertEquals("りんご", validDetail.product().name());
        assertEquals(2, validDetail.quantity());
        assertEquals(500, validDetail.unitPrice());
      } else {
        fail();
      }
    }

    @Test
    @DisplayName("存在しない商品が指定された場合、失敗してエラーを返す")
    void shouldFailOnNonExistingProduct() {
      // Arrange
      var requestDetail = new OrderRequestDetail(1, "存在しない商品", 1, 500);
      when(productRepository.findByName("存在しない商品")).thenReturn(Optional.empty());

      // Act
      var result = sut.validateDetailProductName(requestDetail);

      // Assert
      if (result instanceof Failure<OrderService.ValidOrderDetail>(var errors)) {
        assertThat(errors).containsExactly("注文詳細[1]の商品名が不正です");
      } else {
        fail();
      }
    }
  }

  @Nested
  @DisplayName("checkStock")
  class CheckStockTest {
    @Test
    @DisplayName("checkDetailStockが全て成功なら成功して引数オブジェクトをそのまま返す")
    void shouldSucceedWhenStockIsSufficient() {
      // Arrange
      var validOrder = createSampleValidOrder("田中花子", TIME_1010, "1:りんご:2:500");
      when(stockRepository.findByProductId(1L)).thenReturn(Optional.of(new Stock(1L, 2)));

      // Act
      var result = sut.checkStock(validOrder);

      // Assert
      if (result instanceof Success<OrderService.ValidOrder>(var successOrder)) {
        assertThat(successOrder).isSameAs(validOrder);
      } else {
        fail();
      }
    }

    @Test
    @DisplayName("一つでも失敗の場合、失敗になる")
    void shouldFailWhenStockIsInsufficient() {
      // Arrange
      var validOrder = createSampleValidOrder("田中花子", TIME_1000, "1:りんご:3:500", "2:みかん:1:300");

      when(stockRepository.findByProductId(1L)).thenReturn(Optional.of(new Stock(1L, 3)));
      when(stockRepository.findByProductId(2L)).thenReturn(Optional.of(new Stock(2L, 0)));

      // Act
      var result = sut.checkStock(validOrder);

      // Assert
      assertThat(result).isInstanceOf(Failure.class);
    }
  }

  @Nested
  @DisplayName("checkDetailsStock")
  class CheckDetailsStockTest {
    @Test
    @DisplayName("明細の在庫が十分にある場合、成功する")
    void shouldSucceedWhenStockIsSufficientForAllDetails() {
      // Arrange
      var validOrderDetail = new OrderService.ValidOrderDetail(1, new Product(1L, "りんご"), 2, 500);
      when(stockRepository.findByProductId(1L)).thenReturn(Optional.of(new Stock(1L, 2)));

      // Act
      var result = sut.checkDetailStock(validOrderDetail);

      // Assert
      if (result instanceof Success<OrderService.ValidOrderDetail>(var successDetail)) {
        assertThat(successDetail).isSameAs(validOrderDetail);
      } else {
        fail();
      }
    }

    @Test
    @DisplayName("在庫が不足している場合、エラーになる")
    void shouldFailWhenStockIsInsufficientForAnyDetail() {
      // Arrange
      var validOrderDetail = new OrderService.ValidOrderDetail(1, new Product(1L, "りんご"), 3, 500);
      when(stockRepository.findByProductId(1L)).thenReturn(Optional.of(new Stock(1L, 2)));

      // Act
      var result = sut.checkDetailStock(validOrderDetail);

      // Assert
      if (result instanceof Failure<OrderService.ValidOrderDetail>(var errors)) {
        assertThat(errors).containsExactly("注文詳細[1]の商品「りんご」の在庫が不足しています");
      } else {
        fail();
      }
    }
  }

  @Nested
  @DisplayName("saveOrder")
  class SaveOrderTest {
    @Test
    @DisplayName("注文が保存される")
    void shouldSaveOrder() {
      // Arrange
      var validOrder =
          createSampleValidOrder("Test Alice", TIME_1000, "1:りんご:3:500", "2:みかん:4:100");

      when(orderRepository.save(any(Order.class))).thenReturn(new Order(null, null, null, 0, null));

      // Act
      sut.saveOrder(validOrder);

      // Assert
      verify(orderRepository).save(orderCaptor.capture());
      var savedOrder = orderCaptor.getValue();
      assertEquals("Test Alice", savedOrder.customerName());
      assertEquals(TIME_1000, savedOrder.orderTime());
      assertEquals(1900, savedOrder.totalAmount());
      assertThat(savedOrder.details())
          .hasSize(2)
          .extracting("productName", "quantity", "unitPrice")
          .containsExactly(tuple("りんご", 3, 500), tuple("みかん", 4, 100));
    }
  }

  record Detail(int index, String productName, int quantity, int unitPrice) {
    static Detail parse(String expr) {
      var parts = expr.split(":");
      return new Detail(
          Integer.parseInt(parts[0]),
          parts[1],
          Integer.parseInt(parts[2]),
          Integer.parseInt(parts[3]));
    }
  }

  private static OrderService.ValidOrder createSampleValidOrder(
      String customerName, LocalDateTime orderDateTime, String... details) {
    var validDetails =
        Stream.of(details)
            .map(Detail::parse)
            .map(
                d ->
                    new OrderService.ValidOrderDetail(
                        d.index,
                        new Product((long) d.index, d.productName),
                        d.quantity,
                        d.unitPrice))
            .toList();
    long total = validDetails.stream().mapToLong(d -> (long) d.quantity() * d.unitPrice()).sum();
    return new OrderService.ValidOrder(customerName, orderDateTime, validDetails, total);
  }

  private static OrderRequest createSampleOrderRequest(
      String customerName, LocalDateTime orderDateTime, String... details) {
    var requestDetails =
        Stream.of(details)
            .map(Detail::parse)
            .map(d -> new OrderRequestDetail(d.index, d.productName, d.quantity, d.unitPrice))
            .toList();

    return new OrderRequest(customerName, orderDateTime, requestDetails);
  }
}
