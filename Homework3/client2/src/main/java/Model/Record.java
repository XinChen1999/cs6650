package Model;

import java.sql.Timestamp;

public class Record implements Comparable<Record> {

  private long startTime;
  private String methodType;
  private long latency;
  private String statusCode;


  public Record(long startTime, String methodType, long latency, String statusCode) {
    this.startTime = startTime;
    this.methodType = methodType;
    this.latency = latency;
    this.statusCode = statusCode;
  }

  public long getStartTime() {
    return startTime;
  }

  public void setStartTime(long startTime) {
    this.startTime = startTime;
  }

  public String getMethodType() {
    return methodType;
  }

  public void setMethodType(String methodType) {
    this.methodType = methodType;
  }

  public long getLatency() {
    return latency;
  }

  public void setLatency(int latency) {
    this.latency = latency;
  }

  public String getStatusCode() {
    return statusCode;
  }

  public void setStatusCode(String statusCode) {
    this.statusCode = statusCode;
  }
  @Override
  public String toString(){
    Timestamp timestamp = new Timestamp(this.startTime);
    return timestamp + ", " + methodType + ", " + latency + ", " + statusCode;
  }


  @Override
  public int compareTo(Record o) {
    if (this.getLatency() - o.getLatency() > 0) {
      return 1;
    } else if (this.getLatency() - o.getLatency() == 0) {
      return 0;
    }
    return -1;
  }
}
