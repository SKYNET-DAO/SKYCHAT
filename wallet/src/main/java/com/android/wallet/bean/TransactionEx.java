package com.android.wallet.bean;

import java.io.Serializable;

public class TransactionEx implements Serializable {


    /**
     * result : {"walletversion":60000,"balance":4608959.08554606,"unconfirmed_balance":0,"immature_balance":31968,"txcount":7090,"keypoololdest":1559927284,"keypoolsize":101,"paytxfee":0}
     * id : 27719
     */

    private ResultBean result;
    private int id;

    public ResultBean getResult() {
        return result;
    }

    public void setResult(ResultBean result) {
        this.result = result;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public static class ResultBean {
        /**
         * walletversion : 60000
         * balance : 4608959.08554606
         * unconfirmed_balance : 0
         * immature_balance : 31968
         * txcount : 7090
         * keypoololdest : 1559927284
         * keypoolsize : 101
         * paytxfee : 0
         */

        private int walletversion;
        private double balance;
        private int unconfirmed_balance;
        private int immature_balance;
        private int txcount;
        private int keypoololdest;
        private int keypoolsize;
        private int paytxfee;

        public int getWalletversion() {
            return walletversion;
        }

        public void setWalletversion(int walletversion) {
            this.walletversion = walletversion;
        }

        public double getBalance() {
            return balance;
        }

        public void setBalance(double balance) {
            this.balance = balance;
        }

        public int getUnconfirmed_balance() {
            return unconfirmed_balance;
        }

        public void setUnconfirmed_balance(int unconfirmed_balance) {
            this.unconfirmed_balance = unconfirmed_balance;
        }

        public int getImmature_balance() {
            return immature_balance;
        }

        public void setImmature_balance(int immature_balance) {
            this.immature_balance = immature_balance;
        }

        public int getTxcount() {
            return txcount;
        }

        public void setTxcount(int txcount) {
            this.txcount = txcount;
        }

        public int getKeypoololdest() {
            return keypoololdest;
        }

        public void setKeypoololdest(int keypoololdest) {
            this.keypoololdest = keypoololdest;
        }

        public int getKeypoolsize() {
            return keypoolsize;
        }

        public void setKeypoolsize(int keypoolsize) {
            this.keypoolsize = keypoolsize;
        }

        public int getPaytxfee() {
            return paytxfee;
        }

        public void setPaytxfee(int paytxfee) {
            this.paytxfee = paytxfee;
        }
    }
}
