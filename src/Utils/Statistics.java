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
            for (Map.Entry mapElement : mappingValueCounter.entrySet()) {
                Double uniqueValue = (Double) mapElement.getKey();
                Integer uniqueValueCounter = (Integer) mapElement.getValue();
                System.out.println("Value : " + uniqueValue + " Counter : " + uniqueValueCounter);
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
//
//        List<Integer> tes2 = new ArrayList<Integer>();
//        int[] array = new int[] {1
//                ,1
//                ,1, 3
//                ,3, 5
//                ,5, 9
//                ,15, 17
//                ,3, 9
//                ,7, 11
//                ,11, 13
//                ,17, 19
//        };
//        for (int e : array) {
//            tes2.add(e);
//        }
//        System.out.println(Statistics.getMostFrequentValueListInteger(tes2));

        List<Double> tes2 = new ArrayList<Double>();
        double[] array = new double[] {
                89.68968968968969
                ,84.88488488488488
                ,85.68568568568568
                ,82.18218218218219
                ,82.28228228228228
                ,81.08108108108108
                ,82.48248248248248
                ,81.88188188188188
                ,82.78278278278279
                ,81.08108108108108
                ,81.48148148148148
                ,80.78078078078079
                ,80.18018018018019
                ,79.17917917917919
                ,78.77877877877879
                ,77.57757757757757
                ,77.47747747747748
                ,77.07707707707708
                ,76.77677677677679
                ,77.97797797797797
                ,77.87787787787788
                ,78.47847847847848
                ,78.97897897897897
                ,78.97897897897897
                ,78.87887887887888
                ,78.87887887887888
                ,79.57957957957959
                ,79.67967967967968
                ,79.27927927927928
                ,79.77977977977979
                ,79.17917917917919
        };
        for (double e : array) {
            tes2.add(e);
        }
        System.out.println(Statistics.getMostFrequentValueListDouble(tes2));
    }
}
