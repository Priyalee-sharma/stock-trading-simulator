
import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import javax.swing.*;

class User{
   // String name;
    double cashBalance;
    Map<String, Integer> portfolio;
    List<Transaction> transactionHistory;

    public User(double cashBalance){
        //this.name = name;
        this.cashBalance = cashBalance;
        portfolio = new HashMap<>();
        transactionHistory = new ArrayList<>();
    }

    public double getCashBalance() {
        return cashBalance;
    }

    public List<Transaction> getTransactionHistory() {
        return transactionHistory;
    }

    public Map<String, Integer> getPortfolio() {
        return portfolio;
    }


    public void savePortfolioToFile(String filename) {
    try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
        writer.println("CashBalance " + cashBalance);

        for (Transaction t : transactionHistory) {
            writer.println("Transaction " + t.type + " " + t.stockSymbol + " " + t.quantity + " " + t.price);
        }

        System.out.println("✅ Portfolio saved to file.");
    } catch (IOException e) {
        System.out.println("❌ Error saving portfolio: " + e.getMessage());
    }
}    

public void loadPortfolioFromFile(String filename) {
    File file = new File(filename);
    if (!file.exists()) {
        System.out.println("ℹ️ No saved portfolio found. Starting fresh.");
        return; // Exit the method early
    }

    try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
        String line;
        while ((line = reader.readLine()) != null) {
            String[] parts = line.split(" ");
            switch (parts[0]) {
                case "CashBalance":
                    cashBalance = Double.parseDouble(parts[1]);
                    break;
                case "Stock":
                    portfolio.put(parts[1], Integer.parseInt(parts[2]));
                    break;
                case "Transaction":
                    transactionHistory.add(new Transaction(parts[1], parts[2], Integer.parseInt(parts[3]), Double.parseDouble(parts[4])));
                    break;
            }
        }
    //     System.out.println("✅ Portfolio loaded from file.");
    } catch (IOException e) {
        System.out.println("❌ Error loading portfolio: " + e.getMessage());
    }
}


    public void buyStock(Stock stock, int quantity){
        double totalCost = quantity * stock.price;
        if(totalCost <= cashBalance){
            cashBalance -= totalCost;
            portfolio.put(stock.symbol, portfolio.getOrDefault(stock.symbol, 0) + quantity);
            transactionHistory.add(new Transaction("BUY",stock.symbol , quantity, stock.price));
            System.out.println("Bought "+quantity+" shares of "+stock.symbol);
        }else{
            System.out.println("Insufficient Funds");
        }
    }
    public void sellStock(Stock stock, int quantity){
        int owned = portfolio.getOrDefault(stock.symbol, 0);
        if(quantity <= owned){
            double totalRevenue = quantity * stock.price;
            cashBalance += totalRevenue;
            portfolio.put(stock.symbol, owned - quantity);
            transactionHistory.add(new Transaction("SELL",stock.symbol,quantity,stock.price));
            System.out.println( "SOLD "+quantity+" shares of "+stock.symbol);
        }else{
            System.out.println("You don't own that many shares.");
        }
    }


    public void displayPortfolio(Map<String, Stock> market){
        System.out.println("\nPortfolio");
        double totalValue = cashBalance;
        for(String symbol : portfolio.keySet()){
            int quantity = portfolio.get(symbol);
            double stockPrice = market.get(symbol).price;
            totalValue += quantity * stockPrice;
            System.out.println("stockprice is: "+stockPrice+" quantity is: "+quantity);
            System.out.println(symbol+" : "+quantity+" shares @ $ "+String.format("%.2f", stockPrice));
        }
        System.out.println("Cash balance : $"+String.format("%.2f", cashBalance));
        System.out.println("Total portfolio value: $"+ String.format("%.2f", totalValue));
    }
    
    public void displayTransactionHistory(){
        System.out.println("Transaction History\n");
        for(Transaction t : transactionHistory){
            System.out.println(t);      
        }
    }
}

 class Transaction{
    String type;
    String stockSymbol;
    int quantity;
    double price;

    public Transaction(String type, String stockSymbol, int quantity, double price){
        this.type = type;
        this.stockSymbol = stockSymbol;
        this.quantity = quantity;
        this.price = price;
    }

    @Override

    public String toString(){
        return type+" "+quantity+" "+stockSymbol+" at $ "+String.format("%.2f", price);
    }
}
class Stock{
    String symbol;
    double price;
    public Stock(String symbol, double price){
        this.symbol = symbol;
        this.price = price;
    }
    public void updatePrice(){
        double change = (Math.random() - 0.5) * 4;
        price = Math.max(1.0, price + change);
    }
}
public class PortfolioManager{
    public static Map<String, Stock> market = new HashMap<>();
    public static Scanner scanner = new Scanner(System.in);

   

    public static void main(String[] args){
       
        User user = new User(40000.0);

        market.put("AAPL", new Stock("AAPL", 150.0));
        market.put("TSLA", new Stock("TSLA", 100.0));
        market.put("GOOG", new Stock("GOOG", 180.0));

        user.loadPortfolioFromFile("portfolio.txt");

        new StockTradingUI(user, market); // 🔹 Launch GUI
       
    }
    

}




 class StockTradingUI extends JFrame {
    // Reference to your User and market
    private User user;
    private Map<String, Stock> market;

    public StockTradingUI(User user, Map<String, Stock> market) {
        setVisible(true);
        this.user = user;
        this.market = market;

        setupUI();
    }

    private void setupUI() {
        setTitle("📈 Stock Trading Simulator");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Create components
        JTextArea outputArea = new JTextArea();
        outputArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(outputArea);

        JPanel buttonPanel = new JPanel();
        JButton buyButton = new JButton("Buy Stock");
        JButton sellButton = new JButton("Sell Stock");
        JButton viewPortfolio = new JButton("View Portfolio");
        JButton exitButton = new JButton("Exit");

        // Add buttons to panel
        buttonPanel.add(buyButton);
        buttonPanel.add(sellButton);
        buttonPanel.add(viewPortfolio);
        buttonPanel.add(exitButton);

        // Add to frame
        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        // Event handlers
        buyButton.addActionListener(e -> {
            String symbol = JOptionPane.showInputDialog("Enter stock symbol:");
            String qtyStr = JOptionPane.showInputDialog("Enter quantity:");
            try {
                int qty = Integer.parseInt(qtyStr);
                Stock stock = market.get(symbol);
                if (stock != null) {
                    user.buyStock(stock, qty);
                    outputArea.append("Bought " + qty + " of " + symbol + "\n");
                } else {
                    outputArea.append("Stock not found.\n");
                }
            } catch (Exception ex) {
                outputArea.append("Invalid input.\n");
            }
        });

        sellButton.addActionListener(e -> {
            String symbol = JOptionPane.showInputDialog("Enter stock symbol:");
            String qtyStr = JOptionPane.showInputDialog("Enter quantity:");
            try {
                int qty = Integer.parseInt(qtyStr);
                Stock stock = market.get(symbol);
                if (stock != null) {
                    user.sellStock(stock, qty);
                    outputArea.append("Sold " + qty + " of " + symbol + "\n");
                } else {
                    outputArea.append("Stock not found.\n");
                }
            } catch (Exception ex) {
                outputArea.append("Invalid input.\n");
            }
        });

        viewPortfolio.addActionListener(e -> {
            outputArea.append("Portfolio:\n");
            outputArea.append("Cash: $" + user.getCashBalance() + "\n");
           
            for (Map.Entry<String, Integer> entry : user.getPortfolio().entrySet()) {
                outputArea.append(entry.getKey() + " -> " + entry.getValue() + "\n");
            }
        });

        exitButton.addActionListener(e -> {
            user.savePortfolioToFile("portfolio.txt");
            System.exit(0);
        });

        setVisible(true);
    }
}
