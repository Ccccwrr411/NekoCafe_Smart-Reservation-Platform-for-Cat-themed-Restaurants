package cn.edu.bjfu.nekocafe.entity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class StoreDailyStatsExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public StoreDailyStatsExample() {
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

        public Criteria andStatDateIsNull() {
            addCriterion("stat_date is null");
            return (Criteria) this;
        }

        public Criteria andStatDateIsNotNull() {
            addCriterion("stat_date is not null");
            return (Criteria) this;
        }

        public Criteria andStatDateEqualTo(Date value) {
            addCriterionForJDBCDate("stat_date =", value, "statDate");
            return (Criteria) this;
        }

        public Criteria andStatDateNotEqualTo(Date value) {
            addCriterionForJDBCDate("stat_date <>", value, "statDate");
            return (Criteria) this;
        }

        public Criteria andStatDateGreaterThan(Date value) {
            addCriterionForJDBCDate("stat_date >", value, "statDate");
            return (Criteria) this;
        }

        public Criteria andStatDateGreaterThanOrEqualTo(Date value) {
            addCriterionForJDBCDate("stat_date >=", value, "statDate");
            return (Criteria) this;
        }

        public Criteria andStatDateLessThan(Date value) {
            addCriterionForJDBCDate("stat_date <", value, "statDate");
            return (Criteria) this;
        }

        public Criteria andStatDateLessThanOrEqualTo(Date value) {
            addCriterionForJDBCDate("stat_date <=", value, "statDate");
            return (Criteria) this;
        }

        public Criteria andStatDateIn(List<Date> values) {
            addCriterionForJDBCDate("stat_date in", values, "statDate");
            return (Criteria) this;
        }

        public Criteria andStatDateNotIn(List<Date> values) {
            addCriterionForJDBCDate("stat_date not in", values, "statDate");
            return (Criteria) this;
        }

        public Criteria andStatDateBetween(Date value1, Date value2) {
            addCriterionForJDBCDate("stat_date between", value1, value2, "statDate");
            return (Criteria) this;
        }

        public Criteria andStatDateNotBetween(Date value1, Date value2) {
            addCriterionForJDBCDate("stat_date not between", value1, value2, "statDate");
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

        public Criteria andTotalReservationsIsNull() {
            addCriterion("total_reservations is null");
            return (Criteria) this;
        }

        public Criteria andTotalReservationsIsNotNull() {
            addCriterion("total_reservations is not null");
            return (Criteria) this;
        }

        public Criteria andTotalReservationsEqualTo(Integer value) {
            addCriterion("total_reservations =", value, "totalReservations");
            return (Criteria) this;
        }

        public Criteria andTotalReservationsNotEqualTo(Integer value) {
            addCriterion("total_reservations <>", value, "totalReservations");
            return (Criteria) this;
        }

        public Criteria andTotalReservationsGreaterThan(Integer value) {
            addCriterion("total_reservations >", value, "totalReservations");
            return (Criteria) this;
        }

        public Criteria andTotalReservationsGreaterThanOrEqualTo(Integer value) {
            addCriterion("total_reservations >=", value, "totalReservations");
            return (Criteria) this;
        }

        public Criteria andTotalReservationsLessThan(Integer value) {
            addCriterion("total_reservations <", value, "totalReservations");
            return (Criteria) this;
        }

        public Criteria andTotalReservationsLessThanOrEqualTo(Integer value) {
            addCriterion("total_reservations <=", value, "totalReservations");
            return (Criteria) this;
        }

        public Criteria andTotalReservationsIn(List<Integer> values) {
            addCriterion("total_reservations in", values, "totalReservations");
            return (Criteria) this;
        }

        public Criteria andTotalReservationsNotIn(List<Integer> values) {
            addCriterion("total_reservations not in", values, "totalReservations");
            return (Criteria) this;
        }

        public Criteria andTotalReservationsBetween(Integer value1, Integer value2) {
            addCriterion("total_reservations between", value1, value2, "totalReservations");
            return (Criteria) this;
        }

        public Criteria andTotalReservationsNotBetween(Integer value1, Integer value2) {
            addCriterion("total_reservations not between", value1, value2, "totalReservations");
            return (Criteria) this;
        }

        public Criteria andTotalRevenueIsNull() {
            addCriterion("total_revenue is null");
            return (Criteria) this;
        }

        public Criteria andTotalRevenueIsNotNull() {
            addCriterion("total_revenue is not null");
            return (Criteria) this;
        }

        public Criteria andTotalRevenueEqualTo(BigDecimal value) {
            addCriterion("total_revenue =", value, "totalRevenue");
            return (Criteria) this;
        }

        public Criteria andTotalRevenueNotEqualTo(BigDecimal value) {
            addCriterion("total_revenue <>", value, "totalRevenue");
            return (Criteria) this;
        }

        public Criteria andTotalRevenueGreaterThan(BigDecimal value) {
            addCriterion("total_revenue >", value, "totalRevenue");
            return (Criteria) this;
        }

        public Criteria andTotalRevenueGreaterThanOrEqualTo(BigDecimal value) {
            addCriterion("total_revenue >=", value, "totalRevenue");
            return (Criteria) this;
        }

        public Criteria andTotalRevenueLessThan(BigDecimal value) {
            addCriterion("total_revenue <", value, "totalRevenue");
            return (Criteria) this;
        }

        public Criteria andTotalRevenueLessThanOrEqualTo(BigDecimal value) {
            addCriterion("total_revenue <=", value, "totalRevenue");
            return (Criteria) this;
        }

        public Criteria andTotalRevenueIn(List<BigDecimal> values) {
            addCriterion("total_revenue in", values, "totalRevenue");
            return (Criteria) this;
        }

        public Criteria andTotalRevenueNotIn(List<BigDecimal> values) {
            addCriterion("total_revenue not in", values, "totalRevenue");
            return (Criteria) this;
        }

        public Criteria andTotalRevenueBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("total_revenue between", value1, value2, "totalRevenue");
            return (Criteria) this;
        }

        public Criteria andTotalRevenueNotBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("total_revenue not between", value1, value2, "totalRevenue");
            return (Criteria) this;
        }

        public Criteria andTableTurnoverRateIsNull() {
            addCriterion("table_turnover_rate is null");
            return (Criteria) this;
        }

        public Criteria andTableTurnoverRateIsNotNull() {
            addCriterion("table_turnover_rate is not null");
            return (Criteria) this;
        }

        public Criteria andTableTurnoverRateEqualTo(BigDecimal value) {
            addCriterion("table_turnover_rate =", value, "tableTurnoverRate");
            return (Criteria) this;
        }

        public Criteria andTableTurnoverRateNotEqualTo(BigDecimal value) {
            addCriterion("table_turnover_rate <>", value, "tableTurnoverRate");
            return (Criteria) this;
        }

        public Criteria andTableTurnoverRateGreaterThan(BigDecimal value) {
            addCriterion("table_turnover_rate >", value, "tableTurnoverRate");
            return (Criteria) this;
        }

        public Criteria andTableTurnoverRateGreaterThanOrEqualTo(BigDecimal value) {
            addCriterion("table_turnover_rate >=", value, "tableTurnoverRate");
            return (Criteria) this;
        }

        public Criteria andTableTurnoverRateLessThan(BigDecimal value) {
            addCriterion("table_turnover_rate <", value, "tableTurnoverRate");
            return (Criteria) this;
        }

        public Criteria andTableTurnoverRateLessThanOrEqualTo(BigDecimal value) {
            addCriterion("table_turnover_rate <=", value, "tableTurnoverRate");
            return (Criteria) this;
        }

        public Criteria andTableTurnoverRateIn(List<BigDecimal> values) {
            addCriterion("table_turnover_rate in", values, "tableTurnoverRate");
            return (Criteria) this;
        }

        public Criteria andTableTurnoverRateNotIn(List<BigDecimal> values) {
            addCriterion("table_turnover_rate not in", values, "tableTurnoverRate");
            return (Criteria) this;
        }

        public Criteria andTableTurnoverRateBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("table_turnover_rate between", value1, value2, "tableTurnoverRate");
            return (Criteria) this;
        }

        public Criteria andTableTurnoverRateNotBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("table_turnover_rate not between", value1, value2, "tableTurnoverRate");
            return (Criteria) this;
        }

        public Criteria andRevenuePerSeatIsNull() {
            addCriterion("revenue_per_seat is null");
            return (Criteria) this;
        }

        public Criteria andRevenuePerSeatIsNotNull() {
            addCriterion("revenue_per_seat is not null");
            return (Criteria) this;
        }

        public Criteria andRevenuePerSeatEqualTo(BigDecimal value) {
            addCriterion("revenue_per_seat =", value, "revenuePerSeat");
            return (Criteria) this;
        }

        public Criteria andRevenuePerSeatNotEqualTo(BigDecimal value) {
            addCriterion("revenue_per_seat <>", value, "revenuePerSeat");
            return (Criteria) this;
        }

        public Criteria andRevenuePerSeatGreaterThan(BigDecimal value) {
            addCriterion("revenue_per_seat >", value, "revenuePerSeat");
            return (Criteria) this;
        }

        public Criteria andRevenuePerSeatGreaterThanOrEqualTo(BigDecimal value) {
            addCriterion("revenue_per_seat >=", value, "revenuePerSeat");
            return (Criteria) this;
        }

        public Criteria andRevenuePerSeatLessThan(BigDecimal value) {
            addCriterion("revenue_per_seat <", value, "revenuePerSeat");
            return (Criteria) this;
        }

        public Criteria andRevenuePerSeatLessThanOrEqualTo(BigDecimal value) {
            addCriterion("revenue_per_seat <=", value, "revenuePerSeat");
            return (Criteria) this;
        }

        public Criteria andRevenuePerSeatIn(List<BigDecimal> values) {
            addCriterion("revenue_per_seat in", values, "revenuePerSeat");
            return (Criteria) this;
        }

        public Criteria andRevenuePerSeatNotIn(List<BigDecimal> values) {
            addCriterion("revenue_per_seat not in", values, "revenuePerSeat");
            return (Criteria) this;
        }

        public Criteria andRevenuePerSeatBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("revenue_per_seat between", value1, value2, "revenuePerSeat");
            return (Criteria) this;
        }

        public Criteria andRevenuePerSeatNotBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("revenue_per_seat not between", value1, value2, "revenuePerSeat");
            return (Criteria) this;
        }

        public Criteria andRepeatCustomersIsNull() {
            addCriterion("repeat_customers is null");
            return (Criteria) this;
        }

        public Criteria andRepeatCustomersIsNotNull() {
            addCriterion("repeat_customers is not null");
            return (Criteria) this;
        }

        public Criteria andRepeatCustomersEqualTo(Integer value) {
            addCriterion("repeat_customers =", value, "repeatCustomers");
            return (Criteria) this;
        }

        public Criteria andRepeatCustomersNotEqualTo(Integer value) {
            addCriterion("repeat_customers <>", value, "repeatCustomers");
            return (Criteria) this;
        }

        public Criteria andRepeatCustomersGreaterThan(Integer value) {
            addCriterion("repeat_customers >", value, "repeatCustomers");
            return (Criteria) this;
        }

        public Criteria andRepeatCustomersGreaterThanOrEqualTo(Integer value) {
            addCriterion("repeat_customers >=", value, "repeatCustomers");
            return (Criteria) this;
        }

        public Criteria andRepeatCustomersLessThan(Integer value) {
            addCriterion("repeat_customers <", value, "repeatCustomers");
            return (Criteria) this;
        }

        public Criteria andRepeatCustomersLessThanOrEqualTo(Integer value) {
            addCriterion("repeat_customers <=", value, "repeatCustomers");
            return (Criteria) this;
        }

        public Criteria andRepeatCustomersIn(List<Integer> values) {
            addCriterion("repeat_customers in", values, "repeatCustomers");
            return (Criteria) this;
        }

        public Criteria andRepeatCustomersNotIn(List<Integer> values) {
            addCriterion("repeat_customers not in", values, "repeatCustomers");
            return (Criteria) this;
        }

        public Criteria andRepeatCustomersBetween(Integer value1, Integer value2) {
            addCriterion("repeat_customers between", value1, value2, "repeatCustomers");
            return (Criteria) this;
        }

        public Criteria andRepeatCustomersNotBetween(Integer value1, Integer value2) {
            addCriterion("repeat_customers not between", value1, value2, "repeatCustomers");
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