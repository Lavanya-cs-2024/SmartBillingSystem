package com.smartbilling.model;

import java.sql.Timestamp;

public class Bill {
    private int id;
    private String billNumber;
    private Timestamp billDate;
    private double totalAmount;
    private String status;
    private Timestamp paymentDate;
    private String qrCode;
    private Timestamp qrExpiry;
    private Timestamp cancelledAt;
    private Timestamp expiredAt;
    
    public Bill() {}
    
    public Bill(String billNumber, double totalAmount, String status) {
        this.billNumber = billNumber;
        this.totalAmount = totalAmount;
        this.status = status;
    }
    
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public String getBillNumber() { return billNumber; }
    public void setBillNumber(String billNumber) { this.billNumber = billNumber; }
    
    public Timestamp getBillDate() { return billDate; }
    public void setBillDate(Timestamp billDate) { this.billDate = billDate; }
    
    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public Timestamp getPaymentDate() { return paymentDate; }
    public void setPaymentDate(Timestamp paymentDate) { this.paymentDate = paymentDate; }
    
    public String getQrCode() { return qrCode; }
    public void setQrCode(String qrCode) { this.qrCode = qrCode; }
    
    public Timestamp getQrExpiry() { return qrExpiry; }
    public void setQrExpiry(Timestamp qrExpiry) { this.qrExpiry = qrExpiry; }
    
    public Timestamp getCancelledAt() { return cancelledAt; }
    public void setCancelledAt(Timestamp cancelledAt) { this.cancelledAt = cancelledAt; }
    
    public Timestamp getExpiredAt() { return expiredAt; }
    public void setExpiredAt(Timestamp expiredAt) { this.expiredAt = expiredAt; }
}