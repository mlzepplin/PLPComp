package cop5556sp18;

/**
 * Created by rishabh on 15/04/18.
 */
public class Wrapper {

    public static String className = "cop5556sp18/Wrapper";

    public static String polaraSignature = "(II)F";
    public static String polarrSignature = "(II)F";
    public static String cartxSignature = "(FF)I";
    public static String cartySignature = "(FF)I";
    public static String absSignature = "(D)D";
    public static String powerSignature = "(DD)D";
    public static final String logSignature = "(D)D";
    public static String sinSignature = "(D)D";
    public static String cosSignature = "(D)D";
    public static String atanSignature = "(D)D";

    //a6
    public static int cartx(float radius,float theta) {return (int)(radius * Math.cos(theta));}
    public static int carty(float radius,float theta) {
       return (int)(radius * Math.sin(theta));
    }
    public static float polara(int a,int b) {return (float)Math.atan2(b,a);}
    public static float polarr(int a,int b){return (float)Math.hypot(a,b);}


    //a5
    public static double abs(double a){
        return Math.abs(a);
    }
    public static double power(double a,double b){
        return Math.pow(a,b);
    }
    public static double log(double a) {
        return Math.round(Math.log(a));
    }
    public static double sin(double a) {
        return Math.sin(a);
    }
    public static double cos(double a) {
        return Math.cos(a);
    }
    public static double atan(double a) {
        return Math.atan(a);
    }

}
