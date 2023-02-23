////////////////////////////////////////////////////////////////////////////////////////////////////
//Author: Heewon Kim
//Contact: khw0285@gmail.com
//Credit: Fetch.co, Heewon Kim
////////////////////////////////////////////////////////////////////////////////////////////////////

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * This class track how customer using points and there are rule that we should follow
 * 1. We want the oldest points to be spent first (oldest based on transaction timestamp, not the
 * order they’re received)
 * 2. We want no payer's points to go negative
 */
class FetchBackEnd {
    /**
     * This is a main method
     * Use linux and command argument Line:
     * javac FetchBackEnd.java
     * java FetchBackEnd 5000(amount of points to spend) transactions.csv(name of CSV file)
     *
     * @param args
     */
    public static void main(String[] args) {
        if (args.length != 2){
            System.out.println("Error: Need two argument! \n " +
                    "ex) 5000(amount of points to spend) transactions.csv(name of CSV file)");
            return;
        }
        int pointsToSpend = Integer.parseInt(args[0]);
        List<Map<String, Object>> transactions = readCSV(args[1]);
        sortTransactions(transactions);
        Map<String, Integer> balances = calculateBalances(transactions, pointsToSpend);
        System.out.println(balances);
    }

    /**
     * This method can read csv file and parsing
     *
     * @param fileName this is csv file name
     * @return listMap will contain payer, points, timestamp as Key and Value
     */
    public static List<Map<String, Object>> readCSV(String fileName) {
        String line;
        // initialize Map to store payer, points, timestamp
        List<Map<String, Object>> listMap = new ArrayList<>();

        try {
            //parsing a CSV file into BufferedReader class constructor
            BufferedReader br = new BufferedReader(new FileReader(fileName));
            line = br.readLine();
            String[] employee = line.split(",");

            while ((line = br.readLine()) != null) {
                employee = line.split(",");
                Map<String, Object> transactions = new HashMap<>();
                transactions.put("payer", employee[0]);
                transactions.put("points", Integer.parseInt(employee[1]));
                transactions.put("timestamp", employee[2]);
                listMap.add(transactions);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return listMap;
    }


    /**
     * This method sort by timeline
     *
     * @param transactions this is a transactions
     */
    public static void sortTransactions(List<Map<String, Object>> transactions) {
        Collections.sort(transactions, new Comparator<Map<String, Object>>() {
            @Override
            public int compare(Map<String, Object> t1, Map<String, Object> t2) {
                String timestamp1 = (String) t1.get("timestamp");
                String timestamp2 = (String) t2.get("timestamp");
                return timestamp1.compareTo(timestamp2);
            }
        });
    }

    /**
     * This method calculate account balance
     * If 0 < point < pointToSpend, pointsToSpend - points and account balance will be 0 or
     * If pointToSpend < point, point - pointsToSpend and pointsToSpend balance will be 0 or
     * If point < 0, pointsToSpend + points, and account balance will be 0
     * and if pointsToSpend = 0 and payer already exits in balances, current balance + points or
     * if pointsToSpend = 0, add points
     *
     * @param transactions  Contain account point balances
     * @param pointsToSpend Points that need to be spent
     * @return new Point transactions balance
     */
    public static Map<String, Integer> calculateBalances(List<Map<String, Object>> transactions, int pointsToSpend) {
        Map<String, Integer> balances = new HashMap<>();
        for (Map<String, Object> transaction : transactions) {
            String payer = (String) transaction.get("payer");
            int points = (int) transaction.get("points");

            // skip transactions with zero points
            if (points < 0) {
                pointsToSpend += Math.abs(points);
                points = 0;
            }
            if (points == 0) {
                continue;
            }

            // check if payer has enough points to spend
            if (balances.get(payer) != null && balances.get(payer) < 0) {
                throw new IllegalArgumentException("Insufficient points for payer " + payer);
            }
            // spend points
            if (points < pointsToSpend) {
                pointsToSpend -= points;
                // update balances
                balances.put(payer, 0);
            } else if (pointsToSpend == 0 && balances.get(payer) != null) {
                // update balances
                balances.put(payer, points + balances.get(payer));
            } else if (pointsToSpend == 0) {
                // update balances
                balances.put(payer, points);
            } else {
                points -= pointsToSpend;
                pointsToSpend = 0;
                // update balances
                balances.put(payer, points);
            }
        }
        return balances;
    }

}
