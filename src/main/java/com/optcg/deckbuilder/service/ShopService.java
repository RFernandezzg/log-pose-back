package com.optcg.deckbuilder.service;

import com.optcg.deckbuilder.model.dto.shop.OrderRequest;
import com.optcg.deckbuilder.model.dto.shop.OrderResponse;
import com.optcg.deckbuilder.model.dto.shop.ShopItemDto;
import com.optcg.deckbuilder.model.entity.Order;
import com.optcg.deckbuilder.model.entity.OrderItem;
import com.optcg.deckbuilder.model.entity.ShopItem;
import com.optcg.deckbuilder.model.entity.User;
import com.optcg.deckbuilder.model.enums.OrderStatus;
import com.optcg.deckbuilder.exception.BadRequestException;
import com.optcg.deckbuilder.exception.NotFoundException;
import com.optcg.deckbuilder.repository.OrderRepository;
import com.optcg.deckbuilder.repository.ShopItemRepository;
import com.optcg.deckbuilder.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ShopService {

    private final ShopItemRepository shopItemRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

        public List<ShopItemDto> getAllItems() {
                return shopItemRepository.findAll().stream()
                                .map(this::mapToDto)
                                .collect(Collectors.toList());
    }

    public List<OrderResponse> getMyOrders(String username) {
        User user = userRepository.findByUsername(username)
                                .orElseThrow(() -> new NotFoundException("User not found"));

        return orderRepository.findByUserOrderByCreatedAtDesc(user).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public OrderResponse createOrder(String username, OrderRequest request) {
        User user = userRepository.findByUsername(username)
                                .orElseThrow(() -> new NotFoundException("User not found"));

        Order order = Order.builder()
                .user(user)
                .status(OrderStatus.PENDING)
                .total(BigDecimal.ZERO)
                .build();

        BigDecimal total = BigDecimal.ZERO;

        for (Map.Entry<Long, Integer> entry : request.getItems().entrySet()) {
                        if (entry.getValue() == null || entry.getValue() <= 0) {
                                throw new BadRequestException("Item quantity must be greater than zero");
                        }

            ShopItem item = shopItemRepository.findById(entry.getKey())
                                        .orElseThrow(() -> new NotFoundException("Item not found: " + entry.getKey()));

            if (item.getStock() < entry.getValue()) {
                                throw new BadRequestException("Not enough stock for item: " + item.getName());
            }

            // Reduce stock
            item.setStock(item.getStock() - entry.getValue());
            shopItemRepository.save(item);

            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .item(item)
                    .quantity(entry.getValue())
                    .unitPrice(item.getPrice())
                    .build();

            order.getItems().add(orderItem);

            BigDecimal itemTotal = item.getPrice().multiply(BigDecimal.valueOf(entry.getValue()));
            total = total.add(itemTotal);
        }

        order.setTotal(total);
        Order savedOrder = orderRepository.save(order);

        // Send email receipt async (using @Async in EmailService)
        emailService.sendOrderReceipt(user.getEmail(), user.getUsername(), savedOrder);

        return mapToResponse(savedOrder);
    }

    private OrderResponse mapToResponse(Order order) {
        Map<String, Integer> itemsMap = order.getItems().stream()
                .collect(Collectors.toMap(
                        item -> item.getItem().getName(),
                        OrderItem::getQuantity
                ));

        return OrderResponse.builder()
                .id(order.getId())
                .userId(order.getUser().getId())
                .total(order.getTotal())
                .status(order.getStatus())
                .createdAt(order.getCreatedAt())
                .items(itemsMap)
                .build();
    }

        private ShopItemDto mapToDto(ShopItem item) {
                return ShopItemDto.builder()
                                .id(item.getId())
                                .name(item.getName())
                                .description(item.getDescription())
                                .price(item.getPrice())
                                .imageUrl(item.getImageUrl())
                                .stock(item.getStock())
                                .category(item.getCategory())
                                .createdAt(item.getCreatedAt())
                                .build();
        }
}
