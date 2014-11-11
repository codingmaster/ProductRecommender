package de.hpi.semrecsys.strategy;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import de.hpi.semrecsys.model.Product;

public class RecommendationValidator {

	public static boolean isValidUrl(String urlString) {
		try {
			URL url = new URL(urlString);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestProperty("User-Agent",
					"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");
			conn.connect();
			return conn.getResponseCode() == 200;
			// open the stream and put it into BufferedReader
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	public static boolean isValidRecommendation(Product recommendedProduct) {
		if (recommendedProduct == null || recommendedProduct.getTitle() == null
				|| recommendedProduct.getImgPathes().isEmpty()) {
			return false;
		}

		for (String imgPath : recommendedProduct.getImgPathes()) {
			if (!RecommendationValidator.isValidUrl(imgPath)) {
				return false;
			}
		}

		return true;
	}

}
