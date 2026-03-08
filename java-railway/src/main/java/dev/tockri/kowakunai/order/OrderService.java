package dev.tockri.kowakunai.order;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import dev.tockri.kowakunai.order.db.Order;
import dev.tockri.kowakunai.order.db.OrderDetail;
import dev.tockri.kowakunai.order.db.OrderRepository;
import dev.tockri.kowakunai.order.db.Product;
import dev.tockri.kowakunai.order.db.ProductRepository;
import dev.tockri.kowakunai.order.db.StockRepository;
import dev.tockri.kowakunai.order.dto.OrderFailureResponse;
import dev.tockri.kowakunai.order.dto.OrderRequest;
import dev.tockri.kowakunai.order.dto.OrderRequestDetail;
import dev.tockri.kowakunai.order.dto.OrderResponse;
import dev.tockri.kowakunai.order.dto.OrderSuccessResponse;
import dev.tockri.kowakunai.util.Failure;
import dev.tockri.kowakunai.util.Result;
import dev.tockri.kowakunai.util.Success;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final Clock clock;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final StockRepository stockRepository;

    record ValidOrder(String customerName, LocalDateTime orderDateTime,
            List<ValidOrderDetail> details, long totalAmount) {
    }

    record ValidOrderDetail(int index, Product product, int quantity, int unitPrice) {
    }

    public OrderResponse placeOrder(@Validated OrderRequest request) {
        var result = validate(request)
                .then(this::checkStock)
                .then(this::saveOrder)
                .then(this::sendEmail);

        return switch (result) {
            case Success<Order>(var order) -> new OrderSuccessResponse(order.id(),
                    order.customerName(), order.orderTime(), order.totalAmount());
            case Failure<Order>(var errors) -> new OrderFailureResponse(errors);
        };
    }

    // 注文全体のバリデーションを行う
    Result<ValidOrder> validate(OrderRequest request) {
        if (request.orderDateTime().isAfter(LocalDateTime.now(clock))) {
            return new Failure<>(List.of("注文日時が不正です"));
        }

        var details = Result.collect(request.details(), this::validateDetail);

        return switch (details) {
            case Success<List<ValidOrderDetail>>(var validDetails) -> new Success<>(
                    new ValidOrder(request.customerName(), request.orderDateTime(), validDetails,
                            calcTotalAmount(validDetails)));

            case Failure<List<ValidOrderDetail>> failure -> failure.cast();
        };
    }

    private static long calcTotalAmount(List<ValidOrderDetail> details) {
        return details.stream().mapToLong(d -> d.quantity() * d.unitPrice()).sum();
    }

    // 明細一つぶんのバリデーションを行う
    Result<ValidOrderDetail> validateDetail(OrderRequestDetail detail) {
        var productOpt = productRepository.findByName(detail.productName());

        if (productOpt.isEmpty()) {
            return new Failure<>(List.of(String.format("注文詳細[%d]の商品名が不正です", detail.index())));
        }
        return new Success<>(new ValidOrderDetail(detail.index(), productOpt.get(),
                detail.quantity(), detail.unitPrice()));
    }

    // 在庫をチェックする
    Result<ValidOrder> checkStock(ValidOrder order) {
        var details = Result.collect(order.details(), this::checkDetailStock);
        return switch (details) {
            case Success<List<ValidOrderDetail>> success -> new Success<>(order);
            case Failure<List<ValidOrderDetail>> failure -> failure.cast();
        };
    }

    // 明細一つぶんの在庫をチェックする
    private Result<ValidOrderDetail> checkDetailStock(ValidOrderDetail d) {
        var stock = stockRepository.findByProductId(d.product().id());
        if (stock.isEmpty() || stock.get().quantity() < d.quantity()) {
            return new Failure<>(List.of(
                    String.format("注文詳細[%d]の商品「%s」の在庫が不足しています", d.index(), d.product().name())));
        }
        return new Success<>(d);
    }

    // 注文を保存する
    Result<Order> saveOrder(ValidOrder order) {
        try {
            var saved = orderRepository.save(buildOrder(order));
            return new Success<>(saved);
        } catch (Exception e) {
            return new Failure<>(List.of("注文の保存に失敗しました"));
        }
    }

    // DBに保存するためのOrderオブジェクトを構築する
    Order buildOrder(ValidOrder order) {
        var details = order.details().stream()
                .map(d -> new OrderDetail(null, d.product().name(), d.quantity(), d.unitPrice()))
                .toList();
        return new Order(null, order.customerName(), order.orderDateTime(), order.totalAmount(),
                details);
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
