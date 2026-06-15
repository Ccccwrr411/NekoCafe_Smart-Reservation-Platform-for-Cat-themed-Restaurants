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
    private Long myQueueId;         // 我的排队记录ID（用于确认叫号）
    private Boolean called;         // 是否刚被叫号（店员已叫到该用户）
    private List<QueueItemVO> queueList;     // 等待中（WAITING）
    private List<QueueItemVO> calledList;    // 已叫号（CALLED / KNOWN）
    private List<QueueItemVO> missedList;    // 已过号（MISSED）

    public static class QueueItemVO {
        private Long queueId;
        private Integer number;
        private Integer persons;
        private String type;
        private Integer ahead;
        private String status;       // WAITING/CALLED/KNOWN/MISSED
        private Boolean isMine;      // 是否当前用户的排队号
        private String userName;     // 用户昵称（店员端显示用）

        public Long getQueueId() { return queueId; }
        public void setQueueId(Long queueId) { this.queueId = queueId; }
        public Integer getNumber() { return number; }
        public void setNumber(Integer number) { this.number = number; }
        public Integer getPersons() { return persons; }
        public void setPersons(Integer persons) { this.persons = persons; }
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public Integer getAhead() { return ahead; }
        public void setAhead(Integer ahead) { this.ahead = ahead; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public Boolean getIsMine() { return isMine; }
        public void setIsMine(Boolean isMine) { this.isMine = isMine; }
        public String getUserName() { return userName; }
        public void setUserName(String userName) { this.userName = userName; }
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
    public Long getMyQueueId() { return myQueueId; }
    public void setMyQueueId(Long myQueueId) { this.myQueueId = myQueueId; }
    public Boolean getCalled() { return called; }
    public void setCalled(Boolean called) { this.called = called; }
    public List<QueueItemVO> getQueueList() { return queueList; }
    public void setQueueList(List<QueueItemVO> queueList) { this.queueList = queueList; }
    public List<QueueItemVO> getCalledList() { return calledList; }
    public void setCalledList(List<QueueItemVO> calledList) { this.calledList = calledList; }
    public List<QueueItemVO> getMissedList() { return missedList; }
    public void setMissedList(List<QueueItemVO> missedList) { this.missedList = missedList; }
}
