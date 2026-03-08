package dev.tockri.kowakunai.order;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.commons.annotation.Testable;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import dev.tockri.kowakunai.order.db.Order;
import dev.tockri.kowakunai.order.db.OrderDetail;
import dev.tockri.kowakunai.order.db.OrderRepository;
import dev.tockri.kowakunai.order.db.Product;
import dev.tockri.kowakunai.order.db.ProductRepository;
import dev.tockri.kowakunai.order.db.Stock;
import dev.tockri.kowakunai.order.db.StockRepository;
import dev.tockri.kowakunai.order.dto.OrderFailureResponse;
import dev.tockri.kowakunai.order.dto.OrderRequest;
import dev.tockri.kowakunai.order.dto.OrderRequestDetail;
import dev.tockri.kowakunai.order.dto.OrderSuccessResponse;
import dev.tockri.kowakunai.util.Failure;
import dev.tockri.kowakunai.util.Success;

@ExtendWith(MockitoExtension.class)
@Testable
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
        when(clock.instant()).thenReturn(now.atZone(ZoneId.systemDefault()).toInstant());
        when(clock.getZone()).thenReturn(ZoneId.systemDefault());
    }

    @Nested
    @DisplayName("placeOrder")
    class PlaceOrderTest {
        @Test
        @DisplayName("注文が成功した場合、成功レスポンスを返すこと")
        void shouldReturnSuccessResponse() {
            mockNow(TIME_1005);
            var request = createSampleOrderRequest("田中花子", TIME_1000, "1:りんご:2:500");

            when(productRepository.findByName("りんご")).thenReturn(Optional.of(new Product(1L, "りんご")));
            when(stockRepository.findByProductId(1L)).thenReturn(Optional.of(new Stock(1L, 2)));
            when(orderRepository.save(any(Order.class))).thenReturn(
                    new Order(10L, "田中花子", TIME_1000, 1000,
                            List.of(new OrderDetail(1L, "りんご", 2, 500))));

            var result = sut.placeOrder(request);

            assertThat(result).isEqualTo(new OrderSuccessResponse(10L, "田中花子", TIME_1000, 1000));
        }

        @Test
        @DisplayName("注文の検証に失敗した場合、失敗レスポンスを返し保存しないこと")
        void shouldReturnFailureResponseWhenValidationFails() {
            mockNow(TIME_1000);
            var request = createSampleOrderRequest("田中花子", TIME_1010, "1:りんご:2:500");

            var result = sut.placeOrder(request);

            assertThat(result).isEqualTo(new OrderFailureResponse(List.of("注文日時が不正です")));
            verify(orderRepository, org.mockito.Mockito.never()).save(any(Order.class));
        }

    }

    @Nested
    @DisplayName("validate")
    class ValidateTest {
        @Test
        @DisplayName("妥当な注文は合計金額付きで成功すること")
        void shouldReturnValidOrder() {
            mockNow(TIME_1005);
            var request = createSampleOrderRequest("田中花子", TIME_1000,
                    "1:りんご:2:500", "2:みかん:1:300");
            when(productRepository.findByName("りんご")).thenReturn(Optional.of(new Product(1L, "りんご")));
            when(productRepository.findByName("みかん")).thenReturn(Optional.of(new Product(2L, "みかん")));

            var result = sut.validate(request);

            assertThat(result).isEqualTo(new Success<>(new OrderService.ValidOrder(
                    "田中花子", TIME_1000,
                    List.of(
                            new OrderService.ValidOrderDetail(1, new Product(1L, "りんご"), 2, 500),
                            new OrderService.ValidOrderDetail(2, new Product(2L, "みかん"), 1, 300)),
                    1300)));
        }

        @Test
        @DisplayName("将来の日付の注文はエラーになること")
        void shouldFailOnFutureOrderDate() {
            mockNow(TIME_1000);
            // Arrange
            var request = createSampleOrderRequest("田中花子", TIME_1010, "1:りんご:1:500");

            // Act
            var result = sut.validate(request);

            // Assert
            assertThat(result).isInstanceOf(Failure.class);
            switch (result) {
                case Failure<OrderService.ValidOrder>(var errors) -> assertThat(errors)
                        .containsExactly("注文日時が不正です");
                default -> fail();
            }
        }
    }

    @Nested
    @DisplayName("validateDetail")
    class ValidateDetailTest {
        @Test
        @DisplayName("存在する商品が指定された場合、成功すること")
        void shouldSucceedOnExistingProduct() {
            var product = new Product(1L, "りんご");
            var requestDetail = new OrderRequestDetail(1, "りんご", 2, 500);
            when(productRepository.findByName("りんご")).thenReturn(Optional.of(product));

            var result = sut.validateDetail(requestDetail);

            assertThat(result).isEqualTo(new Success<>(
                    new OrderService.ValidOrderDetail(1, product, 2, 500)));
        }

        @Test
        @DisplayName("存在しない商品が指定された場合、エラーになること")
        void shouldFailOnNonExistingProduct() {
            // Arrange
            var requestDetail = new OrderRequestDetail(1, "存在しない商品", 1, 500);
            when(productRepository.findByName("存在しない商品")).thenReturn(Optional.empty());

            // Act
            var result = sut.validateDetail(requestDetail);

            // Assert
            assertThat(result).isInstanceOf(Failure.class);
            switch (result) {
                case Failure<OrderService.ValidOrderDetail>(var errors) -> assertThat(errors)
                        .containsExactly("注文詳細[1]の商品名が不正です");
                default -> fail();
            }
        }

    }

    @Nested
    @DisplayName("checkStock")
    class CheckStockTest {
        @Test
        @DisplayName("在庫が十分にある場合、成功すること")
        void shouldSucceedWhenStockIsSufficient() {
            var validOrder = createSampleValidOrder("田中花子", TIME_1000, "1:りんご:2:500");

            when(stockRepository.findByProductId(1L)).thenReturn(Optional.of(new Stock(1L, 2)));

            var result = sut.checkStock(validOrder);

            assertThat(result).isEqualTo(new Success<>(validOrder));
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
            assertThat(result).isInstanceOf(Failure.class);
            switch (result) {
                case Failure<OrderService.ValidOrder>(var errors) -> assertThat(errors)
                        .containsExactly("注文詳細[1]の商品\u300cりんご\u300dの在庫が不足しています");
                default -> fail();
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
            var validOrder = createSampleValidOrder("田中花子", TIME_1000, "1:りんご:3:500");

            when(orderRepository.save(any(Order.class))).thenReturn(createSampleOrder());

            // Act
            sut.saveOrder(validOrder);

            // Assert
            verify(orderRepository).save(orderCaptor.capture());
            var savedOrder = orderCaptor.getValue();
            assertThat(savedOrder.customerName()).isEqualTo("田中花子");
            assertThat(savedOrder.orderTime()).isEqualTo(LocalDateTime.of(2026, 3, 8, 10, 0));
            assertThat(savedOrder.totalAmount()).isEqualTo(1500);
        }

        @Test
        @DisplayName("保存時に例外が発生した場合、エラーになること")
        void shouldFailWhenRepositorySaveThrowsException() {
            var validOrder = createSampleValidOrder("田中花子", TIME_1000, "1:りんご:3:500");
            when(orderRepository.save(any(Order.class))).thenThrow(new RuntimeException("db error"));

            var result = sut.saveOrder(validOrder);

            assertThat(result).isEqualTo(new Failure<>(List.of("注文の保存に失敗しました")));
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
        long total = validDetails.stream().mapToLong(d -> d.quantity() * d.unitPrice()).sum();
        return new OrderService.ValidOrder(customerName, orderDateTime, validDetails, total);
    }

    private static OrderRequest createSampleOrderRequest(String customerName,
            LocalDateTime orderDateTime, String... details) {
        return new OrderRequest(customerName, orderDateTime, Stream.of(details).map(Detail::parse)
                .map(d -> new OrderRequestDetail(d.index, d.productName, d.quantity, d.unitPrice))
                .toList());
    }

    private static Order createSampleOrder() {
        return new Order(1L, "田中花子", LocalDateTime.of(2026, 3, 8, 10, 0), 1000,
                List.of(new OrderDetail(1L, "りんご", 2, 500)));
    }
}
