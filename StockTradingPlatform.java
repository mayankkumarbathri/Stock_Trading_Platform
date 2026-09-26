import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;

/**
 * CodeAlpha Java Programming Internship - Task 2
 * Stock Trading Platform (console simulation)
 *
 * Simulates a basic market, lets a user buy/sell stocks, tracks a
 * portfolio's cash and holdings over time, and persists the portfolio
 * to a text file so it survives between runs.
 */
public class StockTradingPlatform {

    // ---------- Stock model ----------
    static class Stock {
        final String symbol;
        final String name;
        double price;

        Stock(String symbol, String name, double price) {
            this.symbol = symbol;
            this.name = name;
            this.price = price;
        }
    }

    // ---------- Transaction record ----------
    static class Transaction {
        final int day;
        final String type;   // "BUY" or "SELL"
        final String symbol;
        final int quantity;
        final double price;

        Transaction(int day, String type, String symbol, int quantity, double price) {
            this.day = day;
            this.type = type;
            this.symbol = symbol;
            this.quantity = quantity;
            this.price = price;
        }

        @Override
        public String toString() {
            return String.format("Day %-3d %-4s %-6d %-6s @ $%.2f", day, type, quantity, symbol, price);
        }
    }

    // ---------- Market: holds all tradable stocks ----------
    static class Market {
        final Map<String, Stock> stocks = new LinkedHashMap<>();
        final Random random = new Random();

        Market() {
            stocks.put("ALPH", new Stock("ALPH", "AlphaSoft Inc.", 150.00));
            stocks.put("BETA", new Stock("BETA", "BetaWorks Ltd.", 85.50));
            stocks.put("GAMA", new Stock("GAMA", "Gamma Energy Co.", 42.75));
            stocks.put("DELT", new Stock("DELT", "Delta Foods Corp.", 12.30));
            stocks.put("EPSI", new Stock("EPSI", "Epsilon Robotics", 310.20));
        }

        Stock get(String symbol) {
            return stocks.get(symbol.toUpperCase());
        }

        void advanceDay() {
            // Simulate a new trading day: each stock moves by up to +/-5%
            for (Stock s : stocks.values()) {
                double changePercent = (random.nextDouble() * 10.0) - 5.0; // -5% to +5%
                s.price = Math.max(0.50, s.price * (1 + changePercent / 100.0));
            }
        }

        void display() {
            System.out.println("\n----------- Market Data -----------");
            System.out.printf("%-8s %-20s %-10s%n", "Symbol", "Company", "Price");
            for (Stock s : stocks.values()) {
                System.out.printf("%-8s %-20s $%-9.2f%n", s.symbol, s.name, s.price);
            }
            System.out.println("------------------------------------\n");
        }
    }

    // ---------- Portfolio: a user's holdings, cash, and history ----------
    static class Portfolio {
        double cash;
        final Map<String, Integer> holdings = new LinkedHashMap<>(); // symbol -> quantity
        final List<Transaction> history = new ArrayList<>();

        Portfolio(double startingCash) {
            this.cash = startingCash;
        }

        void buy(Stock stock, int quantity, int day) {
            double cost = stock.price * quantity;
            cash -= cost;
            holdings.merge(stock.symbol, quantity, Integer::sum);
            history.add(new Transaction(day, "BUY", stock.symbol, quantity, stock.price));
        }

        void sell(Stock stock, int quantity, int day) {
            double proceeds = stock.price * quantity;
            cash += proceeds;
            int remaining = holdings.get(stock.symbol) - quantity;
            if (remaining <= 0) {
                holdings.remove(stock.symbol);
            } else {
                holdings.put(stock.symbol, remaining);
            }
            history.add(new Transaction(day, "SELL", stock.symbol, quantity, stock.price));
        }

        double holdingsValue(Market market) {
            double total = 0.0;
            for (Map.Entry<String, Integer> e : holdings.entrySet()) {
                Stock s = market.get(e.getKey());
                if (s != null) total += s.price * e.getValue();
            }
            return total;
        }

        double totalValue(Market market) {
            return cash + holdingsValue(market);
        }
    }

    // ---------- Application state ----------
    private static final Market market = new Market();
    private static final Portfolio portfolio = new Portfolio(10000.00); // starting cash
    private static final Scanner scanner = new Scanner(System.in);
    private static int currentDay = 1;
    private static final double startingTotalValue = portfolio.totalValue(market);
    private static final String SAVE_FILE = "portfolio.txt";

    public static void main(String[] args) {
        System.out.println("=========================================");
        System.out.println("   CodeAlpha - Stock Trading Platform");
        System.out.println("=========================================");
        System.out.println("Starting cash: $" + String.format("%.2f", portfolio.cash));

        boolean running = true;
        while (running) {
            printMenu();
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1" -> market.display();
                case "2" -> buyStock();
                case "3" -> sellStock();
                case "4" -> viewPortfolio();
                case "5" -> viewHistory();
                case "6" -> advanceDay();
                case "7" -> savePortfolio();
                case "8" -> loadPortfolio();
                case "9" -> {
                    running = false;
                    System.out.println("Goodbye!");
                }
                default -> System.out.println("Invalid choice. Please try again.\n");
            }
        }
        scanner.close();
    }

    private static void printMenu() {
        System.out.println("\nMain Menu (Day " + currentDay + "):");
        System.out.println("1. View Market Data");
        System.out.println("2. Buy Stock");
        System.out.println("3. Sell Stock");
        System.out.println("4. View Portfolio");
        System.out.println("5. View Transaction History");
        System.out.println("6. Advance to Next Day (simulate market movement)");
        System.out.println("7. Save Portfolio to File");
        System.out.println("8. Load Portfolio from File");
        System.out.println("9. Exit");
        System.out.print("Enter your choice: ");
    }

    private static void buyStock() {
        market.display();
        System.out.print("Enter stock symbol to buy: ");
        String symbol = scanner.nextLine().trim().toUpperCase();
        Stock stock = market.get(symbol);
        if (stock == null) {
            System.out.println("Unknown symbol.\n");
            return;
        }
        System.out.print("Enter quantity: ");
        int qty = readPositiveInt();
        if (qty <= 0) return;

        double cost = stock.price * qty;
        if (cost > portfolio.cash) {
            System.out.printf("Insufficient cash. Cost $%.2f exceeds balance $%.2f.%n%n", cost, portfolio.cash);
            return;
        }
        portfolio.buy(stock, qty, currentDay);
        System.out.printf("Bought %d shares of %s for $%.2f.%n%n", qty, symbol, cost);
    }

    private static void sellStock() {
        System.out.print("Enter stock symbol to sell: ");
        String symbol = scanner.nextLine().trim().toUpperCase();
        Stock stock = market.get(symbol);
        if (stock == null) {
            System.out.println("Unknown symbol.\n");
            return;
        }
        Integer owned = portfolio.holdings.get(symbol);
        if (owned == null || owned == 0) {
            System.out.println("You don't own any shares of " + symbol + ".\n");
            return;
        }
        System.out.println("You own " + owned + " shares.");
        System.out.print("Enter quantity to sell: ");
        int qty = readPositiveInt();
        if (qty <= 0) return;
        if (qty > owned) {
            System.out.println("You can't sell more shares than you own.\n");
            return;
        }
        portfolio.sell(stock, qty, currentDay);
        System.out.printf("Sold %d shares of %s for $%.2f.%n%n", qty, symbol, stock.price * qty);
    }

    private static void viewPortfolio() {
        System.out.println("\n----------- Portfolio (Day " + currentDay + ") -----------");
        System.out.printf("Cash balance:     $%.2f%n", portfolio.cash);
        if (portfolio.holdings.isEmpty()) {
            System.out.println("Holdings:         none");
        } else {
            System.out.println("Holdings:");
            System.out.printf("  %-8s %-10s %-12s %-12s%n", "Symbol", "Quantity", "Price", "Value");
            for (Map.Entry<String, Integer> e : portfolio.holdings.entrySet()) {
                Stock s = market.get(e.getKey());
                double value = s.price * e.getValue();
                System.out.printf("  %-8s %-10d $%-11.2f $%-11.2f%n", e.getKey(), e.getValue(), s.price, value);
            }
        }
        double totalValue = portfolio.totalValue(market);
        double gainLoss = totalValue - startingTotalValue;
        System.out.printf("Holdings value:   $%.2f%n", portfolio.holdingsValue(market));
        System.out.printf("Total value:      $%.2f%n", totalValue);
        System.out.printf("Gain/Loss so far: %s$%.2f%n", gainLoss >= 0 ? "+" : "-", Math.abs(gainLoss));
        System.out.println("--------------------------------------------\n");
    }

    private static void viewHistory() {
        if (portfolio.history.isEmpty()) {
            System.out.println("No transactions yet.\n");
            return;
        }
        System.out.println("\n----------- Transaction History -----------");
        for (Transaction t : portfolio.history) {
            System.out.println(t);
        }
        System.out.println("---------------------------------------------\n");
    }

    private static void advanceDay() {
        market.advanceDay();
        currentDay++;
        System.out.println("A new trading day has begun. Market prices have moved.\n");
    }

    private static void savePortfolio() {
        try (FileWriter writer = new FileWriter(SAVE_FILE)) {
            writer.write("day=" + currentDay + "\n");
            writer.write("cash=" + portfolio.cash + "\n");
            for (Map.Entry<String, Integer> e : portfolio.holdings.entrySet()) {
                writer.write("holding=" + e.getKey() + "," + e.getValue() + "\n");
            }
            System.out.println("Portfolio saved to " + SAVE_FILE + ".\n");
        } catch (IOException e) {
            System.out.println("Failed to save portfolio: " + e.getMessage() + "\n");
        }
    }

    private static void loadPortfolio() {
        try (BufferedReader reader = new BufferedReader(new FileReader(SAVE_FILE))) {
            portfolio.holdings.clear();
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                if (line.startsWith("day=")) {
                    currentDay = Integer.parseInt(line.substring(4));
                } else if (line.startsWith("cash=")) {
                    portfolio.cash = Double.parseDouble(line.substring(5));
                } else if (line.startsWith("holding=")) {
                    String[] parts = line.substring(8).split(",");
                    portfolio.holdings.put(parts[0], Integer.parseInt(parts[1]));
                }
            }
            System.out.println("Portfolio loaded from " + SAVE_FILE + ".\n");
        } catch (IOException e) {
            System.out.println("No saved portfolio found (" + e.getMessage() + ").\n");
        }
    }

    private static int readPositiveInt() {
        String input = scanner.nextLine().trim();
        try {
            int value = Integer.parseInt(input);
            if (value <= 0) {
                System.out.println("Quantity must be greater than zero.\n");
                return 0;
            }
            return value;
        } catch (NumberFormatException e) {
            System.out.println("Invalid number.\n");
            return 0;
        }
    }
}
