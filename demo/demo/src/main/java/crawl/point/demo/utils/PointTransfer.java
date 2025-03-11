package crawl.point.demo.utils;

import java.text.DecimalFormat;

public class PointTransfer {
    public static double transferPoint(String point) {
        DecimalFormat df = new DecimalFormat("#.##");
        double pointRes = Double.parseDouble(point);
        double result = pointRes < 30 ? pointRes / 30 * 100 : pointRes;
        return Double.parseDouble(df.format(result)); // Ép lại về kiểu double
    }
}
