package com.guoxiaoxing.phoenix.picker.ui.camera;

import com.guoxiaoxing.phoenix.R;

public enum CarHint {
    ONE(0, R.drawable.phoenix_car_hint_one, "1.左前方"),
    TWO(1, R.drawable.phoenix_car_hint_two, "2.左侧"),
    THREE(2, R.drawable.phoenix_car_hint_three, "3.前排座椅"),
    FOUR(3, R.drawable.phoenix_car_hint_four, "4.仪表盘"),
    FIVE(4, R.drawable.phoenix_car_hint_five, "5.后排座椅"),
    SIX(5, R.drawable.phoenix_car_hint_six, "6.中控板"),
    SENVEN(6, R.drawable.phoenix_car_hint_senven, "7.车尾"),
    EIGHT(7, R.drawable.phoenix_car_hint_eight, "8.后备箱底板"),
    NINE(8, R.drawable.phoenix_car_hint_nine, "9.右后方"),
    TEN(9, R.drawable.phoenix_car_hint_ten, "10.发动机舱"),
    ELEVEN(10, R.drawable.phoenix_car_hint_eleven, "11.出厂铭牌"),
    TWELVE(11, R.drawable.phoenix_car_hint_twelve, "12.行驶证");
    private int index;
    private int res;
    private String text;

     CarHint(int index, int res, String text) {
        this.index = index;
        this.res = res;
        this.text = text;
    }

    public int getIndex() {
        return index;
    }

    public int getRes() {
        return res;
    }

    public String getText() {
        return text;
    }
}
