package cn.edu.bjfu.nekocafe.entity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ReservationsExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public ReservationsExample() {
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

        public Criteria andTableIdIsNull() {
            addCriterion("table_id is null");
            return (Criteria) this;
        }

        public Criteria andTableIdIsNotNull() {
            addCriterion("table_id is not null");
            return (Criteria) this;
        }

        public Criteria andTableIdEqualTo(Integer value) {
            addCriterion("table_id =", value, "tableId");
            return (Criteria) this;
        }

        public Criteria andTableIdNotEqualTo(Integer value) {
            addCriterion("table_id <>", value, "tableId");
            return (Criteria) this;
        }

        public Criteria andTableIdGreaterThan(Integer value) {
            addCriterion("table_id >", value, "tableId");
            return (Criteria) this;
        }

        public Criteria andTableIdGreaterThanOrEqualTo(Integer value) {
            addCriterion("table_id >=", value, "tableId");
            return (Criteria) this;
        }

        public Criteria andTableIdLessThan(Integer value) {
            addCriterion("table_id <", value, "tableId");
            return (Criteria) this;
        }

        public Criteria andTableIdLessThanOrEqualTo(Integer value) {
            addCriterion("table_id <=", value, "tableId");
            return (Criteria) this;
        }

        public Criteria andTableIdIn(List<Integer> values) {
            addCriterion("table_id in", values, "tableId");
            return (Criteria) this;
        }

        public Criteria andTableIdNotIn(List<Integer> values) {
            addCriterion("table_id not in", values, "tableId");
            return (Criteria) this;
        }

        public Criteria andTableIdBetween(Integer value1, Integer value2) {
            addCriterion("table_id between", value1, value2, "tableId");
            return (Criteria) this;
        }

        public Criteria andTableIdNotBetween(Integer value1, Integer value2) {
            addCriterion("table_id not between", value1, value2, "tableId");
            return (Criteria) this;
        }

        public Criteria andCatProfileIdIsNull() {
            addCriterion("cat_profile_id is null");
            return (Criteria) this;
        }

        public Criteria andCatProfileIdIsNotNull() {
            addCriterion("cat_profile_id is not null");
            return (Criteria) this;
        }

        public Criteria andCatProfileIdEqualTo(Integer value) {
            addCriterion("cat_profile_id =", value, "catProfileId");
            return (Criteria) this;
        }

        public Criteria andCatProfileIdNotEqualTo(Integer value) {
            addCriterion("cat_profile_id <>", value, "catProfileId");
            return (Criteria) this;
        }

        public Criteria andCatProfileIdGreaterThan(Integer value) {
            addCriterion("cat_profile_id >", value, "catProfileId");
            return (Criteria) this;
        }

        public Criteria andCatProfileIdGreaterThanOrEqualTo(Integer value) {
            addCriterion("cat_profile_id >=", value, "catProfileId");
            return (Criteria) this;
        }

        public Criteria andCatProfileIdLessThan(Integer value) {
            addCriterion("cat_profile_id <", value, "catProfileId");
            return (Criteria) this;
        }

        public Criteria andCatProfileIdLessThanOrEqualTo(Integer value) {
            addCriterion("cat_profile_id <=", value, "catProfileId");
            return (Criteria) this;
        }

        public Criteria andCatProfileIdIn(List<Integer> values) {
            addCriterion("cat_profile_id in", values, "catProfileId");
            return (Criteria) this;
        }

        public Criteria andCatProfileIdNotIn(List<Integer> values) {
            addCriterion("cat_profile_id not in", values, "catProfileId");
            return (Criteria) this;
        }

        public Criteria andCatProfileIdBetween(Integer value1, Integer value2) {
            addCriterion("cat_profile_id between", value1, value2, "catProfileId");
            return (Criteria) this;
        }

        public Criteria andCatProfileIdNotBetween(Integer value1, Integer value2) {
            addCriterion("cat_profile_id not between", value1, value2, "catProfileId");
            return (Criteria) this;
        }

        public Criteria andReservationTimeIsNull() {
            addCriterion("reservation_time is null");
            return (Criteria) this;
        }

        public Criteria andReservationTimeIsNotNull() {
            addCriterion("reservation_time is not null");
            return (Criteria) this;
        }

        public Criteria andReservationTimeEqualTo(Date value) {
            addCriterion("reservation_time =", value, "reservationTime");
            return (Criteria) this;
        }

        public Criteria andReservationTimeNotEqualTo(Date value) {
            addCriterion("reservation_time <>", value, "reservationTime");
            return (Criteria) this;
        }

        public Criteria andReservationTimeGreaterThan(Date value) {
            addCriterion("reservation_time >", value, "reservationTime");
            return (Criteria) this;
        }

        public Criteria andReservationTimeGreaterThanOrEqualTo(Date value) {
            addCriterion("reservation_time >=", value, "reservationTime");
            return (Criteria) this;
        }

        public Criteria andReservationTimeLessThan(Date value) {
            addCriterion("reservation_time <", value, "reservationTime");
            return (Criteria) this;
        }

        public Criteria andReservationTimeLessThanOrEqualTo(Date value) {
            addCriterion("reservation_time <=", value, "reservationTime");
            return (Criteria) this;
        }

        public Criteria andReservationTimeIn(List<Date> values) {
            addCriterion("reservation_time in", values, "reservationTime");
            return (Criteria) this;
        }

        public Criteria andReservationTimeNotIn(List<Date> values) {
            addCriterion("reservation_time not in", values, "reservationTime");
            return (Criteria) this;
        }

        public Criteria andReservationTimeBetween(Date value1, Date value2) {
            addCriterion("reservation_time between", value1, value2, "reservationTime");
            return (Criteria) this;
        }

        public Criteria andReservationTimeNotBetween(Date value1, Date value2) {
            addCriterion("reservation_time not between", value1, value2, "reservationTime");
            return (Criteria) this;
        }

        public Criteria andDurationMinIsNull() {
            addCriterion("duration_min is null");
            return (Criteria) this;
        }

        public Criteria andDurationMinIsNotNull() {
            addCriterion("duration_min is not null");
            return (Criteria) this;
        }

        public Criteria andDurationMinEqualTo(Integer value) {
            addCriterion("duration_min =", value, "durationMin");
            return (Criteria) this;
        }

        public Criteria andDurationMinNotEqualTo(Integer value) {
            addCriterion("duration_min <>", value, "durationMin");
            return (Criteria) this;
        }

        public Criteria andDurationMinGreaterThan(Integer value) {
            addCriterion("duration_min >", value, "durationMin");
            return (Criteria) this;
        }

        public Criteria andDurationMinGreaterThanOrEqualTo(Integer value) {
            addCriterion("duration_min >=", value, "durationMin");
            return (Criteria) this;
        }

        public Criteria andDurationMinLessThan(Integer value) {
            addCriterion("duration_min <", value, "durationMin");
            return (Criteria) this;
        }

        public Criteria andDurationMinLessThanOrEqualTo(Integer value) {
            addCriterion("duration_min <=", value, "durationMin");
            return (Criteria) this;
        }

        public Criteria andDurationMinIn(List<Integer> values) {
            addCriterion("duration_min in", values, "durationMin");
            return (Criteria) this;
        }

        public Criteria andDurationMinNotIn(List<Integer> values) {
            addCriterion("duration_min not in", values, "durationMin");
            return (Criteria) this;
        }

        public Criteria andDurationMinBetween(Integer value1, Integer value2) {
            addCriterion("duration_min between", value1, value2, "durationMin");
            return (Criteria) this;
        }

        public Criteria andDurationMinNotBetween(Integer value1, Integer value2) {
            addCriterion("duration_min not between", value1, value2, "durationMin");
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

        public Criteria andSpecialRequestIsNull() {
            addCriterion("special_request is null");
            return (Criteria) this;
        }

        public Criteria andSpecialRequestIsNotNull() {
            addCriterion("special_request is not null");
            return (Criteria) this;
        }

        public Criteria andSpecialRequestEqualTo(String value) {
            addCriterion("special_request =", value, "specialRequest");
            return (Criteria) this;
        }

        public Criteria andSpecialRequestNotEqualTo(String value) {
            addCriterion("special_request <>", value, "specialRequest");
            return (Criteria) this;
        }

        public Criteria andSpecialRequestGreaterThan(String value) {
            addCriterion("special_request >", value, "specialRequest");
            return (Criteria) this;
        }

        public Criteria andSpecialRequestGreaterThanOrEqualTo(String value) {
            addCriterion("special_request >=", value, "specialRequest");
            return (Criteria) this;
        }

        public Criteria andSpecialRequestLessThan(String value) {
            addCriterion("special_request <", value, "specialRequest");
            return (Criteria) this;
        }

        public Criteria andSpecialRequestLessThanOrEqualTo(String value) {
            addCriterion("special_request <=", value, "specialRequest");
            return (Criteria) this;
        }

        public Criteria andSpecialRequestLike(String value) {
            addCriterion("special_request like", value, "specialRequest");
            return (Criteria) this;
        }

        public Criteria andSpecialRequestNotLike(String value) {
            addCriterion("special_request not like", value, "specialRequest");
            return (Criteria) this;
        }

        public Criteria andSpecialRequestIn(List<String> values) {
            addCriterion("special_request in", values, "specialRequest");
            return (Criteria) this;
        }

        public Criteria andSpecialRequestNotIn(List<String> values) {
            addCriterion("special_request not in", values, "specialRequest");
            return (Criteria) this;
        }

        public Criteria andSpecialRequestBetween(String value1, String value2) {
            addCriterion("special_request between", value1, value2, "specialRequest");
            return (Criteria) this;
        }

        public Criteria andSpecialRequestNotBetween(String value1, String value2) {
            addCriterion("special_request not between", value1, value2, "specialRequest");
            return (Criteria) this;
        }

        public Criteria andOrderAmountIsNull() {
            addCriterion("order_amount is null");
            return (Criteria) this;
        }

        public Criteria andOrderAmountIsNotNull() {
            addCriterion("order_amount is not null");
            return (Criteria) this;
        }

        public Criteria andOrderAmountEqualTo(BigDecimal value) {
            addCriterion("order_amount =", value, "orderAmount");
            return (Criteria) this;
        }

        public Criteria andOrderAmountNotEqualTo(BigDecimal value) {
            addCriterion("order_amount <>", value, "orderAmount");
            return (Criteria) this;
        }

        public Criteria andOrderAmountGreaterThan(BigDecimal value) {
            addCriterion("order_amount >", value, "orderAmount");
            return (Criteria) this;
        }

        public Criteria andOrderAmountGreaterThanOrEqualTo(BigDecimal value) {
            addCriterion("order_amount >=", value, "orderAmount");
            return (Criteria) this;
        }

        public Criteria andOrderAmountLessThan(BigDecimal value) {
            addCriterion("order_amount <", value, "orderAmount");
            return (Criteria) this;
        }

        public Criteria andOrderAmountLessThanOrEqualTo(BigDecimal value) {
            addCriterion("order_amount <=", value, "orderAmount");
            return (Criteria) this;
        }

        public Criteria andOrderAmountIn(List<BigDecimal> values) {
            addCriterion("order_amount in", values, "orderAmount");
            return (Criteria) this;
        }

        public Criteria andOrderAmountNotIn(List<BigDecimal> values) {
            addCriterion("order_amount not in", values, "orderAmount");
            return (Criteria) this;
        }

        public Criteria andOrderAmountBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("order_amount between", value1, value2, "orderAmount");
            return (Criteria) this;
        }

        public Criteria andOrderAmountNotBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("order_amount not between", value1, value2, "orderAmount");
            return (Criteria) this;
        }

        public Criteria andTotalAmountIsNull() {
            addCriterion("total_amount is null");
            return (Criteria) this;
        }

        public Criteria andTotalAmountIsNotNull() {
            addCriterion("total_amount is not null");
            return (Criteria) this;
        }

        public Criteria andTotalAmountEqualTo(BigDecimal value) {
            addCriterion("total_amount =", value, "totalAmount");
            return (Criteria) this;
        }

        public Criteria andTotalAmountNotEqualTo(BigDecimal value) {
            addCriterion("total_amount <>", value, "totalAmount");
            return (Criteria) this;
        }

        public Criteria andTotalAmountGreaterThan(BigDecimal value) {
            addCriterion("total_amount >", value, "totalAmount");
            return (Criteria) this;
        }

        public Criteria andTotalAmountGreaterThanOrEqualTo(BigDecimal value) {
            addCriterion("total_amount >=", value, "totalAmount");
            return (Criteria) this;
        }

        public Criteria andTotalAmountLessThan(BigDecimal value) {
            addCriterion("total_amount <", value, "totalAmount");
            return (Criteria) this;
        }

        public Criteria andTotalAmountLessThanOrEqualTo(BigDecimal value) {
            addCriterion("total_amount <=", value, "totalAmount");
            return (Criteria) this;
        }

        public Criteria andTotalAmountIn(List<BigDecimal> values) {
            addCriterion("total_amount in", values, "totalAmount");
            return (Criteria) this;
        }

        public Criteria andTotalAmountNotIn(List<BigDecimal> values) {
            addCriterion("total_amount not in", values, "totalAmount");
            return (Criteria) this;
        }

        public Criteria andTotalAmountBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("total_amount between", value1, value2, "totalAmount");
            return (Criteria) this;
        }

        public Criteria andTotalAmountNotBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("total_amount not between", value1, value2, "totalAmount");
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

        public Criteria andPointsUsedIsNull() {
            addCriterion("points_used is null");
            return (Criteria) this;
        }

        public Criteria andPointsUsedIsNotNull() {
            addCriterion("points_used is not null");
            return (Criteria) this;
        }

        public Criteria andPointsUsedEqualTo(Integer value) {
            addCriterion("points_used =", value, "pointsUsed");
            return (Criteria) this;
        }

        public Criteria andPointsUsedNotEqualTo(Integer value) {
            addCriterion("points_used <>", value, "pointsUsed");
            return (Criteria) this;
        }

        public Criteria andPointsUsedGreaterThan(Integer value) {
            addCriterion("points_used >", value, "pointsUsed");
            return (Criteria) this;
        }

        public Criteria andPointsUsedGreaterThanOrEqualTo(Integer value) {
            addCriterion("points_used >=", value, "pointsUsed");
            return (Criteria) this;
        }

        public Criteria andPointsUsedLessThan(Integer value) {
            addCriterion("points_used <", value, "pointsUsed");
            return (Criteria) this;
        }

        public Criteria andPointsUsedLessThanOrEqualTo(Integer value) {
            addCriterion("points_used <=", value, "pointsUsed");
            return (Criteria) this;
        }

        public Criteria andPointsUsedIn(List<Integer> values) {
            addCriterion("points_used in", values, "pointsUsed");
            return (Criteria) this;
        }

        public Criteria andPointsUsedNotIn(List<Integer> values) {
            addCriterion("points_used not in", values, "pointsUsed");
            return (Criteria) this;
        }

        public Criteria andPointsUsedBetween(Integer value1, Integer value2) {
            addCriterion("points_used between", value1, value2, "pointsUsed");
            return (Criteria) this;
        }

        public Criteria andPointsUsedNotBetween(Integer value1, Integer value2) {
            addCriterion("points_used not between", value1, value2, "pointsUsed");
            return (Criteria) this;
        }

        public Criteria andPointsEarnedIsNull() {
            addCriterion("points_earned is null");
            return (Criteria) this;
        }

        public Criteria andPointsEarnedIsNotNull() {
            addCriterion("points_earned is not null");
            return (Criteria) this;
        }

        public Criteria andPointsEarnedEqualTo(Integer value) {
            addCriterion("points_earned =", value, "pointsEarned");
            return (Criteria) this;
        }

        public Criteria andPointsEarnedNotEqualTo(Integer value) {
            addCriterion("points_earned <>", value, "pointsEarned");
            return (Criteria) this;
        }

        public Criteria andPointsEarnedGreaterThan(Integer value) {
            addCriterion("points_earned >", value, "pointsEarned");
            return (Criteria) this;
        }

        public Criteria andPointsEarnedGreaterThanOrEqualTo(Integer value) {
            addCriterion("points_earned >=", value, "pointsEarned");
            return (Criteria) this;
        }

        public Criteria andPointsEarnedLessThan(Integer value) {
            addCriterion("points_earned <", value, "pointsEarned");
            return (Criteria) this;
        }

        public Criteria andPointsEarnedLessThanOrEqualTo(Integer value) {
            addCriterion("points_earned <=", value, "pointsEarned");
            return (Criteria) this;
        }

        public Criteria andPointsEarnedIn(List<Integer> values) {
            addCriterion("points_earned in", values, "pointsEarned");
            return (Criteria) this;
        }

        public Criteria andPointsEarnedNotIn(List<Integer> values) {
            addCriterion("points_earned not in", values, "pointsEarned");
            return (Criteria) this;
        }

        public Criteria andPointsEarnedBetween(Integer value1, Integer value2) {
            addCriterion("points_earned between", value1, value2, "pointsEarned");
            return (Criteria) this;
        }

        public Criteria andPointsEarnedNotBetween(Integer value1, Integer value2) {
            addCriterion("points_earned not between", value1, value2, "pointsEarned");
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

        public Criteria andUpdatedAtIsNull() {
            addCriterion("updated_at is null");
            return (Criteria) this;
        }

        public Criteria andUpdatedAtIsNotNull() {
            addCriterion("updated_at is not null");
            return (Criteria) this;
        }

        public Criteria andUpdatedAtEqualTo(Date value) {
            addCriterion("updated_at =", value, "updatedAt");
            return (Criteria) this;
        }

        public Criteria andUpdatedAtNotEqualTo(Date value) {
            addCriterion("updated_at <>", value, "updatedAt");
            return (Criteria) this;
        }

        public Criteria andUpdatedAtGreaterThan(Date value) {
            addCriterion("updated_at >", value, "updatedAt");
            return (Criteria) this;
        }

        public Criteria andUpdatedAtGreaterThanOrEqualTo(Date value) {
            addCriterion("updated_at >=", value, "updatedAt");
            return (Criteria) this;
        }

        public Criteria andUpdatedAtLessThan(Date value) {
            addCriterion("updated_at <", value, "updatedAt");
            return (Criteria) this;
        }

        public Criteria andUpdatedAtLessThanOrEqualTo(Date value) {
            addCriterion("updated_at <=", value, "updatedAt");
            return (Criteria) this;
        }

        public Criteria andUpdatedAtIn(List<Date> values) {
            addCriterion("updated_at in", values, "updatedAt");
            return (Criteria) this;
        }

        public Criteria andUpdatedAtNotIn(List<Date> values) {
            addCriterion("updated_at not in", values, "updatedAt");
            return (Criteria) this;
        }

        public Criteria andUpdatedAtBetween(Date value1, Date value2) {
            addCriterion("updated_at between", value1, value2, "updatedAt");
            return (Criteria) this;
        }

        public Criteria andUpdatedAtNotBetween(Date value1, Date value2) {
            addCriterion("updated_at not between", value1, value2, "updatedAt");
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