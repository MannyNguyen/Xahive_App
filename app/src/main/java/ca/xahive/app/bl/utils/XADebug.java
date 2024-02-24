package ca.xahive.app.bl.utils;

public class XADebug {
    public static void d(String s) {
        if (Config.DEBUG) {
            System.err.println(s);
        }
    }
}
