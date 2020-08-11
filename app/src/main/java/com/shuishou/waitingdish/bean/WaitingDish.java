package com.shuishou.waitingdish.bean;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class WaitingDish {
    private String dishName;
    private List<WaitingIntentDetail> indentDetails;


    public String getDishName() {
        return dishName;
    }

    public void setDishName(String dishName) {
        this.dishName = dishName;
    }

    public List<WaitingIntentDetail> getIndentDetails() {
        return indentDetails;
    }

    public void setIndentDetails(List<WaitingIntentDetail> indentDetails) {
        this.indentDetails = indentDetails;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WaitingDish that = (WaitingDish) o;
        return Objects.equals(dishName, that.dishName);
    }

    @Override
    public int hashCode() {

        return Objects.hash(dishName);
    }

    @Override
    public String toString() {
        return "WaitingDish{" +
                "dishName=" + dishName +
                '}';
    }
}
