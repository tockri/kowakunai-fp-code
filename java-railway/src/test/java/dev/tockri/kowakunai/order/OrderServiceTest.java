package dev.tockri.kowakunai.order;

import dev.tockri.kowakunai.order.db.*;
import dev.tockri.kowakunai.order.dto.OrderRequest;
import dev.tockri.kowakunai.order.dto.OrderRequestDetail;
import dev.tockri.kowakunai.util.Failure;
import dev.tockri.kowakunai.util.Success;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    Clock clock;

    @Mock
    OrderRepository orderRepository;

    @Mock
    ProductRepository productRepository;

    @Mock
    StockRepository stockRepository;

    @InjectMocks
    OrderService sut;

    @Captor
    ArgumentCaptor<Order> orderCaptor;

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
        @DisplayName("注文が成功した場合、成功レスポンスを返す")
        void shouldReturnSuccessResponse() {
            // Arrange
            mockNow(TIME_1005);
            when(productRepository.findByName(any())).thenReturn(Optional.of(new Product(1L, "りんご")));
            when(stockRepository.findByProductId(any())).thenReturn(Optional.of(new Stock(1L, 2)));
            when(orderRepository.save(any(Order.class))).thenReturn(
                    new Order(null, null, null, 0, null));

            var request = createSampleOrderRequest("田中花子", TIME_1000, "1:りんご:2:500");

            // Act
            sut.placeOrder(request);

            // Assert
            // I/Oが順番に呼ばれていることだけ確認する
            var inOrder = Mockito.inOrder(productRepository, stockRepository, orderRepository);
            inOrder.verify(productRepository).findByName("りんご");
            inOrder.verify(stockRepository).findByProductId(1L);
            inOrder.verify(orderRepository).save(any(Order.class));
        }

        @Test
        @DisplayName("注文の検証に失敗した場合、失敗レスポンスを返し保存しない")
        void shouldReturnFailureResponseWhenValidationFails() {
            // Arrange
            mockNow(TIME_1000);
            var request = createSampleOrderRequest("田中花子", TIME_1010, "1:りんご:2:500");

            // Act
            var result = sut.placeOrder(request);

            // Assert
            if (result instanceof Failure<Order>(var errors)) {
                assertThat(errors).containsExactly("注文日時が不正です");
            } else {
                fail();
            }
            verify(orderRepository, never()).save(any(Order.class));
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
            var request = createSampleOrderRequest("Test Bob", TIME_1000,
                    "1:りんご:2:500", "2:みかん:1:300");

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
            var request = createSampleOrderRequest("田中花子", TIME_1010,
                    "1:りんご:1:500");

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
        @DisplayName("在庫が十分にある場合、成功すること")
        void shouldSucceedWhenStockIsSufficient() {
            // Arrange
            var validOrder = createSampleValidOrder("田中花子", TIME_1000, "1:りんご:2:500");
            when(stockRepository.findByProductId(1L)).thenReturn(Optional.of(new Stock(1L, 2)));

            // Act
            var result = sut.checkStock(validOrder);

            // Assert
            if (result instanceof Success<OrderService.ValidOrder>(var order)) {
                assertThat(order).isSameAs(validOrder);
            } else {
                fail();
            }
        }

        @Test
        @DisplayName("在庫が不足している場合、エラーになること")
        void shouldFailWhenStockIsInsufficient() {
            // Arrange
            var validOrder = createSampleValidOrder("田中花子", TIME_1000, "1:りんご:3:500");

            when(stockRepository.findByProductId(1L)).thenReturn(Optional.of(new Stock(1L, 2)));

            // Act
            var result = sut.checkStock(validOrder);

            // Assert
            if (result instanceof Failure<OrderService.ValidOrder>(var errors)) {
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
        @DisplayName("注文が保存されること")
        void shouldSaveOrder() {
            // Arrange
            var validOrder = createSampleValidOrder("Test Alice", TIME_1000, "1:りんご:3:500");

            when(orderRepository.save(any(Order.class))).thenReturn(new Order(null, null, null, 0, null));

            // Act
            sut.saveOrder(validOrder);

            // Assert
            verify(orderRepository).save(orderCaptor.capture());
            var savedOrder = orderCaptor.getValue();
            assertEquals("Test Alice", savedOrder.customerName());
            assertEquals(TIME_1000, savedOrder.orderTime());
            assertEquals(1500, savedOrder.totalAmount());
        }

        @Test
        @DisplayName("保存時に例外が発生した場合、エラーになること")
        void shouldFailWhenRepositorySaveThrowsException() {
            // Arrange
            var validOrder = createSampleValidOrder("test user", TIME_1010, "1:りんご:3:500");
            when(orderRepository.save(any(Order.class))).thenThrow(new RuntimeException("db error"));

            // Act
            var result = sut.saveOrder(validOrder);

            // Assert
            if (result instanceof Failure<Order>(var errors)) {
                assertThat(errors).containsExactly("注文の保存に失敗しました");
            } else {
                fail();
            }
        }
    }

    record Detail(int index, String productName, int quantity, int unitPrice) {
        static Detail parse(String expr) {
            var parts = expr.split(":");
            return new Detail(Integer.parseInt(parts[0]), parts[1], Integer.parseInt(parts[2]),
                    Integer.parseInt(parts[3]));
        }
    }

    private static OrderService.ValidOrder createSampleValidOrder(String customerName,
                                                                  LocalDateTime orderDateTime, String... details) {
        var validDetails = Stream.of(details).map(Detail::parse)
                .map(d -> new OrderService.ValidOrderDetail(d.index, new Product(1L, d.productName),
                        d.quantity, d.unitPrice))
                .toList();
        long total = validDetails.stream().mapToLong(d -> (long) d.quantity() * d.unitPrice()).sum();
        return new OrderService.ValidOrder(customerName, orderDateTime, validDetails, total);
    }

    private static OrderRequest createSampleOrderRequest(String customerName,
                                                         LocalDateTime orderDateTime, String... details) {
        var requestDetails = Stream.of(details).map(Detail::parse)
                .map(d -> new OrderRequestDetail(d.index, d.productName, d.quantity, d.unitPrice))
                .toList();

        return new OrderRequest(customerName, orderDateTime, requestDetails);
    }
}
