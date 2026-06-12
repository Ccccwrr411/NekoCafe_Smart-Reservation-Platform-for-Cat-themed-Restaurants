package cn.edu.bjfu.nekocafe.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class QueueExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public QueueExample() {
        oredCriteria = new ArrayList<>();
    }

    public void setOrderByClause(String orderByClause) {
        this.orderByClause = orderByClause;
    }

    public String getOrderByClause() {
        return orderByClause;
    }

    public void setDistinct(boolean distinct) {
        this.distinct = distinct;
    }

    public boolean isDistinct() {
        return distinct;
    }

    public List<Criteria> getOredCriteria() {
        return oredCriteria;
    }

    public void or(Criteria criteria) {
        oredCriteria.add(criteria);
    }

    public Criteria or() {
        Criteria criteria = createCriteriaInternal();
        oredCriteria.add(criteria);
        return criteria;
    }

    public Criteria createCriteria() {
        Criteria criteria = createCriteriaInternal();
        if (oredCriteria.size() == 0) {
            oredCriteria.add(criteria);
        }
        return criteria;
    }

    protected Criteria createCriteriaInternal() {
        Criteria criteria = new Criteria();
        return criteria;
    }

    public void clear() {
        oredCriteria.clear();
        orderByClause = null;
        distinct = false;
    }

    protected abstract static class GeneratedCriteria {
        protected List<Criterion> criteria;

        protected GeneratedCriteria() {
            super();
            criteria = new ArrayList<>();
        }

        public boolean isValid() {
            return criteria.size() > 0;
        }

        public List<Criterion> getAllCriteria() {
            return criteria;
        }

        public List<Criterion> getCriteria() {
            return criteria;
        }

        protected void addCriterion(String condition) {
            if (condition == null) {
                throw new RuntimeException("Value for condition cannot be null");
            }
            criteria.add(new Criterion(condition));
        }

        protected void addCriterion(String condition, Object value, String property) {
            if (value == null) {
                throw new RuntimeException("Value for " + property + " cannot be null");
            }
            criteria.add(new Criterion(condition, value));
        }

        protected void addCriterion(String condition, Object value1, Object value2, String property) {
            if (value1 == null || value2 == null) {
                throw new RuntimeException("Between values for " + property + " cannot be null");
            }
            criteria.add(new Criterion(condition, value1, value2));
        }

        public Criteria andQueueIdIsNull() {
            addCriterion("queue_id is null");
            return (Criteria) this;
        }

        public Criteria andQueueIdIsNotNull() {
            addCriterion("queue_id is not null");
            return (Criteria) this;
        }

        public Criteria andQueueIdEqualTo(Long value) {
            addCriterion("queue_id =", value, "queueId");
            return (Criteria) this;
        }

        public Criteria andQueueIdNotEqualTo(Long value) {
            addCriterion("queue_id <>", value, "queueId");
            return (Criteria) this;
        }

        public Criteria andQueueIdGreaterThan(Long value) {
            addCriterion("queue_id >", value, "queueId");
            return (Criteria) this;
        }

        public Criteria andQueueIdGreaterThanOrEqualTo(Long value) {
            addCriterion("queue_id >=", value, "queueId");
            return (Criteria) this;
        }

        public Criteria andQueueIdLessThan(Long value) {
            addCriterion("queue_id <", value, "queueId");
            return (Criteria) this;
        }

        public Criteria andQueueIdLessThanOrEqualTo(Long value) {
            addCriterion("queue_id <=", value, "queueId");
            return (Criteria) this;
        }

        public Criteria andQueueIdIn(List<Long> values) {
            addCriterion("queue_id in", values, "queueId");
            return (Criteria) this;
        }

        public Criteria andQueueIdNotIn(List<Long> values) {
            addCriterion("queue_id not in", values, "queueId");
            return (Criteria) this;
        }

        public Criteria andQueueIdBetween(Long value1, Long value2) {
            addCriterion("queue_id between", value1, value2, "queueId");
            return (Criteria) this;
        }

        public Criteria andQueueIdNotBetween(Long value1, Long value2) {
            addCriterion("queue_id not between", value1, value2, "queueId");
            return (Criteria) this;
        }

        public Criteria andStoreIdIsNull() {
            addCriterion("store_id is null");
            return (Criteria) this;
        }

        public Criteria andStoreIdIsNotNull() {
            addCriterion("store_id is not null");
            return (Criteria) this;
        }

        public Criteria andStoreIdEqualTo(Integer value) {
            addCriterion("store_id =", value, "storeId");
            return (Criteria) this;
        }

        public Criteria andStoreIdNotEqualTo(Integer value) {
            addCriterion("store_id <>", value, "storeId");
            return (Criteria) this;
        }

        public Criteria andStoreIdGreaterThan(Integer value) {
            addCriterion("store_id >", value, "storeId");
            return (Criteria) this;
        }

        public Criteria andStoreIdGreaterThanOrEqualTo(Integer value) {
            addCriterion("store_id >=", value, "storeId");
            return (Criteria) this;
        }

        public Criteria andStoreIdLessThan(Integer value) {
            addCriterion("store_id <", value, "storeId");
            return (Criteria) this;
        }

        public Criteria andStoreIdLessThanOrEqualTo(Integer value) {
            addCriterion("store_id <=", value, "storeId");
            return (Criteria) this;
        }

        public Criteria andStoreIdIn(List<Integer> values) {
            addCriterion("store_id in", values, "storeId");
            return (Criteria) this;
        }

        public Criteria andStoreIdNotIn(List<Integer> values) {
            addCriterion("store_id not in", values, "storeId");
            return (Criteria) this;
        }

        public Criteria andStoreIdBetween(Integer value1, Integer value2) {
            addCriterion("store_id between", value1, value2, "storeId");
            return (Criteria) this;
        }

        public Criteria andStoreIdNotBetween(Integer value1, Integer value2) {
            addCriterion("store_id not between", value1, value2, "storeId");
            return (Criteria) this;
        }

        public Criteria andUserIdIsNull() {
            addCriterion("user_id is null");
            return (Criteria) this;
        }

        public Criteria andUserIdIsNotNull() {
            addCriterion("user_id is not null");
            return (Criteria) this;
        }

        public Criteria andUserIdEqualTo(Long value) {
            addCriterion("user_id =", value, "userId");
            return (Criteria) this;
        }

        public Criteria andUserIdNotEqualTo(Long value) {
            addCriterion("user_id <>", value, "userId");
            return (Criteria) this;
        }

        public Criteria andUserIdGreaterThan(Long value) {
            addCriterion("user_id >", value, "userId");
            return (Criteria) this;
        }

        public Criteria andUserIdGreaterThanOrEqualTo(Long value) {
            addCriterion("user_id >=", value, "userId");
            return (Criteria) this;
        }

        public Criteria andUserIdLessThan(Long value) {
            addCriterion("user_id <", value, "userId");
            return (Criteria) this;
        }

        public Criteria andUserIdLessThanOrEqualTo(Long value) {
            addCriterion("user_id <=", value, "userId");
            return (Criteria) this;
        }

        public Criteria andUserIdIn(List<Long> values) {
            addCriterion("user_id in", values, "userId");
            return (Criteria) this;
        }

        public Criteria andUserIdNotIn(List<Long> values) {
            addCriterion("user_id not in", values, "userId");
            return (Criteria) this;
        }

        public Criteria andUserIdBetween(Long value1, Long value2) {
            addCriterion("user_id between", value1, value2, "userId");
            return (Criteria) this;
        }

        public Criteria andUserIdNotBetween(Long value1, Long value2) {
            addCriterion("user_id not between", value1, value2, "userId");
            return (Criteria) this;
        }

        public Criteria andPartySizeIsNull() {
            addCriterion("party_size is null");
            return (Criteria) this;
        }

        public Criteria andPartySizeIsNotNull() {
            addCriterion("party_size is not null");
            return (Criteria) this;
        }

        public Criteria andPartySizeEqualTo(Integer value) {
            addCriterion("party_size =", value, "partySize");
            return (Criteria) this;
        }

        public Criteria andPartySizeNotEqualTo(Integer value) {
            addCriterion("party_size <>", value, "partySize");
            return (Criteria) this;
        }

        public Criteria andPartySizeGreaterThan(Integer value) {
            addCriterion("party_size >", value, "partySize");
            return (Criteria) this;
        }

        public Criteria andPartySizeGreaterThanOrEqualTo(Integer value) {
            addCriterion("party_size >=", value, "partySize");
            return (Criteria) this;
        }

        public Criteria andPartySizeLessThan(Integer value) {
            addCriterion("party_size <", value, "partySize");
            return (Criteria) this;
        }

        public Criteria andPartySizeLessThanOrEqualTo(Integer value) {
            addCriterion("party_size <=", value, "partySize");
            return (Criteria) this;
        }

        public Criteria andPartySizeIn(List<Integer> values) {
            addCriterion("party_size in", values, "partySize");
            return (Criteria) this;
        }

        public Criteria andPartySizeNotIn(List<Integer> values) {
            addCriterion("party_size not in", values, "partySize");
            return (Criteria) this;
        }

        public Criteria andPartySizeBetween(Integer value1, Integer value2) {
            addCriterion("party_size between", value1, value2, "partySize");
            return (Criteria) this;
        }

        public Criteria andPartySizeNotBetween(Integer value1, Integer value2) {
            addCriterion("party_size not between", value1, value2, "partySize");
            return (Criteria) this;
        }

        public Criteria andPreferredTableTypeIsNull() {
            addCriterion("preferred_table_type is null");
            return (Criteria) this;
        }

        public Criteria andPreferredTableTypeIsNotNull() {
            addCriterion("preferred_table_type is not null");
            return (Criteria) this;
        }

        public Criteria andPreferredTableTypeEqualTo(String value) {
            addCriterion("preferred_table_type =", value, "preferredTableType");
            return (Criteria) this;
        }

        public Criteria andPreferredTableTypeNotEqualTo(String value) {
            addCriterion("preferred_table_type <>", value, "preferredTableType");
            return (Criteria) this;
        }

        public Criteria andPreferredTableTypeGreaterThan(String value) {
            addCriterion("preferred_table_type >", value, "preferredTableType");
            return (Criteria) this;
        }

        public Criteria andPreferredTableTypeGreaterThanOrEqualTo(String value) {
            addCriterion("preferred_table_type >=", value, "preferredTableType");
            return (Criteria) this;
        }

        public Criteria andPreferredTableTypeLessThan(String value) {
            addCriterion("preferred_table_type <", value, "preferredTableType");
            return (Criteria) this;
        }

        public Criteria andPreferredTableTypeLessThanOrEqualTo(String value) {
            addCriterion("preferred_table_type <=", value, "preferredTableType");
            return (Criteria) this;
        }

        public Criteria andPreferredTableTypeLike(String value) {
            addCriterion("preferred_table_type like", value, "preferredTableType");
            return (Criteria) this;
        }

        public Criteria andPreferredTableTypeNotLike(String value) {
            addCriterion("preferred_table_type not like", value, "preferredTableType");
            return (Criteria) this;
        }

        public Criteria andPreferredTableTypeIn(List<String> values) {
            addCriterion("preferred_table_type in", values, "preferredTableType");
            return (Criteria) this;
        }

        public Criteria andPreferredTableTypeNotIn(List<String> values) {
            addCriterion("preferred_table_type not in", values, "preferredTableType");
            return (Criteria) this;
        }

        public Criteria andPreferredTableTypeBetween(String value1, String value2) {
            addCriterion("preferred_table_type between", value1, value2, "preferredTableType");
            return (Criteria) this;
        }

        public Criteria andPreferredTableTypeNotBetween(String value1, String value2) {
            addCriterion("preferred_table_type not between", value1, value2, "preferredTableType");
            return (Criteria) this;
        }

        public Criteria andStatusIsNull() {
            addCriterion("status is null");
            return (Criteria) this;
        }

        public Criteria andStatusIsNotNull() {
            addCriterion("status is not null");
            return (Criteria) this;
        }

        public Criteria andStatusEqualTo(String value) {
            addCriterion("status =", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusNotEqualTo(String value) {
            addCriterion("status <>", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusGreaterThan(String value) {
            addCriterion("status >", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusGreaterThanOrEqualTo(String value) {
            addCriterion("status >=", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusLessThan(String value) {
            addCriterion("status <", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusLessThanOrEqualTo(String value) {
            addCriterion("status <=", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusLike(String value) {
            addCriterion("status like", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusNotLike(String value) {
            addCriterion("status not like", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusIn(List<String> values) {
            addCriterion("status in", values, "status");
            return (Criteria) this;
        }

        public Criteria andStatusNotIn(List<String> values) {
            addCriterion("status not in", values, "status");
            return (Criteria) this;
        }

        public Criteria andStatusBetween(String value1, String value2) {
            addCriterion("status between", value1, value2, "status");
            return (Criteria) this;
        }

        public Criteria andStatusNotBetween(String value1, String value2) {
            addCriterion("status not between", value1, value2, "status");
            return (Criteria) this;
        }

        public Criteria andQueueNumberIsNull() {
            addCriterion("queue_number is null");
            return (Criteria) this;
        }

        public Criteria andQueueNumberIsNotNull() {
            addCriterion("queue_number is not null");
            return (Criteria) this;
        }

        public Criteria andQueueNumberEqualTo(String value) {
            addCriterion("queue_number =", value, "queueNumber");
            return (Criteria) this;
        }

        public Criteria andQueueNumberNotEqualTo(String value) {
            addCriterion("queue_number <>", value, "queueNumber");
            return (Criteria) this;
        }

        public Criteria andQueueNumberGreaterThan(String value) {
            addCriterion("queue_number >", value, "queueNumber");
            return (Criteria) this;
        }

        public Criteria andQueueNumberGreaterThanOrEqualTo(String value) {
            addCriterion("queue_number >=", value, "queueNumber");
            return (Criteria) this;
        }

        public Criteria andQueueNumberLessThan(String value) {
            addCriterion("queue_number <", value, "queueNumber");
            return (Criteria) this;
        }

        public Criteria andQueueNumberLessThanOrEqualTo(String value) {
            addCriterion("queue_number <=", value, "queueNumber");
            return (Criteria) this;
        }

        public Criteria andQueueNumberLike(String value) {
            addCriterion("queue_number like", value, "queueNumber");
            return (Criteria) this;
        }

        public Criteria andQueueNumberNotLike(String value) {
            addCriterion("queue_number not like", value, "queueNumber");
            return (Criteria) this;
        }

        public Criteria andQueueNumberIn(List<String> values) {
            addCriterion("queue_number in", values, "queueNumber");
            return (Criteria) this;
        }

        public Criteria andQueueNumberNotIn(List<String> values) {
            addCriterion("queue_number not in", values, "queueNumber");
            return (Criteria) this;
        }

        public Criteria andQueueNumberBetween(String value1, String value2) {
            addCriterion("queue_number between", value1, value2, "queueNumber");
            return (Criteria) this;
        }

        public Criteria andQueueNumberNotBetween(String value1, String value2) {
            addCriterion("queue_number not between", value1, value2, "queueNumber");
            return (Criteria) this;
        }

        public Criteria andCalledAtIsNull() {
            addCriterion("called_at is null");
            return (Criteria) this;
        }

        public Criteria andCalledAtIsNotNull() {
            addCriterion("called_at is not null");
            return (Criteria) this;
        }

        public Criteria andCalledAtEqualTo(Date value) {
            addCriterion("called_at =", value, "calledAt");
            return (Criteria) this;
        }

        public Criteria andCalledAtNotEqualTo(Date value) {
            addCriterion("called_at <>", value, "calledAt");
            return (Criteria) this;
        }

        public Criteria andCalledAtGreaterThan(Date value) {
            addCriterion("called_at >", value, "calledAt");
            return (Criteria) this;
        }

        public Criteria andCalledAtGreaterThanOrEqualTo(Date value) {
            addCriterion("called_at >=", value, "calledAt");
            return (Criteria) this;
        }

        public Criteria andCalledAtLessThan(Date value) {
            addCriterion("called_at <", value, "calledAt");
            return (Criteria) this;
        }

        public Criteria andCalledAtLessThanOrEqualTo(Date value) {
            addCriterion("called_at <=", value, "calledAt");
            return (Criteria) this;
        }

        public Criteria andCalledAtIn(List<Date> values) {
            addCriterion("called_at in", values, "calledAt");
            return (Criteria) this;
        }

        public Criteria andCalledAtNotIn(List<Date> values) {
            addCriterion("called_at not in", values, "calledAt");
            return (Criteria) this;
        }

        public Criteria andCalledAtBetween(Date value1, Date value2) {
            addCriterion("called_at between", value1, value2, "calledAt");
            return (Criteria) this;
        }

        public Criteria andCalledAtNotBetween(Date value1, Date value2) {
            addCriterion("called_at not between", value1, value2, "calledAt");
            return (Criteria) this;
        }

        public Criteria andSeatedTableIdIsNull() {
            addCriterion("seated_table_id is null");
            return (Criteria) this;
        }

        public Criteria andSeatedTableIdIsNotNull() {
            addCriterion("seated_table_id is not null");
            return (Criteria) this;
        }

        public Criteria andSeatedTableIdEqualTo(Integer value) {
            addCriterion("seated_table_id =", value, "seatedTableId");
            return (Criteria) this;
        }

        public Criteria andSeatedTableIdNotEqualTo(Integer value) {
            addCriterion("seated_table_id <>", value, "seatedTableId");
            return (Criteria) this;
        }

        public Criteria andSeatedTableIdGreaterThan(Integer value) {
            addCriterion("seated_table_id >", value, "seatedTableId");
            return (Criteria) this;
        }

        public Criteria andSeatedTableIdGreaterThanOrEqualTo(Integer value) {
            addCriterion("seated_table_id >=", value, "seatedTableId");
            return (Criteria) this;
        }

        public Criteria andSeatedTableIdLessThan(Integer value) {
            addCriterion("seated_table_id <", value, "seatedTableId");
            return (Criteria) this;
        }

        public Criteria andSeatedTableIdLessThanOrEqualTo(Integer value) {
            addCriterion("seated_table_id <=", value, "seatedTableId");
            return (Criteria) this;
        }

        public Criteria andSeatedTableIdIn(List<Integer> values) {
            addCriterion("seated_table_id in", values, "seatedTableId");
            return (Criteria) this;
        }

        public Criteria andSeatedTableIdNotIn(List<Integer> values) {
            addCriterion("seated_table_id not in", values, "seatedTableId");
            return (Criteria) this;
        }

        public Criteria andSeatedTableIdBetween(Integer value1, Integer value2) {
            addCriterion("seated_table_id between", value1, value2, "seatedTableId");
            return (Criteria) this;
        }

        public Criteria andSeatedTableIdNotBetween(Integer value1, Integer value2) {
            addCriterion("seated_table_id not between", value1, value2, "seatedTableId");
            return (Criteria) this;
        }

        public Criteria andCreatedAtIsNull() {
            addCriterion("created_at is null");
            return (Criteria) this;
        }

        public Criteria andCreatedAtIsNotNull() {
            addCriterion("created_at is not null");
            return (Criteria) this;
        }

        public Criteria andCreatedAtEqualTo(Date value) {
            addCriterion("created_at =", value, "createdAt");
            return (Criteria) this;
        }

        public Criteria andCreatedAtNotEqualTo(Date value) {
            addCriterion("created_at <>", value, "createdAt");
            return (Criteria) this;
        }

        public Criteria andCreatedAtGreaterThan(Date value) {
            addCriterion("created_at >", value, "createdAt");
            return (Criteria) this;
        }

        public Criteria andCreatedAtGreaterThanOrEqualTo(Date value) {
            addCriterion("created_at >=", value, "createdAt");
            return (Criteria) this;
        }

        public Criteria andCreatedAtLessThan(Date value) {
            addCriterion("created_at <", value, "createdAt");
            return (Criteria) this;
        }

        public Criteria andCreatedAtLessThanOrEqualTo(Date value) {
            addCriterion("created_at <=", value, "createdAt");
            return (Criteria) this;
        }

        public Criteria andCreatedAtIn(List<Date> values) {
            addCriterion("created_at in", values, "createdAt");
            return (Criteria) this;
        }

        public Criteria andCreatedAtNotIn(List<Date> values) {
            addCriterion("created_at not in", values, "createdAt");
            return (Criteria) this;
        }

        public Criteria andCreatedAtBetween(Date value1, Date value2) {
            addCriterion("created_at between", value1, value2, "createdAt");
            return (Criteria) this;
        }

        public Criteria andCreatedAtNotBetween(Date value1, Date value2) {
            addCriterion("created_at not between", value1, value2, "createdAt");
            return (Criteria) this;
        }
    }

    public static class Criteria extends GeneratedCriteria {
        protected Criteria() {
            super();
        }
    }

    public static class Criterion {
        private String condition;

        private Object value;

        private Object secondValue;

        private boolean noValue;

        private boolean singleValue;

        private boolean betweenValue;

        private boolean listValue;

        private String typeHandler;

        public String getCondition() {
            return condition;
        }

        public Object getValue() {
            return value;
        }

        public Object getSecondValue() {
            return secondValue;
        }

        public boolean isNoValue() {
            return noValue;
        }

        public boolean isSingleValue() {
            return singleValue;
        }

        public boolean isBetweenValue() {
            return betweenValue;
        }

        public boolean isListValue() {
            return listValue;
        }

        public String getTypeHandler() {
            return typeHandler;
        }

        protected Criterion(String condition) {
            super();
            this.condition = condition;
            this.typeHandler = null;
            this.noValue = true;
        }

        protected Criterion(String condition, Object value, String typeHandler) {
            super();
            this.condition = condition;
            this.value = value;
            this.typeHandler = typeHandler;
            if (value instanceof List<?>) {
                this.listValue = true;
            } else {
                this.singleValue = true;
            }
        }

        protected Criterion(String condition, Object value) {
            this(condition, value, null);
        }

        protected Criterion(String condition, Object value, Object secondValue, String typeHandler) {
            super();
            this.condition = condition;
            this.value = value;
            this.secondValue = secondValue;
            this.typeHandler = typeHandler;
            this.betweenValue = true;
        }

        protected Criterion(String condition, Object value, Object secondValue) {
            this(condition, value, secondValue, null);
        }
    }
}