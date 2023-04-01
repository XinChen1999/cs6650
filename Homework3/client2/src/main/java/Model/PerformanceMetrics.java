package Model;

import java.util.Collections;
import java.util.List;

public class PerformanceMetrics {
  private List<Record> requestRecords;

  public PerformanceMetrics(List<Record> requestRecords) {
    this.requestRecords = requestRecords;
    Collections.sort(this.requestRecords);
  }

  public double getMeanLatency() {
    long sum = 0;
    for (Record record : this.requestRecords) {
      sum += record.getLatency();
    }

    return (double) sum / (double)(this.requestRecords.size());
  }

  public double getMedianLatency() {
    if (this.requestRecords.size() % 2 == 1) {
      return requestRecords.get(requestRecords.size() / 2).getLatency();
    } else {
      return (requestRecords.get(requestRecords.size() / 2 - 1).getLatency() + requestRecords.get(requestRecords.size() / 2).getLatency()) / 2;
    }
  }

  public long get99thPercentileLatency() {
    return requestRecords.get((int)Math.floor(requestRecords.size()*0.99)).getLatency();
  }

  public long getMinLatency() {
    return requestRecords.get(0).getLatency();
  }

  public long getMaxLatency() {
    return requestRecords.get(requestRecords.size() - 1).getLatency();
  }
}
