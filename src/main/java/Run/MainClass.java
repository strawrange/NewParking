package Run;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


class Solution {
    public String decodeString(String s) {
        int stack = 0;
        String result = new String();
        int start = 0;
        int num = 0;
        for (int i = 0; i < s.length(); i++){
            while (s.charAt(i) >= 'a' && s.charAt(i) <= 'z'){
                result = result + s.charAt(i);
                i++;
                if (i >= s.length()){
                    return result;
                }
            }
            while (s.charAt(i) >= '0' && s.charAt(i) <= '9'){
                char c = s.charAt(i);
                num = num * 10 + Character.getNumericValue(c);
                i++;
            }
            for (;i < s.length();i++){
                if (s.charAt(i) == '['){
                    if (start == 0){
                        start = i + 1;
                    }
                    stack++;
                }
                if (s.charAt(i) == ']'){
                    stack--;
                    if (stack == 0){
                        String newString =  decodeString(s.substring(start, i));
                        start = 0;
                        while(--num >= 0){
                            result = result + newString;
                        }
                        break;
                    }
                }
            }
        }
        return result;
    }
}

public class MainClass {
    public static String stringToString(String input) {
        if (input == null) {
            return "null";
        }
        return input;
    }

    public static void main(String[] args) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        String line;
        while ((line = in.readLine()) != null) {
            String s = stringToString(line);

            String ret = new Solution().decodeString(s);

            String out = (ret);

            System.out.print(out);
        }
    }
}