package com.couple.schedule_meeting.util;

public class GeoToGridConverter {
    private static final double RE = 6371.00877; // Earth radius (km)
    private static final double GRID = 5.0;      // Grid spacing (km)
    private static final double SLAT1 = 30.0;    // Projection latitude 1 (degree)
    private static final double SLAT2 = 60.0;    // Projection latitude 2 (degree)
    private static final double OLON = 126.0;    // Reference longitude (degree)
    private static final double OLAT = 38.0;     // Reference latitude (degree)
    private static final double XO = 43;         // Reference x-coordinate (GRID)
    private static final double YO = 136;        // Reference y-coordinate (GRID)

    public static class Grid {
        public final int x;
        public final int y;

        public Grid(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    public static Grid convert(double lat, double lon) {
        double DEGRAD = Math.PI / 180.0;
        double re = RE / GRID;
        double slat1 = SLAT1 * DEGRAD;
        double slat2 = SLAT2 * DEGRAD;
        double olon = OLON * DEGRAD;
        double olat = OLAT * DEGRAD;

        double sn = Math.tan(Math.PI * 0.25 + slat2 * 0.5) / Math.tan(Math.PI * 0.25 + slat1 * 0.5);
        sn = Math.log(Math.cos(slat1) / Math.cos(slat2)) / Math.log(sn);
        double sf = Math.tan(Math.PI * 0.25 + slat1 * 0.5);
        sf = Math.pow(sf, sn) * Math.cos(slat1) / sn;
        double ro = Math.tan(Math.PI * 0.25 + olat * 0.5);
        ro = re * sf / Math.pow(ro, sn);

        double ra = Math.tan(Math.PI * 0.25 + lat * DEGRAD * 0.5);
        ra = re * sf / Math.pow(ra, sn);
        double theta = lon * DEGRAD - olon;
        if (theta > Math.PI) theta -= 2.0 * Math.PI;
        if (theta < -Math.PI) theta += 2.0 * Math.PI;
        theta *= sn;

        int x = (int)(ra * Math.sin(theta) + XO + 0.5);
        int y = (int)(ro - ra * Math.cos(theta) + YO + 0.5);

        return new Grid(x, y);
    }
} 