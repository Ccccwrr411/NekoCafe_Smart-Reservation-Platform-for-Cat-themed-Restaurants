package cn.edu.bjfu.nekocafe.entity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CouponUsageExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public CouponUsageExample() {
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

        public Criteria andUsageIdIsNull() {
            addCriterion("usage_id is null");
            return (Criteria) this;
        }

        public Criteria andUsageIdIsNotNull() {
            addCriterion("usage_id is not null");
            return (Criteria) this;
        }

        public Criteria andUsageIdEqualTo(Long value) {
            addCriterion("usage_id =", value, "usageId");
            return (Criteria) this;
        }

        public Criteria andUsageIdNotEqualTo(Long value) {
            addCriterion("usage_id <>", value, "usageId");
            return (Criteria) this;
        }

        public Criteria andUsageIdGreaterThan(Long value) {
            addCriterion("usage_id >", value, "usageId");
            return (Criteria) this;
        }

        public Criteria andUsageIdGreaterThanOrEqualTo(Long value) {
            addCriterion("usage_id >=", value, "usageId");
            return (Criteria) this;
        }

        public Criteria andUsageIdLessThan(Long value) {
            addCriterion("usage_id <", value, "usageId");
            return (Criteria) this;
        }

        public Criteria andUsageIdLessThanOrEqualTo(Long value) {
            addCriterion("usage_id <=", value, "usageId");
            return (Criteria) this;
        }

        public Criteria andUsageIdIn(List<Long> values) {
            addCriterion("usage_id in", values, "usageId");
            return (Criteria) this;
        }

        public Criteria andUsageIdNotIn(List<Long> values) {
            addCriterion("usage_id not in", values, "usageId");
            return (Criteria) this;
        }

        public Criteria andUsageIdBetween(Long value1, Long value2) {
            addCriterion("usage_id between", value1, value2, "usageId");
            return (Criteria) this;
        }

        public Criteria andUsageIdNotBetween(Long value1, Long value2) {
            addCriterion("usage_id not between", value1, value2, "usageId");
            return (Criteria) this;
        }

        public Criteria andCouponIdIsNull() {
            addCriterion("coupon_id is null");
            return (Criteria) this;
        }

        public Criteria andCouponIdIsNotNull() {
            addCriterion("coupon_id is not null");
            return (Criteria) this;
        }

        public Criteria andCouponIdEqualTo(Long value) {
            addCriterion("coupon_id =", value, "couponId");
            return (Criteria) this;
        }

        public Criteria andCouponIdNotEqualTo(Long value) {
            addCriterion("coupon_id <>", value, "couponId");
            return (Criteria) this;
        }

        public Criteria andCouponIdGreaterThan(Long value) {
            addCriterion("coupon_id >", value, "couponId");
            return (Criteria) this;
        }

        public Criteria andCouponIdGreaterThanOrEqualTo(Long value) {
            addCriterion("coupon_id >=", value, "couponId");
            return (Criteria) this;
        }

        public Criteria andCouponIdLessThan(Long value) {
            addCriterion("coupon_id <", value, "couponId");
            return (Criteria) this;
        }

        public Criteria andCouponIdLessThanOrEqualTo(Long value) {
            addCriterion("coupon_id <=", value, "couponId");
            return (Criteria) this;
        }

        public Criteria andCouponIdIn(List<Long> values) {
            addCriterion("coupon_id in", values, "couponId");
            return (Criteria) this;
        }

        public Criteria andCouponIdNotIn(List<Long> values) {
            addCriterion("coupon_id not in", values, "couponId");
            return (Criteria) this;
        }

        public Criteria andCouponIdBetween(Long value1, Long value2) {
            addCriterion("coupon_id between", value1, value2, "couponId");
            return (Criteria) this;
        }

        public Criteria andCouponIdNotBetween(Long value1, Long value2) {
            addCriterion("coupon_id not between", value1, value2, "couponId");
            return (Criteria) this;
        }

        public Criteria andReservationIdIsNull() {
            addCriterion("reservation_id is null");
            return (Criteria) this;
        }

        public Criteria andReservationIdIsNotNull() {
            addCriterion("reservation_id is not null");
            return (Criteria) this;
        }

        public Criteria andReservationIdEqualTo(Long value) {
            addCriterion("reservation_id =", value, "reservationId");
            return (Criteria) this;
        }

        public Criteria andReservationIdNotEqualTo(Long value) {
            addCriterion("reservation_id <>", value, "reservationId");
            return (Criteria) this;
        }

        public Criteria andReservationIdGreaterThan(Long value) {
            addCriterion("reservation_id >", value, "reservationId");
            return (Criteria) this;
        }

        public Criteria andReservationIdGreaterThanOrEqualTo(Long value) {
            addCriterion("reservation_id >=", value, "reservationId");
            return (Criteria) this;
        }

        public Criteria andReservationIdLessThan(Long value) {
            addCriterion("reservation_id <", value, "reservationId");
            return (Criteria) this;
        }

        public Criteria andReservationIdLessThanOrEqualTo(Long value) {
            addCriterion("reservation_id <=", value, "reservationId");
            return (Criteria) this;
        }

        public Criteria andReservationIdIn(List<Long> values) {
            addCriterion("reservation_id in", values, "reservationId");
            return (Criteria) this;
        }

        public Criteria andReservationIdNotIn(List<Long> values) {
            addCriterion("reservation_id not in", values, "reservationId");
            return (Criteria) this;
        }

        public Criteria andReservationIdBetween(Long value1, Long value2) {
            addCriterion("reservation_id between", value1, value2, "reservationId");
            return (Criteria) this;
        }

        public Criteria andReservationIdNotBetween(Long value1, Long value2) {
            addCriterion("reservation_id not between", value1, value2, "reservationId");
            return (Criteria) this;
        }

        public Criteria andDiscountAmountIsNull() {
            addCriterion("discount_amount is null");
            return (Criteria) this;
        }

        public Criteria andDiscountAmountIsNotNull() {
            addCriterion("discount_amount is not null");
            return (Criteria) this;
        }

        public Criteria andDiscountAmountEqualTo(BigDecimal value) {
            addCriterion("discount_amount =", value, "discountAmount");
            return (Criteria) this;
        }

        public Criteria andDiscountAmountNotEqualTo(BigDecimal value) {
            addCriterion("discount_amount <>", value, "discountAmount");
            return (Criteria) this;
        }

        public Criteria andDiscountAmountGreaterThan(BigDecimal value) {
            addCriterion("discount_amount >", value, "discountAmount");
            return (Criteria) this;
        }

        public Criteria andDiscountAmountGreaterThanOrEqualTo(BigDecimal value) {
            addCriterion("discount_amount >=", value, "discountAmount");
            return (Criteria) this;
        }

        public Criteria andDiscountAmountLessThan(BigDecimal value) {
            addCriterion("discount_amount <", value, "discountAmount");
            return (Criteria) this;
        }

        public Criteria andDiscountAmountLessThanOrEqualTo(BigDecimal value) {
            addCriterion("discount_amount <=", value, "discountAmount");
            return (Criteria) this;
        }

        public Criteria andDiscountAmountIn(List<BigDecimal> values) {
            addCriterion("discount_amount in", values, "discountAmount");
            return (Criteria) this;
        }

        public Criteria andDiscountAmountNotIn(List<BigDecimal> values) {
            addCriterion("discount_amount not in", values, "discountAmount");
            return (Criteria) this;
        }

        public Criteria andDiscountAmountBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("discount_amount between", value1, value2, "discountAmount");
            return (Criteria) this;
        }

        public Criteria andDiscountAmountNotBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("discount_amount not between", value1, value2, "discountAmount");
            return (Criteria) this;
        }

        public Criteria andUsedAtIsNull() {
            addCriterion("used_at is null");
            return (Criteria) this;
        }

        public Criteria andUsedAtIsNotNull() {
            addCriterion("used_at is not null");
            return (Criteria) this;
        }

        public Criteria andUsedAtEqualTo(Date value) {
            addCriterion("used_at =", value, "usedAt");
            return (Criteria) this;
        }

        public Criteria andUsedAtNotEqualTo(Date value) {
            addCriterion("used_at <>", value, "usedAt");
            return (Criteria) this;
        }

        public Criteria andUsedAtGreaterThan(Date value) {
            addCriterion("used_at >", value, "usedAt");
            return (Criteria) this;
        }

        public Criteria andUsedAtGreaterThanOrEqualTo(Date value) {
            addCriterion("used_at >=", value, "usedAt");
            return (Criteria) this;
        }

        public Criteria andUsedAtLessThan(Date value) {
            addCriterion("used_at <", value, "usedAt");
            return (Criteria) this;
        }

        public Criteria andUsedAtLessThanOrEqualTo(Date value) {
            addCriterion("used_at <=", value, "usedAt");
            return (Criteria) this;
        }

        public Criteria andUsedAtIn(List<Date> values) {
            addCriterion("used_at in", values, "usedAt");
            return (Criteria) this;
        }

        public Criteria andUsedAtNotIn(List<Date> values) {
            addCriterion("used_at not in", values, "usedAt");
            return (Criteria) this;
        }

        public Criteria andUsedAtBetween(Date value1, Date value2) {
            addCriterion("used_at between", value1, value2, "usedAt");
            return (Criteria) this;
        }

        public Criteria andUsedAtNotBetween(Date value1, Date value2) {
            addCriterion("used_at not between", value1, value2, "usedAt");
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