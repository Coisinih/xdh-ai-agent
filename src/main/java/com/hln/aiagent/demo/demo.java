package com.hln.aiagent.demo;

import java.util.ArrayList;
import java.util.List;

public class demo {
    public static void main(String[] args) {
        int[][] arr = {
                {1, 2, 3, 4},
                {12, 13, 14, 5},
                {11, 16, 15, 6},
                {10, 9, 8, 7}
        };
        List<Integer> objects = new ArrayList<>();
        Object[] array = objects.toArray();
        spiralArray(arr);
    }

    public static int[] spiralArray(int[][] array) {
        int x = 0,y = 0;
        int  rows = array.length, col = array[0].length;
        int total = rows * col;
        int count = 0;
        int[] res = new int[total];
        while(true) {
            for(int i = y; i < col - y - 1; i++) {
                res[count++] = array[x][i];
            }
            if(count == total) return res;

            for(int i = x; i < rows - x - 1; i++) {
                res[count++] = array[i][col - y - 1];
            }
            if(count == total) return res;

            for(int i = col - y - 1; i > y; i--) {
                res[count++] = array[rows - x - 1][i];
            }
            if(count == total) return res;

            for(int i = rows - x - 1; i > x; i--) {
                res[count++] = array[i][y];
            }
            if(count == total) return res;
            x++;
            y++;
        }
    }

}
