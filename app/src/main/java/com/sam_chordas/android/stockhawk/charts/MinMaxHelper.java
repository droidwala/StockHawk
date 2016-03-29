package com.sam_chordas.android.stockhawk.charts;

public class MinMaxHelper {

    static float getMin_value(float[] inputArray){

        float minvalue = inputArray[0];

        for(int i=1;i<=inputArray.length - 1;i++){
            if(inputArray[i] !=0 && inputArray[i] < minvalue){
                minvalue = inputArray[i];
            }
        }
        return minvalue;
    }

    static float getMax_value(float[] inputArray){

        float maxvalue = inputArray[0];

        for (int i = 1; i <= inputArray.length - 1 ; i++) {
            if(inputArray[i] > maxvalue){
                maxvalue = inputArray[i];
            }
        }
        return maxvalue;
    }

    static int GCD(int a,int b){
        if(b==0) return a;
        return GCD(b,a%b);
    }

    static int getMinGraphValue(int min_value){
        int MIN = 0;
        if(min_value > 0 && min_value < 10){
            MIN = 0;
        }
        else if(min_value > 9 && min_value < 100){
            MIN = min_value - (min_value % 10);//2 digit
        }
        else if(min_value > 99 && min_value < 1000){
            MIN = min_value - (min_value % 100);//3 digit
        }
        else if(min_value > 999 && min_value < 10000){
            MIN = min_value - (min_value % 1000);//4 digit
        }
        else if(min_value > 9999 && min_value < 100000){
            MIN = min_value - (min_value % 10000);// 5 digit
        }
        else if(min_value==0){
            MIN = 0;
        }
        return MIN;
    }

    static int getMaxGraphValue(int max_value){
        int MAX = 0;
        if(max_value > 0 && max_value < 9){
            MAX = 10;
        }
        else if(max_value > 9 && max_value < 100){
            MAX = (max_value - (max_value % 10)) + 10;//2 digit
        }
        else if(max_value > 99 && max_value < 1000){
            MAX = (max_value - (max_value % 100)) + 100;//3 digit
        }
        else if(max_value > 999 && max_value < 10000){
            MAX = (max_value - (max_value % 1000)) + 1000;//4 digit
        }
        else if(max_value > 9999 && max_value < 100000){
            MAX = (max_value - (max_value % 10000)) + 10000;// 5 digit
        }
        else if(max_value == 0){
            MAX = 0;
        }
        return MAX;
    }
}
