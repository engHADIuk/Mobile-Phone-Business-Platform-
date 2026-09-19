package org.gwanhee.groupproject;

public class PaymentCard {
    private String paymentMethodId;
    private String cardHolderName;
    private String cardNumber;
    private String expiryDate;
    private String cvc;

    public PaymentCard(String paymentMethodId, String cardHolderName, String cardNumber, String expiryDate, String cvc) {
        this.paymentMethodId = paymentMethodId;
        this.cardHolderName = cardHolderName;
        this.cardNumber = cardNumber;
        this.expiryDate = expiryDate;
        this.cvc = cvc;
    }

    public String getPaymentMethodId() {
        return paymentMethodId;
    }

    public String getCardHolderName() {
        return cardHolderName;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public String getExpiryDate() {
        return expiryDate;
    }

    public String getCvc() {
        return cvc;
    }

    @Override
    public String toString() {
        return cardHolderName + " - **** **** **** " + cardNumber.substring(cardNumber.length() - 4)
                + "    Exp: " + expiryDate;
    }
}