import java.util.List;

public class OrderListResult {
    private final List<Order> orders;
    private final int currentPage;
    private final int totalPages;

    public OrderListResult(List<Order> orders, int currentPage, int totalPages) {
        this.orders = orders;
        this.currentPage = currentPage;
        this.totalPages = totalPages;
    }

    public List<Order> getOrders() {
        return orders;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public int getTotalPages() {
        return totalPages;
    }
}
