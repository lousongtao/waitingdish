package com.shuishou.waitingdish.bean;

public class WaitingIntentDetail {
    private int detailId;
    private String desk;
    private int amount;
    private String requirement;

    public WaitingIntentDetail(int detailId, String desk, int amount, String requirement) {
        this.detailId = detailId;
        this.desk = desk;
        this.amount = amount;
        this.requirement = requirement;
    }

    public int getDetailId() {
        return detailId;
    }

    public void setDetailId(int detailId) {
        this.detailId = detailId;
    }

    public String getDesk() {
        return desk;
    }

    public void setDesk(String desk) {
        this.desk = desk;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public String getRequirement() {
        return requirement;
    }

    public void setRequirement(String requirement) {
        this.requirement = requirement;
    }
}
