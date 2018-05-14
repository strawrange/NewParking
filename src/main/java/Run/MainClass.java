package Run;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
class Solution {
    public int divide(int dividend, int divisor) {
        if (dividend > Integer.MAX_VALUE || dividend < Integer.MIN_VALUE || divisor > Integer.MAX_VALUE || divisor < Integer.MIN_VALUE){
            return Integer.MAX_VALUE;
        }
        int i = 0;
        boolean positive = ( (dividend>= 0 && divisor >= 0) || (dividend<= 0 && divisor <= 0));
        if (dividend == Integer.MIN_VALUE){
            i = 1;
            if (divisor >= 0 && dividend < -divisor){
                dividend = dividend + divisor;
            }else if (divisor < 0 && divisor > dividend){
                dividend = dividend - divisor;
            }else{
                return 0;
            }
        }
        if (divisor == 1 || divisor == -1){
            return positive?dividend:-dividend;
        }
        dividend = Math.abs(dividend);
        divisor = Math.abs(divisor);
        for (; dividend >= divisor; i++){
            dividend = dividend - divisor;
        }
        return positive?i:-i;
    }
}

public class MainClass {
    public static void main(String[] args) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        String line;
        while ((line = in.readLine()) != null) {
            int dividend = Integer.parseInt(line);
            line = in.readLine();
            int divisor = Integer.parseInt(line);

            int ret = new Solution().divide(dividend, divisor);

            String out = String.valueOf(ret);

            System.out.print(out);
        }
    }
}