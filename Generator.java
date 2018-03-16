/**
 * Author: Li Jingwei
 * Stock Market Generator
 */
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;


public class Generator {
	// Create private variables.
	private static HashMap<String, Integer> mapS = new HashMap<String, Integer>(); // Map for storing Ticker and Shares.
	private static HashMap<String, Double> mapP = new HashMap<String, Double>(); // Map for storing Ticker and Price Per share.
	private static HashMap<String, Double> mapD = new HashMap<String, Double>(); // Map for storing Ticker and Dividend.
	private static ArrayList<String> tickers = new ArrayList<>(); // ArrayList to save the tickers.
	private static final String prefix = "      - ";
	private static String secondLine;
	private static String thirdLine = prefix + "$0 of dividend income";
	private static String fourthLine = "   Transactions:";
	private static double dividendIncome = 0;
	private static String previousDate = "";
	
	// Main function.
	public static void main(String[] args) throws IOException {
		// Create ArrayList to store dataset.
		ArrayList<String> dataset = new ArrayList<>();
		// create input variable
		BufferedReader br = new BufferedReader(new FileReader("stock_market.txt"));
		
		int actions_size = 0;
		// separate data into different sets.
		do {
		for (String str: br.readLine().split("\\{")) {
			System.out.println(str);
			if (str.contains("stock_actions")) {
				actions_size = dataset.size();
			}
			if (str.contains("date")) {
				dataset.add(str);
			}
		}} while (br.ready());
		// Create a new array to store date numbers.
		int[] dates = new int[dataset.size()];
		// Loop through data sets
		for (int i = 0; i < dataset.size(); i++) {
			int dataNum = Integer.parseInt(dataset.get(i).replaceAll("[^0-9.]", "").substring(0, 8));
			dates[i] = dataNum;
		}
		// Get the size of actions and stockActions.
		int actionNum = actions_size;
		int stockNum = dataset.size() - actions_size;
		// Create loop numbers.
		int i = 0;
		int j = actionNum;
		
		// Sort and output
		while (i < actionNum) {
			while (j < actionNum + stockNum) {
				if (dates[i] < dates[j]) {
					output(dataset.get(i));
					i++;
				}
				else {
					output(dataset.get(j));
					j++;
				}
				
				if (i == actionNum )
					break;
			}
			break;
		}
		// Output the remaining strings.
		if (i == actionNum) {
			for (int q = j; q < dataset.size(); q++)
				output(dataset.get(q));
		}
		else {
			for (int q = i; q < actionNum; q++)
				output(dataset.get(q));
		}
		br.close();
	}

	/**
	 * Output actions.
	 * @param str
	 * @throws IOException 
	 */	
	public static void output(String str) throws IOException {
		// Create the date string.
		String date = str.substring(9, 19);
		// Store the head line string.
		String headLine = "On " + date.replaceAll("/", "-") + ", you have:";
		// Create the output buffered writer.
		PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter("output.txt", true)));
		// Output the head line.
		out.println(headLine);
		// Write depending on action or stock.
		if (str.contains("action")) {
			// Split to get share value and price value.
			String shareSplit[] = str.split("shares");
			String priceSplit[] = shareSplit[0].split("price");
			// Find the ticker.
			String ticker = priceSplit[1].replaceAll("[^A-Z]", "");
			// Check BUY or SELL action
			String BorS;
			int shareChange = Integer.parseInt(shareSplit[1].replaceAll("[^0-9.]", ""));
			double pricePS = Double.parseDouble(priceSplit[1].replaceAll("[^0-9.]", ""));
			String additionalString = "";
			if (str.contains("BUY")) {
				BorS = "bought";
				// Add information into map.
				if (!mapP.containsKey(ticker)) {
					mapP.put(ticker, pricePS);
					mapS.put(ticker, shareChange);
					mapD.put(ticker, 0.00);
					tickers.add(ticker);
				} else {
					double newPricePerShare = (mapS.get(ticker)*mapP.get(ticker) + shareChange * pricePS)/(mapS.get(ticker)+shareChange);
					mapP.put(ticker, newPricePerShare);
					mapS.put(ticker, mapS.get(ticker) + shareChange);
				}
			}
			else {
				BorS = "sold";
				mapS.put(ticker, mapS.get(ticker) - shareChange);
				double profit = shareChange * (pricePS - mapP.get(ticker));
				String effect;
				if (profit > 0) {
					effect = "profit";
				} else {
					effect = "loss";
				}
					// Add words to the fifth line.
					additionalString += " for a " + effect + " of $" + formatChange(profit);
			}
			// Store the second line.
			for (int i = 0; i < tickers.size(); i++) {
				if (mapS.get(tickers.get(i)) != 0) {
					secondLine = prefix + mapS.get(tickers.get(i)) + " shares of " + tickers.get(i) + " at $" + formatChange(mapP.get(tickers.get(i))) + " per share";
					// Print the second line.
					out.println(secondLine);
				}
			}
			// Print the third line.
			out.println(thirdLine);
			// Print the fourth line.
			out.println(fourthLine);
			// Print the fifth line.
			out.println(prefix + "You " + BorS + " " + shareChange + " shares of " + ticker + " at a price of $" + formatChange(pricePS) + " per share" + additionalString);
		} else if (str.contains("stock")) {
			// Split the stock information.
			String[] splitSplit = str.split("split");
			// Get the stock string.
			String stock = splitSplit[1].replaceAll("[^A-Z]", "");
			// Check whether the stock exists.
			if (!(mapS.containsKey(stock))) {
				return;
			}
			// Check whether split manipulation is valid.
			String splitValue = splitSplit[1].replaceAll("[^0-9]", "");
			if (splitValue.equals("")) {
				// Print the second line.
				for (int i = 0; i < tickers.size(); i++) {
					if (mapS.get(tickers.get(i)) != 0) {
						secondLine = prefix + mapS.get(tickers.get(i)) + " shares of " + tickers.get(i) + " at $" + formatChange(mapP.get(tickers.get(i))) + " per share";
						// Print the second line.
						out.println(secondLine);
					}
				}
				// Manipulate dividend change.
				String dividendSplit[] = splitSplit[0].split("dividend");
				mapD.put(stock, Double.parseDouble(dividendSplit[1].replaceAll("[^0-9.]", "")));
				// Calculate dividend income.
				dividendIncome += mapD.get(stock) * mapS.get(stock);
				// Get and print the third line.
				thirdLine = prefix + "$" + formatChange(dividendIncome) + " of dividend income";
				out.println(thirdLine);
				// Print the remaining lines.
				out.println(fourthLine);
				out.println(prefix + stock + " paid out $" + formatChange(mapD.get(stock)) + " dividend per share, and you have " + mapS.get(stock) + " shares");				
			}
			else {
				// Split manipulation.
				int split = Integer.parseInt(splitValue);
				mapP.put(stock, mapP.get(stock) / split);
				mapS.put(stock, mapS.get(stock) * split);
				// Update the second line.
				for (int i = 0; i < tickers.size(); i++) {
					if (mapS.get(tickers.get(i)) != 0) {
						secondLine = prefix + mapS.get(tickers.get(i)) + " shares of " + tickers.get(i) + " at $" + formatChange(mapP.get(tickers.get(i))) + " per share";
						// Print the second line.
						out.println(secondLine);
					}
				}

				// Print the remaining lines
				out.println(thirdLine);
				out.println(fourthLine);
				out.println(prefix + stock + " split " + split + " to 1, and you have " + mapS.get(stock) + " shares");
			}
		}
		else {
			System.out.println("Error!");
		}
		out.close();
		previousDate = date;
	}
	
	public static String formatChange(double num) {
		return String.format("%.2f", num);
	}
}
