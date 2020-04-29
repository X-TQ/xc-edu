package com.xuecheng.manage_course.ribbon;


/**
 * @Author xtq
 * @Date 2020/3/5 18:50
 * @Description
 */

public class TestApp {
    public static void main(String[] args) {
        int[] numArr = {5,65,2,9,12,7,42,6};
        numArr = sort(numArr);
        for(int num : numArr){
            System.out.print(num+"  ");
        }
    }
    public static int[] sort(int[] arr){
        for(int i=1; i<arr.length; i++){
            for(int j=i; j>0; j--){
                if(arr[j]<arr[j-1]){
                    int temp = arr[j-1];
                    arr[j-1] = arr[j];
                    arr[j] = temp;
                }
            }
        }
        return arr;
    }
}
