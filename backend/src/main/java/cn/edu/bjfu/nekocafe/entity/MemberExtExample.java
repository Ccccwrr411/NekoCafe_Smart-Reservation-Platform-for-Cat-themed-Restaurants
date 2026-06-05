package cn.edu.bjfu.nekocafe.entity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MemberExtExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public MemberExtExample() {
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

        public Criteria andLevelIsNull() {
            addCriterion("level is null");
            return (Criteria) this;
        }

        public Criteria andLevelIsNotNull() {
            addCriterion("level is not null");
            return (Criteria) this;
        }

        public Criteria andLevelEqualTo(Integer value) {
            addCriterion("level =", value, "level");
            return (Criteria) this;
        }

        public Criteria andLevelNotEqualTo(Integer value) {
            addCriterion("level <>", value, "level");
            return (Criteria) this;
        }

        public Criteria andLevelGreaterThan(Integer value) {
            addCriterion("level >", value, "level");
            return (Criteria) this;
        }

        public Criteria andLevelGreaterThanOrEqualTo(Integer value) {
            addCriterion("level >=", value, "level");
            return (Criteria) this;
        }

        public Criteria andLevelLessThan(Integer value) {
            addCriterion("level <", value, "level");
            return (Criteria) this;
        }

        public Criteria andLevelLessThanOrEqualTo(Integer value) {
            addCriterion("level <=", value, "level");
            return (Criteria) this;
        }

        public Criteria andLevelIn(List<Integer> values) {
            addCriterion("level in", values, "level");
            return (Criteria) this;
        }

        public Criteria andLevelNotIn(List<Integer> values) {
            addCriterion("level not in", values, "level");
            return (Criteria) this;
        }

        public Criteria andLevelBetween(Integer value1, Integer value2) {
            addCriterion("level between", value1, value2, "level");
            return (Criteria) this;
        }

        public Criteria andLevelNotBetween(Integer value1, Integer value2) {
            addCriterion("level not between", value1, value2, "level");
            return (Criteria) this;
        }

        public Criteria andTotalPointsIsNull() {
            addCriterion("total_points is null");
            return (Criteria) this;
        }

        public Criteria andTotalPointsIsNotNull() {
            addCriterion("total_points is not null");
            return (Criteria) this;
        }

        public Criteria andTotalPointsEqualTo(Integer value) {
            addCriterion("total_points =", value, "totalPoints");
            return (Criteria) this;
        }

        public Criteria andTotalPointsNotEqualTo(Integer value) {
            addCriterion("total_points <>", value, "totalPoints");
            return (Criteria) this;
        }

        public Criteria andTotalPointsGreaterThan(Integer value) {
            addCriterion("total_points >", value, "totalPoints");
            return (Criteria) this;
        }

        public Criteria andTotalPointsGreaterThanOrEqualTo(Integer value) {
            addCriterion("total_points >=", value, "totalPoints");
            return (Criteria) this;
        }

        public Criteria andTotalPointsLessThan(Integer value) {
            addCriterion("total_points <", value, "totalPoints");
            return (Criteria) this;
        }

        public Criteria andTotalPointsLessThanOrEqualTo(Integer value) {
            addCriterion("total_points <=", value, "totalPoints");
            return (Criteria) this;
        }

        public Criteria andTotalPointsIn(List<Integer> values) {
            addCriterion("total_points in", values, "totalPoints");
            return (Criteria) this;
        }

        public Criteria andTotalPointsNotIn(List<Integer> values) {
            addCriterion("total_points not in", values, "totalPoints");
            return (Criteria) this;
        }

        public Criteria andTotalPointsBetween(Integer value1, Integer value2) {
            addCriterion("total_points between", value1, value2, "totalPoints");
            return (Criteria) this;
        }

        public Criteria andTotalPointsNotBetween(Integer value1, Integer value2) {
            addCriterion("total_points not between", value1, value2, "totalPoints");
            return (Criteria) this;
        }

        public Criteria andCumulativeAmountIsNull() {
            addCriterion("cumulative_amount is null");
            return (Criteria) this;
        }

        public Criteria andCumulativeAmountIsNotNull() {
            addCriterion("cumulative_amount is not null");
            return (Criteria) this;
        }

        public Criteria andCumulativeAmountEqualTo(BigDecimal value) {
            addCriterion("cumulative_amount =", value, "cumulativeAmount");
            return (Criteria) this;
        }

        public Criteria andCumulativeAmountNotEqualTo(BigDecimal value) {
            addCriterion("cumulative_amount <>", value, "cumulativeAmount");
            return (Criteria) this;
        }

        public Criteria andCumulativeAmountGreaterThan(BigDecimal value) {
            addCriterion("cumulative_amount >", value, "cumulativeAmount");
            return (Criteria) this;
        }

        public Criteria andCumulativeAmountGreaterThanOrEqualTo(BigDecimal value) {
            addCriterion("cumulative_amount >=", value, "cumulativeAmount");
            return (Criteria) this;
        }

        public Criteria andCumulativeAmountLessThan(BigDecimal value) {
            addCriterion("cumulative_amount <", value, "cumulativeAmount");
            return (Criteria) this;
        }

        public Criteria andCumulativeAmountLessThanOrEqualTo(BigDecimal value) {
            addCriterion("cumulative_amount <=", value, "cumulativeAmount");
            return (Criteria) this;
        }

        public Criteria andCumulativeAmountIn(List<BigDecimal> values) {
            addCriterion("cumulative_amount in", values, "cumulativeAmount");
            return (Criteria) this;
        }

        public Criteria andCumulativeAmountNotIn(List<BigDecimal> values) {
            addCriterion("cumulative_amount not in", values, "cumulativeAmount");
            return (Criteria) this;
        }

        public Criteria andCumulativeAmountBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("cumulative_amount between", value1, value2, "cumulativeAmount");
            return (Criteria) this;
        }

        public Criteria andCumulativeAmountNotBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("cumulative_amount not between", value1, value2, "cumulativeAmount");
            return (Criteria) this;
        }

        public Criteria andLastVisitTimeIsNull() {
            addCriterion("last_visit_time is null");
            return (Criteria) this;
        }

        public Criteria andLastVisitTimeIsNotNull() {
            addCriterion("last_visit_time is not null");
            return (Criteria) this;
        }

        public Criteria andLastVisitTimeEqualTo(Date value) {
            addCriterion("last_visit_time =", value, "lastVisitTime");
            return (Criteria) this;
        }

        public Criteria andLastVisitTimeNotEqualTo(Date value) {
            addCriterion("last_visit_time <>", value, "lastVisitTime");
            return (Criteria) this;
        }

        public Criteria andLastVisitTimeGreaterThan(Date value) {
            addCriterion("last_visit_time >", value, "lastVisitTime");
            return (Criteria) this;
        }

        public Criteria andLastVisitTimeGreaterThanOrEqualTo(Date value) {
            addCriterion("last_visit_time >=", value, "lastVisitTime");
            return (Criteria) this;
        }

        public Criteria andLastVisitTimeLessThan(Date value) {
            addCriterion("last_visit_time <", value, "lastVisitTime");
            return (Criteria) this;
        }

        public Criteria andLastVisitTimeLessThanOrEqualTo(Date value) {
            addCriterion("last_visit_time <=", value, "lastVisitTime");
            return (Criteria) this;
        }

        public Criteria andLastVisitTimeIn(List<Date> values) {
            addCriterion("last_visit_time in", values, "lastVisitTime");
            return (Criteria) this;
        }

        public Criteria andLastVisitTimeNotIn(List<Date> values) {
            addCriterion("last_visit_time not in", values, "lastVisitTime");
            return (Criteria) this;
        }

        public Criteria andLastVisitTimeBetween(Date value1, Date value2) {
            addCriterion("last_visit_time between", value1, value2, "lastVisitTime");
            return (Criteria) this;
        }

        public Criteria andLastVisitTimeNotBetween(Date value1, Date value2) {
            addCriterion("last_visit_time not between", value1, value2, "lastVisitTime");
            return (Criteria) this;
        }

        public Criteria andPreferencesIsNull() {
            addCriterion("preferences is null");
            return (Criteria) this;
        }

        public Criteria andPreferencesIsNotNull() {
            addCriterion("preferences is not null");
            return (Criteria) this;
        }

        public Criteria andPreferencesEqualTo(String value) {
            addCriterion("preferences =", value, "preferences");
            return (Criteria) this;
        }

        public Criteria andPreferencesNotEqualTo(String value) {
            addCriterion("preferences <>", value, "preferences");
            return (Criteria) this;
        }

        public Criteria andPreferencesGreaterThan(String value) {
            addCriterion("preferences >", value, "preferences");
            return (Criteria) this;
        }

        public Criteria andPreferencesGreaterThanOrEqualTo(String value) {
            addCriterion("preferences >=", value, "preferences");
            return (Criteria) this;
        }

        public Criteria andPreferencesLessThan(String value) {
            addCriterion("preferences <", value, "preferences");
            return (Criteria) this;
        }

        public Criteria andPreferencesLessThanOrEqualTo(String value) {
            addCriterion("preferences <=", value, "preferences");
            return (Criteria) this;
        }

        public Criteria andPreferencesLike(String value) {
            addCriterion("preferences like", value, "preferences");
            return (Criteria) this;
        }

        public Criteria andPreferencesNotLike(String value) {
            addCriterion("preferences not like", value, "preferences");
            return (Criteria) this;
        }

        public Criteria andPreferencesIn(List<String> values) {
            addCriterion("preferences in", values, "preferences");
            return (Criteria) this;
        }

        public Criteria andPreferencesNotIn(List<String> values) {
            addCriterion("preferences not in", values, "preferences");
            return (Criteria) this;
        }

        public Criteria andPreferencesBetween(String value1, String value2) {
            addCriterion("preferences between", value1, value2, "preferences");
            return (Criteria) this;
        }

        public Criteria andPreferencesNotBetween(String value1, String value2) {
            addCriterion("preferences not between", value1, value2, "preferences");
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