package dev.tockri.kowakunai.order;

import dev.tockri.kowakunai.order.db.*;
import dev.tockri.kowakunai.order.dto.OrderRequest;
import dev.tockri.kowakunai.order.dto.OrderRequestDetail;
import dev.tockri.kowakunai.util.Failure;
import dev.tockri.kowakunai.util.Result;
import dev.tockri.kowakunai.util.Success;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Service
@RequiredArgsConstructor
public class OrderService {
  private final Clock clock;
  private final OrderRepository orderRepository;
  private final ProductRepository productRepository;
  private final StockRepository stockRepository;

  record ValidOrder(
      String customerName,
      LocalDateTime orderDateTime,
      List<ValidOrderDetail> details,
      long totalAmount) {}

  record ValidOrderDetail(int index, Product product, int quantity, int unitPrice) {}

  public Result<Order> placeOrder(@Validated OrderRequest request) {
    return validateOrderTime(request)
        .then(this::validateProductName)
        .then(this::checkStock)
        .then(this::saveOrder)
        .then(this::sendEmail);
  }

  // 注文日時のバリデーションを行う
  Result<OrderRequest> validateOrderTime(OrderRequest request) {
    if (request.orderDateTime().isAfter(LocalDateTime.now(clock))) {
      return new Failure<>(List.of("注文日時が不正です"));
    }
    return new Success<>(request);
  }

  // 商品名のバリデーションを行う
  Result<ValidOrder> validateProductName(OrderRequest request) {
    var detailsResult = Result.collect(request.details(), this::validateDetailProductName);

    return switch (detailsResult) {
      case Success<List<ValidOrderDetail>>(var validDetails) ->
          new Success<>(
              new ValidOrder(
                  request.customerName(),
                  request.orderDateTime(),
                  validDetails,
                  calcTotalAmount(validDetails)));

      case Failure<List<ValidOrderDetail>> failure -> failure.cast();
    };
  }

  private static long calcTotalAmount(List<ValidOrderDetail> details) {
    return details.stream().mapToLong(d -> (long) d.quantity() * d.unitPrice()).sum();
  }

  // 明細一つぶんのバリデーションを行う
  Result<ValidOrderDetail> validateDetailProductName(OrderRequestDetail detail) {
    var productOpt = productRepository.findByName(detail.productName());

    if (productOpt.isEmpty()) {
      return new Failure<>(List.of(String.format("注文詳細[%d]の商品名が不正です", detail.index())));
    }
    return new Success<>(
        new ValidOrderDetail(
            detail.index(), productOpt.get(), detail.quantity(), detail.unitPrice()));
  }

  // 在庫をチェックする
  Result<ValidOrder> checkStock(ValidOrder order) {
    var details = Result.collect(order.details(), this::checkDetailStock);
    return switch (details) {
      case Success<List<ValidOrderDetail>> ignored -> new Success<>(order);
      case Failure<List<ValidOrderDetail>> failure -> failure.cast();
    };
  }

  // 明細一つぶんの在庫をチェックする
  Result<ValidOrderDetail> checkDetailStock(ValidOrderDetail d) {
    var stock = stockRepository.findByProductId(d.product().id());
    if (stock.isEmpty() || stock.get().quantity() < d.quantity()) {
      return new Failure<>(
          List.of(String.format("注文詳細[%d]の商品「%s」の在庫が不足しています", d.index(), d.product().name())));
    }
    return new Success<>(d);
  }

  // 注文を保存する
  Result<Order> saveOrder(ValidOrder order) {
    try {
      var saved = orderRepository.save(buildOrder(order));
      return new Success<>(saved);
    } catch (Exception e) {
      throw new RuntimeException("Failed to save order", e);
    }
  }

  // DBに保存するためのOrderオブジェクトを構築する
  private Order buildOrder(ValidOrder order) {
    var details =
        order.details().stream()
            .map(d -> new OrderDetail(null, d.product().name(), d.quantity(), d.unitPrice()))
            .toList();
    return new Order(
        null, order.customerName(), order.orderDateTime(), order.totalAmount(), details);
  }

  // メールを送信する
  Result<Order> sendEmail(Order order) {
    try {
      // ここではメール送信処理を省略
      return new Success<>(order);
    } catch (Exception e) {
      return new Failure<>(List.of("注文は完了しましたが、注文完了メールの送信に失敗しました。注文履歴画面にてご確認ください。"));
    }
  }
}
