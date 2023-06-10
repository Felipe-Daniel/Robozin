package org.robots.helpers;

public class GenericHelper {
	// SEE: https://robowiki.net/wiki/Bin_Smoothing
	public static void logHit(double[] bins, int index) {
        for (int x = 0; x < bins.length; x++) {
            bins[x] += 1 / Math.pow(Math.abs(x - index) + 1, 2);
        }
    }
}
