// All imports
import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.ArrayList;

// 1. Product class (no public)
class Product {
    private final int productId;
    private String name, category;
    private int quantity;
    private double price;
    public static final int LOW_STOCK_THRESHOLD = 3;
    public Product(int id, String name, String cat, int qty, double price) {
        this.productId = id; this.name=name; this.category=cat; this.quantity=qty; this.price=price;
    }
    public int getId() { return productId; }
    public String getName() { return name; }
    public String getCategory() { return category; }
    public int getQuantity() { return quantity; }
    public double getPrice() { return price; }
    public void reduceStock(int qty) { quantity -= qty; }
    public boolean isLowStock() { return quantity <= LOW_STOCK_THRESHOLD; }
}

// 2. Inventory class (no public)
class Inventory {
    private final ArrayList<Product> products = new ArrayList<>();
    public void addProduct(Product p){ products.add(p); }
    public Product findProductById(int id){ for(Product p:products) if(p.getId()==id) return p; return null; }
    public List<Product> getAllProducts(){ return new ArrayList<>(products); }
}

// 3. AuditLogService
class AuditLogService {
    private final ArrayList<String> logs = new ArrayList<>();
    public void addLog(String msg){ logs.add(java.time.LocalDateTime.now()+" | "+msg); }
}

// 4. SalesService
class SalesService {
    private final Inventory inventory;
    private final AuditLogService audit;
    public SalesService(Inventory inv, AuditLogService audit){ this.inventory=inv; this.audit=audit; }
    public void sellProduct(int id,int qty){
        Product p = inventory.findProductById(id);
        if(p==null) throw new IllegalStateException("Product Not Found!");
        if(p.getQuantity()<qty) throw new IllegalStateException("Not enough stock.");
        p.reduceStock(qty);
        audit.addLog("Sold "+qty+" units of "+p.getName());
    }
}

// 5. MainGUI
class MainGUI extends JFrame {
    private final Inventory inventory = new Inventory();
    private final AuditLogService auditLog = new AuditLogService();
    private final SalesService salesService = new SalesService(inventory, auditLog);

    private final JTextField idField = new JTextField(10);
    private final JTextField nameField = new JTextField(10);
    private final JTextField categoryField = new JTextField(10);
    private final JTextField qtyField = new JTextField(10);
    private final JTextField priceField = new JTextField(10);
    private final JTextField sellQtyField = new JTextField(10);
    private final JLabel messageLabel = new JLabel(" ");
    private final JButton addBtn = new JButton("Add Product");
    private final JButton sellBtn = new JButton("Sell Product");
    private final JComboBox<String> productIdCombo = new JComboBox<>();
    private final JTextArea inventoryArea = new JTextArea(15,50);

    public MainGUI(){
        setTitle("Store Management System");
        setSize(800,600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets=new Insets(5,5,5,5); gbc.fill=GridBagConstraints.HORIZONTAL;

        gbc.gridx=0; gbc.gridy=0; panel.add(new JLabel("ID:"),gbc); gbc.gridx=1; panel.add(idField,gbc);
        gbc.gridx=0; gbc.gridy=1; panel.add(new JLabel("Name:"),gbc); gbc.gridx=1; panel.add(nameField,gbc);
        gbc.gridx=0; gbc.gridy=2; panel.add(new JLabel("Category:"),gbc); gbc.gridx=1; panel.add(categoryField,gbc);
        gbc.gridx=0; gbc.gridy=3; panel.add(new JLabel("Quantity:"),gbc); gbc.gridx=1; panel.add(qtyField,gbc);
        gbc.gridx=0; gbc.gridy=4; panel.add(new JLabel("Price:"),gbc); gbc.gridx=1; panel.add(priceField,gbc);
        gbc.gridx=0; gbc.gridy=5; panel.add(addBtn,gbc); gbc.gridx=1; panel.add(messageLabel,gbc);
        gbc.gridx=0; gbc.gridy=6; panel.add(new JLabel("Product ID:"),gbc); gbc.gridx=1; panel.add(productIdCombo,gbc);
        gbc.gridx=0; gbc.gridy=7; panel.add(new JLabel("Qty to Sell:"),gbc); gbc.gridx=1; panel.add(sellQtyField,gbc);
        gbc.gridx=0; gbc.gridy=8; panel.add(sellBtn,gbc);
        gbc.gridx=0; gbc.gridy=9; gbc.gridwidth=2;
        inventoryArea.setEditable(false); panel.add(new JScrollPane(inventoryArea),gbc);

        add(panel);

        addBtn.addActionListener(e->addProduct());
        sellBtn.addActionListener(e->sellProduct());

        setVisible(true);
    }

    private void addProduct(){
        try{
            int id=Integer.parseInt(idField.getText().trim());
            int qty=Integer.parseInt(qtyField.getText().trim());
            double price=Double.parseDouble(priceField.getText().trim());
            String name=nameField.getText().trim();
            String category=categoryField.getText().trim();

            Product p=new Product(id,name,category,qty,price);
            inventory.addProduct(p);
            auditLog.addLog("Added product: "+name);
            refreshInventoryTable();
            refreshProductCombo();
            idField.setText(""); nameField.setText(""); categoryField.setText(""); qtyField.setText(""); priceField.setText("");
        }catch(Exception ex){ messageLabel.setText("[ERROR] Invalid input"); }
    }

    private void sellProduct(){
        try{
            String selected=(String)productIdCombo.getSelectedItem();
            if(selected==null) return;
            int id=Integer.parseInt(selected);
            int qty=Integer.parseInt(sellQtyField.getText().trim());
            salesService.sellProduct(id,qty);
            refreshInventoryTable();
            sellQtyField.setText("");
        }catch(Exception ex){ messageLabel.setText("[ERROR] "+ex.getMessage()); }
    }

    private void refreshInventoryTable(){
        StringBuilder sb=new StringBuilder();
        for(Product p:inventory.getAllProducts())
            sb.append(p.getId()).append(" | ").append(p.getName()).append(" | ")
              .append(p.getCategory()).append(" | Qty: ").append(p.getQuantity()).append(" | Rs ")
              .append(p.getPrice()).append("\n");
        inventoryArea.setText(sb.toString());
    }

    private void refreshProductCombo(){
        productIdCombo.removeAllItems();
        for(Product p:inventory.getAllProducts())
            productIdCombo.addItem(""+p.getId());
    }
}

// 6. Main class
public class Main{
    public static void main(String[] args){
        SwingUtilities.invokeLater(()->new MainGUI());
    }
}
