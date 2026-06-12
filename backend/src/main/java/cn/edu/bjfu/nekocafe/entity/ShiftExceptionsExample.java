package cn.edu.bjfu.nekocafe.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class ShiftExceptionsExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public ShiftExceptionsExample() {
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

        protected void addCriterionForJDBCDate(String condition, Date value, String property) {
            if (value == null) {
                throw new RuntimeException("Value for " + property + " cannot be null");
            }
            addCriterion(condition, new java.sql.Date(value.getTime()), property);
        }

        protected void addCriterionForJDBCDate(String condition, List<Date> values, String property) {
            if (values == null || values.size() == 0) {
                throw new RuntimeException("Value list for " + property + " cannot be null or empty");
            }
            List<java.sql.Date> dateList = new ArrayList<>();
            Iterator<Date> iter = values.iterator();
            while (iter.hasNext()) {
                dateList.add(new java.sql.Date(iter.next().getTime()));
            }
            addCriterion(condition, dateList, property);
        }

        protected void addCriterionForJDBCDate(String condition, Date value1, Date value2, String property) {
            if (value1 == null || value2 == null) {
                throw new RuntimeException("Between values for " + property + " cannot be null");
            }
            addCriterion(condition, new java.sql.Date(value1.getTime()), new java.sql.Date(value2.getTime()), property);
        }

        public Criteria andExceptionIdIsNull() {
            addCriterion("exception_id is null");
            return (Criteria) this;
        }

        public Criteria andExceptionIdIsNotNull() {
            addCriterion("exception_id is not null");
            return (Criteria) this;
        }

        public Criteria andExceptionIdEqualTo(Long value) {
            addCriterion("exception_id =", value, "exceptionId");
            return (Criteria) this;
        }

        public Criteria andExceptionIdNotEqualTo(Long value) {
            addCriterion("exception_id <>", value, "exceptionId");
            return (Criteria) this;
        }

        public Criteria andExceptionIdGreaterThan(Long value) {
            addCriterion("exception_id >", value, "exceptionId");
            return (Criteria) this;
        }

        public Criteria andExceptionIdGreaterThanOrEqualTo(Long value) {
            addCriterion("exception_id >=", value, "exceptionId");
            return (Criteria) this;
        }

        public Criteria andExceptionIdLessThan(Long value) {
            addCriterion("exception_id <", value, "exceptionId");
            return (Criteria) this;
        }

        public Criteria andExceptionIdLessThanOrEqualTo(Long value) {
            addCriterion("exception_id <=", value, "exceptionId");
            return (Criteria) this;
        }

        public Criteria andExceptionIdIn(List<Long> values) {
            addCriterion("exception_id in", values, "exceptionId");
            return (Criteria) this;
        }

        public Criteria andExceptionIdNotIn(List<Long> values) {
            addCriterion("exception_id not in", values, "exceptionId");
            return (Criteria) this;
        }

        public Criteria andExceptionIdBetween(Long value1, Long value2) {
            addCriterion("exception_id between", value1, value2, "exceptionId");
            return (Criteria) this;
        }

        public Criteria andExceptionIdNotBetween(Long value1, Long value2) {
            addCriterion("exception_id not between", value1, value2, "exceptionId");
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

        public Criteria andStaffIdIsNull() {
            addCriterion("staff_id is null");
            return (Criteria) this;
        }

        public Criteria andStaffIdIsNotNull() {
            addCriterion("staff_id is not null");
            return (Criteria) this;
        }

        public Criteria andStaffIdEqualTo(Long value) {
            addCriterion("staff_id =", value, "staffId");
            return (Criteria) this;
        }

        public Criteria andStaffIdNotEqualTo(Long value) {
            addCriterion("staff_id <>", value, "staffId");
            return (Criteria) this;
        }

        public Criteria andStaffIdGreaterThan(Long value) {
            addCriterion("staff_id >", value, "staffId");
            return (Criteria) this;
        }

        public Criteria andStaffIdGreaterThanOrEqualTo(Long value) {
            addCriterion("staff_id >=", value, "staffId");
            return (Criteria) this;
        }

        public Criteria andStaffIdLessThan(Long value) {
            addCriterion("staff_id <", value, "staffId");
            return (Criteria) this;
        }

        public Criteria andStaffIdLessThanOrEqualTo(Long value) {
            addCriterion("staff_id <=", value, "staffId");
            return (Criteria) this;
        }

        public Criteria andStaffIdIn(List<Long> values) {
            addCriterion("staff_id in", values, "staffId");
            return (Criteria) this;
        }

        public Criteria andStaffIdNotIn(List<Long> values) {
            addCriterion("staff_id not in", values, "staffId");
            return (Criteria) this;
        }

        public Criteria andStaffIdBetween(Long value1, Long value2) {
            addCriterion("staff_id between", value1, value2, "staffId");
            return (Criteria) this;
        }

        public Criteria andStaffIdNotBetween(Long value1, Long value2) {
            addCriterion("staff_id not between", value1, value2, "staffId");
            return (Criteria) this;
        }

        public Criteria andExceptionDateIsNull() {
            addCriterion("exception_date is null");
            return (Criteria) this;
        }

        public Criteria andExceptionDateIsNotNull() {
            addCriterion("exception_date is not null");
            return (Criteria) this;
        }

        public Criteria andExceptionDateEqualTo(Date value) {
            addCriterionForJDBCDate("exception_date =", value, "exceptionDate");
            return (Criteria) this;
        }

        public Criteria andExceptionDateNotEqualTo(Date value) {
            addCriterionForJDBCDate("exception_date <>", value, "exceptionDate");
            return (Criteria) this;
        }

        public Criteria andExceptionDateGreaterThan(Date value) {
            addCriterionForJDBCDate("exception_date >", value, "exceptionDate");
            return (Criteria) this;
        }

        public Criteria andExceptionDateGreaterThanOrEqualTo(Date value) {
            addCriterionForJDBCDate("exception_date >=", value, "exceptionDate");
            return (Criteria) this;
        }

        public Criteria andExceptionDateLessThan(Date value) {
            addCriterionForJDBCDate("exception_date <", value, "exceptionDate");
            return (Criteria) this;
        }

        public Criteria andExceptionDateLessThanOrEqualTo(Date value) {
            addCriterionForJDBCDate("exception_date <=", value, "exceptionDate");
            return (Criteria) this;
        }

        public Criteria andExceptionDateIn(List<Date> values) {
            addCriterionForJDBCDate("exception_date in", values, "exceptionDate");
            return (Criteria) this;
        }

        public Criteria andExceptionDateNotIn(List<Date> values) {
            addCriterionForJDBCDate("exception_date not in", values, "exceptionDate");
            return (Criteria) this;
        }

        public Criteria andExceptionDateBetween(Date value1, Date value2) {
            addCriterionForJDBCDate("exception_date between", value1, value2, "exceptionDate");
            return (Criteria) this;
        }

        public Criteria andExceptionDateNotBetween(Date value1, Date value2) {
            addCriterionForJDBCDate("exception_date not between", value1, value2, "exceptionDate");
            return (Criteria) this;
        }

        public Criteria andTypeIsNull() {
            addCriterion("type is null");
            return (Criteria) this;
        }

        public Criteria andTypeIsNotNull() {
            addCriterion("type is not null");
            return (Criteria) this;
        }

        public Criteria andTypeEqualTo(String value) {
            addCriterion("type =", value, "type");
            return (Criteria) this;
        }

        public Criteria andTypeNotEqualTo(String value) {
            addCriterion("type <>", value, "type");
            return (Criteria) this;
        }

        public Criteria andTypeGreaterThan(String value) {
            addCriterion("type >", value, "type");
            return (Criteria) this;
        }

        public Criteria andTypeGreaterThanOrEqualTo(String value) {
            addCriterion("type >=", value, "type");
            return (Criteria) this;
        }

        public Criteria andTypeLessThan(String value) {
            addCriterion("type <", value, "type");
            return (Criteria) this;
        }

        public Criteria andTypeLessThanOrEqualTo(String value) {
            addCriterion("type <=", value, "type");
            return (Criteria) this;
        }

        public Criteria andTypeLike(String value) {
            addCriterion("type like", value, "type");
            return (Criteria) this;
        }

        public Criteria andTypeNotLike(String value) {
            addCriterion("type not like", value, "type");
            return (Criteria) this;
        }

        public Criteria andTypeIn(List<String> values) {
            addCriterion("type in", values, "type");
            return (Criteria) this;
        }

        public Criteria andTypeNotIn(List<String> values) {
            addCriterion("type not in", values, "type");
            return (Criteria) this;
        }

        public Criteria andTypeBetween(String value1, String value2) {
            addCriterion("type between", value1, value2, "type");
            return (Criteria) this;
        }

        public Criteria andTypeNotBetween(String value1, String value2) {
            addCriterion("type not between", value1, value2, "type");
            return (Criteria) this;
        }

        public Criteria andOriginalScheduleIdIsNull() {
            addCriterion("original_schedule_id is null");
            return (Criteria) this;
        }

        public Criteria andOriginalScheduleIdIsNotNull() {
            addCriterion("original_schedule_id is not null");
            return (Criteria) this;
        }

        public Criteria andOriginalScheduleIdEqualTo(Long value) {
            addCriterion("original_schedule_id =", value, "originalScheduleId");
            return (Criteria) this;
        }

        public Criteria andOriginalScheduleIdNotEqualTo(Long value) {
            addCriterion("original_schedule_id <>", value, "originalScheduleId");
            return (Criteria) this;
        }

        public Criteria andOriginalScheduleIdGreaterThan(Long value) {
            addCriterion("original_schedule_id >", value, "originalScheduleId");
            return (Criteria) this;
        }

        public Criteria andOriginalScheduleIdGreaterThanOrEqualTo(Long value) {
            addCriterion("original_schedule_id >=", value, "originalScheduleId");
            return (Criteria) this;
        }

        public Criteria andOriginalScheduleIdLessThan(Long value) {
            addCriterion("original_schedule_id <", value, "originalScheduleId");
            return (Criteria) this;
        }

        public Criteria andOriginalScheduleIdLessThanOrEqualTo(Long value) {
            addCriterion("original_schedule_id <=", value, "originalScheduleId");
            return (Criteria) this;
        }

        public Criteria andOriginalScheduleIdIn(List<Long> values) {
            addCriterion("original_schedule_id in", values, "originalScheduleId");
            return (Criteria) this;
        }

        public Criteria andOriginalScheduleIdNotIn(List<Long> values) {
            addCriterion("original_schedule_id not in", values, "originalScheduleId");
            return (Criteria) this;
        }

        public Criteria andOriginalScheduleIdBetween(Long value1, Long value2) {
            addCriterion("original_schedule_id between", value1, value2, "originalScheduleId");
            return (Criteria) this;
        }

        public Criteria andOriginalScheduleIdNotBetween(Long value1, Long value2) {
            addCriterion("original_schedule_id not between", value1, value2, "originalScheduleId");
            return (Criteria) this;
        }

        public Criteria andNewScheduleIdIsNull() {
            addCriterion("new_schedule_id is null");
            return (Criteria) this;
        }

        public Criteria andNewScheduleIdIsNotNull() {
            addCriterion("new_schedule_id is not null");
            return (Criteria) this;
        }

        public Criteria andNewScheduleIdEqualTo(Long value) {
            addCriterion("new_schedule_id =", value, "newScheduleId");
            return (Criteria) this;
        }

        public Criteria andNewScheduleIdNotEqualTo(Long value) {
            addCriterion("new_schedule_id <>", value, "newScheduleId");
            return (Criteria) this;
        }

        public Criteria andNewScheduleIdGreaterThan(Long value) {
            addCriterion("new_schedule_id >", value, "newScheduleId");
            return (Criteria) this;
        }

        public Criteria andNewScheduleIdGreaterThanOrEqualTo(Long value) {
            addCriterion("new_schedule_id >=", value, "newScheduleId");
            return (Criteria) this;
        }

        public Criteria andNewScheduleIdLessThan(Long value) {
            addCriterion("new_schedule_id <", value, "newScheduleId");
            return (Criteria) this;
        }

        public Criteria andNewScheduleIdLessThanOrEqualTo(Long value) {
            addCriterion("new_schedule_id <=", value, "newScheduleId");
            return (Criteria) this;
        }

        public Criteria andNewScheduleIdIn(List<Long> values) {
            addCriterion("new_schedule_id in", values, "newScheduleId");
            return (Criteria) this;
        }

        public Criteria andNewScheduleIdNotIn(List<Long> values) {
            addCriterion("new_schedule_id not in", values, "newScheduleId");
            return (Criteria) this;
        }

        public Criteria andNewScheduleIdBetween(Long value1, Long value2) {
            addCriterion("new_schedule_id between", value1, value2, "newScheduleId");
            return (Criteria) this;
        }

        public Criteria andNewScheduleIdNotBetween(Long value1, Long value2) {
            addCriterion("new_schedule_id not between", value1, value2, "newScheduleId");
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

        public Criteria andApproverIdIsNull() {
            addCriterion("approver_id is null");
            return (Criteria) this;
        }

        public Criteria andApproverIdIsNotNull() {
            addCriterion("approver_id is not null");
            return (Criteria) this;
        }

        public Criteria andApproverIdEqualTo(Long value) {
            addCriterion("approver_id =", value, "approverId");
            return (Criteria) this;
        }

        public Criteria andApproverIdNotEqualTo(Long value) {
            addCriterion("approver_id <>", value, "approverId");
            return (Criteria) this;
        }

        public Criteria andApproverIdGreaterThan(Long value) {
            addCriterion("approver_id >", value, "approverId");
            return (Criteria) this;
        }

        public Criteria andApproverIdGreaterThanOrEqualTo(Long value) {
            addCriterion("approver_id >=", value, "approverId");
            return (Criteria) this;
        }

        public Criteria andApproverIdLessThan(Long value) {
            addCriterion("approver_id <", value, "approverId");
            return (Criteria) this;
        }

        public Criteria andApproverIdLessThanOrEqualTo(Long value) {
            addCriterion("approver_id <=", value, "approverId");
            return (Criteria) this;
        }

        public Criteria andApproverIdIn(List<Long> values) {
            addCriterion("approver_id in", values, "approverId");
            return (Criteria) this;
        }

        public Criteria andApproverIdNotIn(List<Long> values) {
            addCriterion("approver_id not in", values, "approverId");
            return (Criteria) this;
        }

        public Criteria andApproverIdBetween(Long value1, Long value2) {
            addCriterion("approver_id between", value1, value2, "approverId");
            return (Criteria) this;
        }

        public Criteria andApproverIdNotBetween(Long value1, Long value2) {
            addCriterion("approver_id not between", value1, value2, "approverId");
            return (Criteria) this;
        }

        public Criteria andReasonIsNull() {
            addCriterion("reason is null");
            return (Criteria) this;
        }

        public Criteria andReasonIsNotNull() {
            addCriterion("reason is not null");
            return (Criteria) this;
        }

        public Criteria andReasonEqualTo(String value) {
            addCriterion("reason =", value, "reason");
            return (Criteria) this;
        }

        public Criteria andReasonNotEqualTo(String value) {
            addCriterion("reason <>", value, "reason");
            return (Criteria) this;
        }

        public Criteria andReasonGreaterThan(String value) {
            addCriterion("reason >", value, "reason");
            return (Criteria) this;
        }

        public Criteria andReasonGreaterThanOrEqualTo(String value) {
            addCriterion("reason >=", value, "reason");
            return (Criteria) this;
        }

        public Criteria andReasonLessThan(String value) {
            addCriterion("reason <", value, "reason");
            return (Criteria) this;
        }

        public Criteria andReasonLessThanOrEqualTo(String value) {
            addCriterion("reason <=", value, "reason");
            return (Criteria) this;
        }

        public Criteria andReasonLike(String value) {
            addCriterion("reason like", value, "reason");
            return (Criteria) this;
        }

        public Criteria andReasonNotLike(String value) {
            addCriterion("reason not like", value, "reason");
            return (Criteria) this;
        }

        public Criteria andReasonIn(List<String> values) {
            addCriterion("reason in", values, "reason");
            return (Criteria) this;
        }

        public Criteria andReasonNotIn(List<String> values) {
            addCriterion("reason not in", values, "reason");
            return (Criteria) this;
        }

        public Criteria andReasonBetween(String value1, String value2) {
            addCriterion("reason between", value1, value2, "reason");
            return (Criteria) this;
        }

        public Criteria andReasonNotBetween(String value1, String value2) {
            addCriterion("reason not between", value1, value2, "reason");
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