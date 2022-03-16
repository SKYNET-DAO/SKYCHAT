package cn.wildfire.imshat.wallet.bean;

import java.io.Serializable;
import java.util.List;

public class TransactionBean implements Serializable {

    private int purpose;
    private long updateTime;
    private String amount;
    private  String free;
    private String free_kb;
    private String bytes;
    private List<String> ins;
    private List<String > outs;
    private String txid;

    public long getUpdateTime() {
        return updateTime;
    }

    public TransactionBean setUpdateTime(long updateTime) {
        this.updateTime = updateTime;
        return this;
    }

    public String getAmount() {
        return amount;
    }

    public TransactionBean setAmount(String amount) {
        this.amount = amount;
        return this;
    }

    public String getFree() {
        return free;
    }

    public TransactionBean setFree(String free) {
        this.free = free;
        return this;
    }

    public String getFree_kb() {
        return free_kb;
    }

    public TransactionBean setFree_kb(String free_kb) {
        this.free_kb = free_kb;
        return this;
    }

    public String getBytes() {
        return bytes;
    }

    public TransactionBean setBytes(String bytes) {
        this.bytes = bytes;
        return this;
    }

    public List<String> getIns() {
        return ins;
    }

    public TransactionBean setIns(List<String> ins) {
        this.ins = ins;
        return this;
    }

    public List<String> getOuts() {
        return outs;
    }

    public TransactionBean setOuts(List<String> outs) {
        this.outs = outs;
        return  this;
    }

    public String getTxid() {
        return txid;
    }

    public TransactionBean setTxid(String txid) {
        this.txid = txid;
        return this;
    }

    public int getPurpose() {
        return purpose;
    }

    public TransactionBean setPurpose(int purpose) {
        this.purpose = purpose;
        return this;
    }

}
