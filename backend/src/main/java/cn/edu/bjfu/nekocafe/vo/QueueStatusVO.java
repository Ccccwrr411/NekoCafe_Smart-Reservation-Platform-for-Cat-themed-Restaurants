package cn.edu.bjfu.nekocafe.vo;

import java.util.List;

/**
 * VO - 排队状态响应体（对应接口 J-1）
 */
public class QueueStatusVO {
    private Integer storeId;
    private Integer waitingCount;
    private Integer avgWaitMinutes;
    private Integer currentNumber;
    private Integer myNumber;       // 未取号时为 null
    private Integer myWaitMinutes;
    private List<QueueItemVO> queueList;

    public static class QueueItemVO {
        private Integer number;
        private Integer persons;
        private String type;
        private Integer ahead;

        public Integer getNumber() { return number; }
        public void setNumber(Integer number) { this.number = number; }
        public Integer getPersons() { return persons; }
        public void setPersons(Integer persons) { this.persons = persons; }
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public Integer getAhead() { return ahead; }
        public void setAhead(Integer ahead) { this.ahead = ahead; }
    }

    public Integer getStoreId() { return storeId; }
    public void setStoreId(Integer storeId) { this.storeId = storeId; }
    public Integer getWaitingCount() { return waitingCount; }
    public void setWaitingCount(Integer waitingCount) { this.waitingCount = waitingCount; }
    public Integer getAvgWaitMinutes() { return avgWaitMinutes; }
    public void setAvgWaitMinutes(Integer avgWaitMinutes) { this.avgWaitMinutes = avgWaitMinutes; }
    public Integer getCurrentNumber() { return currentNumber; }
    public void setCurrentNumber(Integer currentNumber) { this.currentNumber = currentNumber; }
    public Integer getMyNumber() { return myNumber; }
    public void setMyNumber(Integer myNumber) { this.myNumber = myNumber; }
    public Integer getMyWaitMinutes() { return myWaitMinutes; }
    public void setMyWaitMinutes(Integer myWaitMinutes) { this.myWaitMinutes = myWaitMinutes; }
    public List<QueueItemVO> getQueueList() { return queueList; }
    public void setQueueList(List<QueueItemVO> queueList) { this.queueList = queueList; }
}
