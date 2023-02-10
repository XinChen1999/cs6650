package model;

import java.math.BigInteger;
import java.util.*;
import java.util.function.IntBinaryOperator;
import java.util.stream.Collectors;

public class StatisticGenerator {

    private static List<String[]> records;
    private static List<Integer> latencies;

    public StatisticGenerator(List<String[]> records) {
        this.records = records;
        this.latencies = new ArrayList<>();

        for (int i = 1; i < records.size(); i++) {
            this.latencies.add(Integer.valueOf(records.get(i)[2]));
        }

        Collections.sort(this.latencies);
    }

    public double getMeanValue() {
        OptionalDouble average = this.latencies
                .stream()
                .mapToDouble(a -> a)
                .average();

        return average.isPresent() ? average.getAsDouble() : 0;
    }

    public double getMedianValue() {
        if (this.latencies.size() % 2 == 1) {
            return (double)this.latencies.get(this.records.size() / 2);
        } else {
            return (double)(this.latencies.get(this.latencies.size() / 2 - 1) + this.latencies.get(this.latencies.size() / 2)) / 2;
        }
    }

    public double get99PercentValue() {
        return (double)latencies.get((int)Math.ceil(latencies.size() * 0.99));
    }

    public double getMinValue() {
        return (double)this.latencies.get(0);
    }

    public double getMaxValue() {
        return (double)this.latencies.get(latencies.size() - 1);
    }

    public List<String[]> getPlot(){
        List<Long> startTimes = this.records
                .stream()
                .filter(a -> !Objects.equals(a[0], "Start Time"))
                .map(a -> Long.parseLong(a[0]))
                .sorted()
                .collect(Collectors.toList());
        Long minStartTime = startTimes.get(0);
        Long maxStartTime = startTimes.get(startTimes.size() - 1);
        Integer totalSeconds = (int) (maxStartTime - minStartTime) / 1000 + 1;
        int[] plot = new int[totalSeconds];
        Arrays.fill(plot, 0);
        for(Long startTime: startTimes){
            Integer second =(int) (startTime - minStartTime) / 1000;
            plot[second] += 1;
        }

        List<String[]> plotStatistics = new ArrayList<>();
        plotStatistics.add(new String[]{"Second", "Number of Request"});
        for(int i = 0; i < plot.length; i++){
            String[] reqRecord = new String[2];
            reqRecord[0] = String.valueOf(i);
            reqRecord[1]= String.valueOf(plot[i]);
            plotStatistics.add(reqRecord);
        }

        return plotStatistics;
    }
}
