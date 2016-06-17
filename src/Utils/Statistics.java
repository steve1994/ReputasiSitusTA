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

    public static Double getMostFrequentValueListDouble(List<Double> listDouble) {
        Double mostFrequentValue = 0.0;

        HashSet<Double> uniqueValues = new HashSet<Double>();
        if (listDouble.size() > 0) {
            for (Double value : listDouble) {
                uniqueValues.add(value);
            }
            HashMap<Double,Integer> mappingValueCounter = new HashMap<Double, Integer>();
            Iterator e = uniqueValues.iterator();
            while (e.hasNext()) {
                Double uniqueValue = (Double) e.next();
                mappingValueCounter.put(uniqueValue,0);
            }
            for (Double listElement : listDouble) {
                Integer newCounter = mappingValueCounter.get(listElement) + 1;
                mappingValueCounter.replace(listElement,newCounter);
            }
            Integer maximumCounter = 0;
            for (Map.Entry mapElement : mappingValueCounter.entrySet()) {
                Double uniqueValue = (Double) mapElement.getKey();
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
        int[] array = new int[] {1
                ,1
                ,1, 3
                ,3, 5
                ,5, 9
                ,15, 17
                ,3, 9
                ,7, 11
                ,11, 13
                ,17, 19
        };
        for (int e : array) {
            tes2.add(e);
        }
        System.out.println(Statistics.getMostFrequentValueListInteger(tes2));

//        List<Double> tes2 = new ArrayList<Double>();
//        double[] array = new double[] {85.79, 85.39, 83.78, 82.48, 82.38, 81.58, 81.48, 81.38, 81.48, 81.08, 80.78, 80.18, 80.88, 81.08, 80.98, 80.78};
//        for (double e : array) {
//            tes2.add(e);
//        }
//        System.out.println(Statistics.getMostFrequentValueListDouble(tes2));
    }
}
