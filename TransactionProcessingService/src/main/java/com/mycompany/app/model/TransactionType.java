package com.mycompany.app.model;

public enum TransactionType {
    TRANSFER,
    BILL_PAYMENT,
    DIRECT_DEPOSIT,
    RECURRING;

    public boolean isCredit(){
        switch(this){
            case DIRECT_DEPOSIT:
                return true;
            case BILL_PAYMENT:
                return false;
            case TRANSFER:
            case RECURRING:
            default:
                //For the Transaction/Recurring I will remember to inspect from/to account
                return false;
        }
    }
    //This is a "debit" azin outgoing transaction...
    public boolean isDebit(){
        return !isCredit();
    }
}
