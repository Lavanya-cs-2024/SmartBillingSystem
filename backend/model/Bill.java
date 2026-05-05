/**
 * Bill.java
 * 
 * PURPOSE:
 * Model class representing a Bill from 'bills' database table.
 * A Bill is the HEADER/MAIN record for a transaction.
 * 
 * RELATIONSHIPS:
 * One Bill → Many BillItems (one-to-many)
 * One Bill → Many StockLogs (one-to-many)
 * 
 * BILL STATUS FLOW:
 * PENDING → PAID (successful payment)
 * PENDING → FAILED (user cancelled)
 * PENDING → EXPIRED (30 minutes timeout)
 */

package backend.model;

import java.sql.Timestamp;  // For date/time values

public class Bill {
    
    // ========== FIELDS (Match bills table columns) ==========
    
    private int id;                  // Unique bill ID (Primary Key)
    private String billNumber;       // Human-readable ID (e.g., "BILL-1234567890")
    private Timestamp billDate;      // When bill was created
    private double totalAmount;      // Final total after all items
    private String status;           // PENDING, PAID, FAILED, EXPIRED
    private Timestamp paymentDate;   // When payment completed (NULL if not paid)
    private String qrCode;           // QR code data for UPI payment
    private Timestamp qrExpiry;      // QR valid until (3 minutes from generation)
    private Timestamp cancelledAt;   // When user cancelled
    private Timestamp expiredAt;     // When bill auto-expired
    
    // ========== CONSTRUCTORS ==========
    
    /**
     * Default constructor - creates empty Bill object
     */
    public Bill() {}
    
    /**
     * Parameterized constructor for new bills
     * @param billNumber Unique bill identifier
     * @param totalAmount Total bill amount
     * @param status Initial status (usually "PENDING")
     */
    public Bill(String billNumber, double totalAmount, String status) {
        this.billNumber = billNumber;
        this.totalAmount = totalAmount;
        this.status = status;
    }
    
    // ========== GETTERS ==========
    
    public int getId() { 
        return id; 
    }
    
    public String getBillNumber() { 
        return billNumber; 
    }
    
    public Timestamp getBillDate() { 
        return billDate; 
    }
    
    public double getTotalAmount() { 
        return totalAmount; 
    }
    
    public String getStatus() { 
        return status; 
    }
    
    public Timestamp getPaymentDate() { 
        return paymentDate; 
    }
    
    public String getQrCode() { 
        return qrCode; 
    }
    
    public Timestamp getQrExpiry() { 
        return qrExpiry; 
    }
    
    public Timestamp getCancelledAt() { 
        return cancelledAt; 
    }
    
    public Timestamp getExpiredAt() { 
        return expiredAt; 
    }
    
    // ========== SETTERS ==========
    
    public void setId(int id) { 
        this.id = id; 
    }
    
    public void setBillNumber(String billNumber) { 
        this.billNumber = billNumber; 
    }
    
    public void setBillDate(Timestamp billDate) { 
        this.billDate = billDate; 
    }
    
    public void setTotalAmount(double totalAmount) { 
        this.totalAmount = totalAmount; 
    }
    
    public void setStatus(String status) { 
        this.status = status; 
    }
    
    public void setPaymentDate(Timestamp paymentDate) { 
        this.paymentDate = paymentDate; 
    }
    
    public void setQrCode(String qrCode) { 
        this.qrCode = qrCode; 
    }
    
    public void setQrExpiry(Timestamp qrExpiry) { 
        this.qrExpiry = qrExpiry; 
    }
    
    public void setCancelledAt(Timestamp cancelledAt) { 
        this.cancelledAt = cancelledAt; 
    }
    
    public void setExpiredAt(Timestamp expiredAt) { 
        this.expiredAt = expiredAt; 
    }
}