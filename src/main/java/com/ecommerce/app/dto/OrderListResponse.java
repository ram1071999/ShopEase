package com.ecommerce.app.dto;

import com.ecommerce.app.model.Order;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;

/**
 * Qikink-style wrapped order list: { success, data[], pagination }.
 * Generic so both OrderController (maps to OrderResponse, hides supplier data)
 * and AdminController (maps to raw Order) can reuse the same shape.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderListResponse<T> {

    private boolean success;
    private List<T> data;
    private Pagination pagination;

    public static <T> OrderListResponse<T> from(Page<Order> page, Function<Order, T> mapper) {
        List<T> data = page.getContent().stream().map(mapper).toList();
        Pagination pagination = new Pagination(
                page.getNumber() + 1,   // 1-indexed jaise Qikink me hai
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.hasNext()
        );
        return new OrderListResponse<>(true, data, pagination);
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Pagination {
        private int page;
        private int perPage;
        private long totalResults;
        private int totalPages;
        private boolean hasNext;
    }
}
