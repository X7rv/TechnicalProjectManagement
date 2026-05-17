package model;

import util.Money;

/**
 * Customer.java
 * نموذج العميل - يتولد من الطلبات.
 */
public class Customer {

    private final String name;
    private final String email;
    private final double totalSpent;
    private final int    orderCount;
    private final String rank;

    public Customer(String name, String email, double totalSpent, int orderCount, String rank) {
        this.name       = name;
        this.email      = email;
        this.totalSpent = totalSpent;
        this.orderCount = orderCount;
        this.rank       = rank;
    }

    public String getName()       { return name; }
    public String getEmail()      { return email == null || email.isEmpty() ? "—" : email; }
    public double getTotalSpent() { return totalSpent; }
    public int    getOrderCount() { return orderCount; }
    public String getRank()       { return rank; }

    public String getTotalFormatted() {
        return Money.format(totalSpent);
    }
}
