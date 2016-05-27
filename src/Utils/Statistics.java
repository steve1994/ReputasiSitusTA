package Utils;

import org.javatuples.Pair;

import java.util.*;

/**
 * Created by steve on 26/05/2016.
 */
public class Statistics {
    public static double getAverageListLong(List<Long> listLong) {
        long sumElement = 0;
        for (Long element : listLong) {
            sumElement += element;
        }
        double average = 0;
        if (listLong.size() > 0) {
            average = (double) sumElement / (double) listLong.size();
        }
        return average;
    }

    public static double getAverageListDouble(List<Double> listDouble) {
        double sumElement = 0;
        for (Double element : listDouble) {
            sumElement += element;
        }
        double average = 0;
        if (listDouble.size() > 0) {
            average = (double) sumElement / (double) listDouble.size();
        }
        return average;
    }

    public static double getAverageListInteger(List<Integer> listInteger) {
        int sumElement = 0;
        for (Integer element : listInteger) {
            sumElement += element;
        }
        double average = 0;
        if (listInteger.size() > 0) {
            average = (double) sumElement / (double) listInteger.size();
        }
        return average;
    }

    public static Pair<Integer,Long> getMinimumValueListLong(List<Long> listLong) {
        int indexMinValue = 0;
        Long minimumValue = 0L;
        if (listLong.size() > 0) {
            minimumValue = listLong.get(0);
            for (int i = 1; i < listLong.size(); i++) {
                if (listLong.get(i) < minimumValue) {
                    indexMinValue = i;
                    minimumValue = listLong.get(i);
                }
            }
        }
        Pair<Integer,Long> pairMinimum = new Pair<Integer, Long>(indexMinValue,minimumValue);
        return pairMinimum;
    }

    public static Pair<Integer,Double> getMinimumValueListDouble(List<Double> listDouble) {
        int indexMinValue = 0;
        Double minimumValue = 0.0;
        if (listDouble.size() > 0) {
            minimumValue = listDouble.get(0);
            for (int i = 1; i < listDouble.size(); i++) {
                if (listDouble.get(i) < minimumValue) {
                    indexMinValue = i;
                    minimumValue = listDouble.get(i);
                }
            }
        }
        Pair<Integer,Double> pairMinimum = new Pair<Integer, Double>(indexMinValue,minimumValue);
        return pairMinimum;
    }

    public static Pair<Integer,Integer> getMinimumValueListnteger(List<Integer> listInteger) {
        int indexMinValue = 0;
        Integer minimumValue = 0;
        if (listInteger.size() > 0) {
            minimumValue = listInteger.get(0);
            for (int i = 1; i < listInteger.size(); i++) {
                if (listInteger.get(i) < minimumValue) {
                    indexMinValue = i;
                    minimumValue = listInteger.get(i);
                }
            }
        }
        Pair<Integer,Integer> pairMinimum = new Pair<Integer, Integer>(indexMinValue,minimumValue);
        return pairMinimum;
    }

    public static Integer getMostFrequentValueListInteger(List<Integer> listInteger) {
        Integer mostFrequentValue = 0;

        HashSet<Integer> uniqueValues = new HashSet<Integer>();
        if (listInteger.size() > 0) {
            for (Integer value : listInteger) {
                uniqueValues.add(value);
            }
            HashMap<Integer,Integer> mappingValueCounter = new HashMap<Integer, Integer>();
            Iterator e = uniqueValues.iterator();
            while (e.hasNext()) {
                Integer uniqueValue = (Integer) e.next();
                mappingValueCounter.put(uniqueValue,0);
            }
            for (Integer listElement : listInteger) {
                Integer newCounter = mappingValueCounter.get(listElement) + 1;
                mappingValueCounter.replace(listElement,newCounter);
            }
            Integer maximumCounter = 0;
            for (Map.Entry mapElement : mappingValueCounter.entrySet()) {
                Integer uniqueValue = (Integer) mapElement.getKey();
                Integer uniqueValueCounter = (Integer) mapElement.getValue();
                if (uniqueValueCounter > maximumCounter) {
                    mostFrequentValue = uniqueValue;
                    maximumCounter = uniqueValueCounter;
                }
            }
        }

        return mostFrequentValue;
    }

    public static void main(String[] args) {
//        List<Integer> tes = new ArrayList<Integer>();
//        tes.add(1);
//        tes.add(3);
//        tes.add(3);
//        tes.add(4);
//        tes.add(4);
//        tes.add(2);
//        tes.add(2);
//        System.out.println(Statistics.getMostFrequentValueListInteger(tes));
        List<Integer> tes2 = new ArrayList<Integer>();
        int[] array = new int[] {3, 5
                ,3, 5
                ,11, 15, 17
                ,5, 7, 11
                ,7, 9
                ,7, 9
                ,9, 11, 15, 19
                ,7, 11, 13
                ,5, 13
                ,9, 13, 17, 19
        };
        for (int e : array) {
            tes2.add(e);
        }
        tes2.remove(0); tes2.remove(0);

        System.out.println(Statistics.getMostFrequentValueListInteger(tes2));
    }
}
