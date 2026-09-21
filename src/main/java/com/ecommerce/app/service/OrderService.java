package com.ecommerce.app.service;

import com.ecommerce.app.dto.OrderRequest;
import com.ecommerce.app.model.*;
import com.ecommerce.app.repository.CartItemRepository;
import com.ecommerce.app.repository.OrderRepository;
import com.ecommerce.app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;
    private final QikinkService qikinkService;

    // false karne par paid order admin ke confirm karne tak PENDING rahega.
    @Value("${app.auto-confirm-paid-orders:true}")
    private boolean autoConfirmPaidOrders;

    @Transactional
    public Order placeOrder(Long userId, OrderRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<CartItem> cartItems = cartItemRepository.findByUser(user);
        if (cartItems.isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }

        Order order = new Order();
        order.setUser(user);
        order.setShippingAddress(request.getShippingAddress());
        order.setShippingCity(request.getShippingCity());
        order.setShippingState(request.getShippingState());
        order.setShippingZip(request.getShippingZip());
        order.setShippingPhone(request.getShippingPhone());
        order.setStatus(Order.OrderStatus.PENDING);
        // Online orders stay unconfirmable until PaymentService marks them PAID.
        order.setPaymentMethod("ONLINE".equalsIgnoreCase(request.getPaymentMethod()) ? "ONLINE" : "COD");
        order.setPaymentStatus("PENDING");

        BigDecimal total = BigDecimal.ZERO;
        for (CartItem ci : cartItems) {
            OrderItem oi = new OrderItem();
            oi.setOrder(order);
            oi.setProduct(ci.getProduct());
            oi.setQuantity(ci.getQuantity());
            oi.setPriceAtPurchase(ci.getProduct().getPrice());
            order.getItems().add(oi);

            total = total.add(ci.getProduct().getPrice().multiply(BigDecimal.valueOf(ci.getQuantity())));
        }
        order.setTotalAmount(total);

        Order saved = orderRepository.save(order);
        cartItemRepository.deleteByUser(user);

        return saved;
    }

    public List<Order> getUserOrders(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return orderRepository.findByUserOrderByCreatedAtDesc(user);
    }

    public Order getOrderById(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAllByOrderByCreatedAtDesc();
    }

    @Transactional
    public Order updateOrderStatus(Long orderId, Order.OrderStatus status) {
        Order order = getOrderById(orderId);

        // Online payment wale order ko tabhi CONFIRM (aur Qikink ko push) karo jab paise aa chuke hon.
        if (status == Order.OrderStatus.CONFIRMED
                && "ONLINE".equals(order.getPaymentMethod())
                && !"PAID".equals(order.getPaymentStatus())) {
            throw new RuntimeException("Online payment abhi PAID nahi hai, order confirm nahi ho sakta");
        }

        order.setStatus(status);
        Order saved = orderRepository.save(order);
        // Push Qikink-fulfilled items once the admin confirms the order (not on PENDING,
        // so a still-uncertain order — e.g. awaiting payment — never gets sent to print).
        if (status == Order.OrderStatus.CONFIRMED) {
            qikinkService.pushOrderIfApplicable(saved);
            saved = orderRepository.save(saved); // persist qikinkPushed flag if it was set
        }
        return saved;
    }

    /**
     * Online payment PAID hote hi order ko CONFIRMED karta hai aur Qikink ko bhejta hai.
     * Sirf ONLINE + PAID + PENDING order par chalta hai, COD par kabhi nahi.
     * Dobara call hone par kuch nahi karta (status ab PENDING nahi hota).
     */
    @Transactional
    public void confirmAfterPayment(Long orderId) {
        if (!autoConfirmPaidOrders) {
            return;
        }
        Order order = getOrderById(orderId);
        if (!"ONLINE".equals(order.getPaymentMethod()) || !"PAID".equals(order.getPaymentStatus())) {
            return;
        }
        if (order.getStatus() != Order.OrderStatus.PENDING) {
            return;
        }

        order.setStatus(Order.OrderStatus.CONFIRMED);
        Order saved = orderRepository.save(order);
        qikinkService.pushOrderIfApplicable(saved);
        orderRepository.save(saved); // qikinkPushed flag save karo
        log.info("Order {} auto-confirmed after payment (qikinkPushed={})", orderId, saved.isQikinkPushed());
    }
}
