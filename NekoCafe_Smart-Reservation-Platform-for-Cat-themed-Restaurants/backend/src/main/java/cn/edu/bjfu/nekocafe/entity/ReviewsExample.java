package cn.edu.bjfu.nekocafe.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ReviewsExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public ReviewsExample() {
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

        public Criteria andReviewIdIsNull() {
            addCriterion("review_id is null");
            return (Criteria) this;
        }

        public Criteria andReviewIdIsNotNull() {
            addCriterion("review_id is not null");
            return (Criteria) this;
        }

        public Criteria andReviewIdEqualTo(Long value) {
            addCriterion("review_id =", value, "reviewId");
            return (Criteria) this;
        }

        public Criteria andReviewIdNotEqualTo(Long value) {
            addCriterion("review_id <>", value, "reviewId");
            return (Criteria) this;
        }

        public Criteria andReviewIdGreaterThan(Long value) {
            addCriterion("review_id >", value, "reviewId");
            return (Criteria) this;
        }

        public Criteria andReviewIdGreaterThanOrEqualTo(Long value) {
            addCriterion("review_id >=", value, "reviewId");
            return (Criteria) this;
        }

        public Criteria andReviewIdLessThan(Long value) {
            addCriterion("review_id <", value, "reviewId");
            return (Criteria) this;
        }

        public Criteria andReviewIdLessThanOrEqualTo(Long value) {
            addCriterion("review_id <=", value, "reviewId");
            return (Criteria) this;
        }

        public Criteria andReviewIdIn(List<Long> values) {
            addCriterion("review_id in", values, "reviewId");
            return (Criteria) this;
        }

        public Criteria andReviewIdNotIn(List<Long> values) {
            addCriterion("review_id not in", values, "reviewId");
            return (Criteria) this;
        }

        public Criteria andReviewIdBetween(Long value1, Long value2) {
            addCriterion("review_id between", value1, value2, "reviewId");
            return (Criteria) this;
        }

        public Criteria andReviewIdNotBetween(Long value1, Long value2) {
            addCriterion("review_id not between", value1, value2, "reviewId");
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

        public Criteria andOverallRatingIsNull() {
            addCriterion("overall_rating is null");
            return (Criteria) this;
        }

        public Criteria andOverallRatingIsNotNull() {
            addCriterion("overall_rating is not null");
            return (Criteria) this;
        }

        public Criteria andOverallRatingEqualTo(Integer value) {
            addCriterion("overall_rating =", value, "overallRating");
            return (Criteria) this;
        }

        public Criteria andOverallRatingNotEqualTo(Integer value) {
            addCriterion("overall_rating <>", value, "overallRating");
            return (Criteria) this;
        }

        public Criteria andOverallRatingGreaterThan(Integer value) {
            addCriterion("overall_rating >", value, "overallRating");
            return (Criteria) this;
        }

        public Criteria andOverallRatingGreaterThanOrEqualTo(Integer value) {
            addCriterion("overall_rating >=", value, "overallRating");
            return (Criteria) this;
        }

        public Criteria andOverallRatingLessThan(Integer value) {
            addCriterion("overall_rating <", value, "overallRating");
            return (Criteria) this;
        }

        public Criteria andOverallRatingLessThanOrEqualTo(Integer value) {
            addCriterion("overall_rating <=", value, "overallRating");
            return (Criteria) this;
        }

        public Criteria andOverallRatingIn(List<Integer> values) {
            addCriterion("overall_rating in", values, "overallRating");
            return (Criteria) this;
        }

        public Criteria andOverallRatingNotIn(List<Integer> values) {
            addCriterion("overall_rating not in", values, "overallRating");
            return (Criteria) this;
        }

        public Criteria andOverallRatingBetween(Integer value1, Integer value2) {
            addCriterion("overall_rating between", value1, value2, "overallRating");
            return (Criteria) this;
        }

        public Criteria andOverallRatingNotBetween(Integer value1, Integer value2) {
            addCriterion("overall_rating not between", value1, value2, "overallRating");
            return (Criteria) this;
        }

        public Criteria andFoodRatingIsNull() {
            addCriterion("food_rating is null");
            return (Criteria) this;
        }

        public Criteria andFoodRatingIsNotNull() {
            addCriterion("food_rating is not null");
            return (Criteria) this;
        }

        public Criteria andFoodRatingEqualTo(Integer value) {
            addCriterion("food_rating =", value, "foodRating");
            return (Criteria) this;
        }

        public Criteria andFoodRatingNotEqualTo(Integer value) {
            addCriterion("food_rating <>", value, "foodRating");
            return (Criteria) this;
        }

        public Criteria andFoodRatingGreaterThan(Integer value) {
            addCriterion("food_rating >", value, "foodRating");
            return (Criteria) this;
        }

        public Criteria andFoodRatingGreaterThanOrEqualTo(Integer value) {
            addCriterion("food_rating >=", value, "foodRating");
            return (Criteria) this;
        }

        public Criteria andFoodRatingLessThan(Integer value) {
            addCriterion("food_rating <", value, "foodRating");
            return (Criteria) this;
        }

        public Criteria andFoodRatingLessThanOrEqualTo(Integer value) {
            addCriterion("food_rating <=", value, "foodRating");
            return (Criteria) this;
        }

        public Criteria andFoodRatingIn(List<Integer> values) {
            addCriterion("food_rating in", values, "foodRating");
            return (Criteria) this;
        }

        public Criteria andFoodRatingNotIn(List<Integer> values) {
            addCriterion("food_rating not in", values, "foodRating");
            return (Criteria) this;
        }

        public Criteria andFoodRatingBetween(Integer value1, Integer value2) {
            addCriterion("food_rating between", value1, value2, "foodRating");
            return (Criteria) this;
        }

        public Criteria andFoodRatingNotBetween(Integer value1, Integer value2) {
            addCriterion("food_rating not between", value1, value2, "foodRating");
            return (Criteria) this;
        }

        public Criteria andServiceRatingIsNull() {
            addCriterion("service_rating is null");
            return (Criteria) this;
        }

        public Criteria andServiceRatingIsNotNull() {
            addCriterion("service_rating is not null");
            return (Criteria) this;
        }

        public Criteria andServiceRatingEqualTo(Integer value) {
            addCriterion("service_rating =", value, "serviceRating");
            return (Criteria) this;
        }

        public Criteria andServiceRatingNotEqualTo(Integer value) {
            addCriterion("service_rating <>", value, "serviceRating");
            return (Criteria) this;
        }

        public Criteria andServiceRatingGreaterThan(Integer value) {
            addCriterion("service_rating >", value, "serviceRating");
            return (Criteria) this;
        }

        public Criteria andServiceRatingGreaterThanOrEqualTo(Integer value) {
            addCriterion("service_rating >=", value, "serviceRating");
            return (Criteria) this;
        }

        public Criteria andServiceRatingLessThan(Integer value) {
            addCriterion("service_rating <", value, "serviceRating");
            return (Criteria) this;
        }

        public Criteria andServiceRatingLessThanOrEqualTo(Integer value) {
            addCriterion("service_rating <=", value, "serviceRating");
            return (Criteria) this;
        }

        public Criteria andServiceRatingIn(List<Integer> values) {
            addCriterion("service_rating in", values, "serviceRating");
            return (Criteria) this;
        }

        public Criteria andServiceRatingNotIn(List<Integer> values) {
            addCriterion("service_rating not in", values, "serviceRating");
            return (Criteria) this;
        }

        public Criteria andServiceRatingBetween(Integer value1, Integer value2) {
            addCriterion("service_rating between", value1, value2, "serviceRating");
            return (Criteria) this;
        }

        public Criteria andServiceRatingNotBetween(Integer value1, Integer value2) {
            addCriterion("service_rating not between", value1, value2, "serviceRating");
            return (Criteria) this;
        }

        public Criteria andEnvironmentRatingIsNull() {
            addCriterion("environment_rating is null");
            return (Criteria) this;
        }

        public Criteria andEnvironmentRatingIsNotNull() {
            addCriterion("environment_rating is not null");
            return (Criteria) this;
        }

        public Criteria andEnvironmentRatingEqualTo(Integer value) {
            addCriterion("environment_rating =", value, "environmentRating");
            return (Criteria) this;
        }

        public Criteria andEnvironmentRatingNotEqualTo(Integer value) {
            addCriterion("environment_rating <>", value, "environmentRating");
            return (Criteria) this;
        }

        public Criteria andEnvironmentRatingGreaterThan(Integer value) {
            addCriterion("environment_rating >", value, "environmentRating");
            return (Criteria) this;
        }

        public Criteria andEnvironmentRatingGreaterThanOrEqualTo(Integer value) {
            addCriterion("environment_rating >=", value, "environmentRating");
            return (Criteria) this;
        }

        public Criteria andEnvironmentRatingLessThan(Integer value) {
            addCriterion("environment_rating <", value, "environmentRating");
            return (Criteria) this;
        }

        public Criteria andEnvironmentRatingLessThanOrEqualTo(Integer value) {
            addCriterion("environment_rating <=", value, "environmentRating");
            return (Criteria) this;
        }

        public Criteria andEnvironmentRatingIn(List<Integer> values) {
            addCriterion("environment_rating in", values, "environmentRating");
            return (Criteria) this;
        }

        public Criteria andEnvironmentRatingNotIn(List<Integer> values) {
            addCriterion("environment_rating not in", values, "environmentRating");
            return (Criteria) this;
        }

        public Criteria andEnvironmentRatingBetween(Integer value1, Integer value2) {
            addCriterion("environment_rating between", value1, value2, "environmentRating");
            return (Criteria) this;
        }

        public Criteria andEnvironmentRatingNotBetween(Integer value1, Integer value2) {
            addCriterion("environment_rating not between", value1, value2, "environmentRating");
            return (Criteria) this;
        }

        public Criteria andCatInteractionRatingIsNull() {
            addCriterion("cat_interaction_rating is null");
            return (Criteria) this;
        }

        public Criteria andCatInteractionRatingIsNotNull() {
            addCriterion("cat_interaction_rating is not null");
            return (Criteria) this;
        }

        public Criteria andCatInteractionRatingEqualTo(Integer value) {
            addCriterion("cat_interaction_rating =", value, "catInteractionRating");
            return (Criteria) this;
        }

        public Criteria andCatInteractionRatingNotEqualTo(Integer value) {
            addCriterion("cat_interaction_rating <>", value, "catInteractionRating");
            return (Criteria) this;
        }

        public Criteria andCatInteractionRatingGreaterThan(Integer value) {
            addCriterion("cat_interaction_rating >", value, "catInteractionRating");
            return (Criteria) this;
        }

        public Criteria andCatInteractionRatingGreaterThanOrEqualTo(Integer value) {
            addCriterion("cat_interaction_rating >=", value, "catInteractionRating");
            return (Criteria) this;
        }

        public Criteria andCatInteractionRatingLessThan(Integer value) {
            addCriterion("cat_interaction_rating <", value, "catInteractionRating");
            return (Criteria) this;
        }

        public Criteria andCatInteractionRatingLessThanOrEqualTo(Integer value) {
            addCriterion("cat_interaction_rating <=", value, "catInteractionRating");
            return (Criteria) this;
        }

        public Criteria andCatInteractionRatingIn(List<Integer> values) {
            addCriterion("cat_interaction_rating in", values, "catInteractionRating");
            return (Criteria) this;
        }

        public Criteria andCatInteractionRatingNotIn(List<Integer> values) {
            addCriterion("cat_interaction_rating not in", values, "catInteractionRating");
            return (Criteria) this;
        }

        public Criteria andCatInteractionRatingBetween(Integer value1, Integer value2) {
            addCriterion("cat_interaction_rating between", value1, value2, "catInteractionRating");
            return (Criteria) this;
        }

        public Criteria andCatInteractionRatingNotBetween(Integer value1, Integer value2) {
            addCriterion("cat_interaction_rating not between", value1, value2, "catInteractionRating");
            return (Criteria) this;
        }

        public Criteria andContentIsNull() {
            addCriterion("content is null");
            return (Criteria) this;
        }

        public Criteria andContentIsNotNull() {
            addCriterion("content is not null");
            return (Criteria) this;
        }

        public Criteria andContentEqualTo(String value) {
            addCriterion("content =", value, "content");
            return (Criteria) this;
        }

        public Criteria andContentNotEqualTo(String value) {
            addCriterion("content <>", value, "content");
            return (Criteria) this;
        }

        public Criteria andContentGreaterThan(String value) {
            addCriterion("content >", value, "content");
            return (Criteria) this;
        }

        public Criteria andContentGreaterThanOrEqualTo(String value) {
            addCriterion("content >=", value, "content");
            return (Criteria) this;
        }

        public Criteria andContentLessThan(String value) {
            addCriterion("content <", value, "content");
            return (Criteria) this;
        }

        public Criteria andContentLessThanOrEqualTo(String value) {
            addCriterion("content <=", value, "content");
            return (Criteria) this;
        }

        public Criteria andContentLike(String value) {
            addCriterion("content like", value, "content");
            return (Criteria) this;
        }

        public Criteria andContentNotLike(String value) {
            addCriterion("content not like", value, "content");
            return (Criteria) this;
        }

        public Criteria andContentIn(List<String> values) {
            addCriterion("content in", values, "content");
            return (Criteria) this;
        }

        public Criteria andContentNotIn(List<String> values) {
            addCriterion("content not in", values, "content");
            return (Criteria) this;
        }

        public Criteria andContentBetween(String value1, String value2) {
            addCriterion("content between", value1, value2, "content");
            return (Criteria) this;
        }

        public Criteria andContentNotBetween(String value1, String value2) {
            addCriterion("content not between", value1, value2, "content");
            return (Criteria) this;
        }

        public Criteria andImagesIsNull() {
            addCriterion("images is null");
            return (Criteria) this;
        }

        public Criteria andImagesIsNotNull() {
            addCriterion("images is not null");
            return (Criteria) this;
        }

        public Criteria andImagesEqualTo(Object value) {
            addCriterion("images =", value, "images");
            return (Criteria) this;
        }

        public Criteria andImagesNotEqualTo(Object value) {
            addCriterion("images <>", value, "images");
            return (Criteria) this;
        }

        public Criteria andImagesGreaterThan(Object value) {
            addCriterion("images >", value, "images");
            return (Criteria) this;
        }

        public Criteria andImagesGreaterThanOrEqualTo(Object value) {
            addCriterion("images >=", value, "images");
            return (Criteria) this;
        }

        public Criteria andImagesLessThan(Object value) {
            addCriterion("images <", value, "images");
            return (Criteria) this;
        }

        public Criteria andImagesLessThanOrEqualTo(Object value) {
            addCriterion("images <=", value, "images");
            return (Criteria) this;
        }

        public Criteria andImagesIn(List<Object> values) {
            addCriterion("images in", values, "images");
            return (Criteria) this;
        }

        public Criteria andImagesNotIn(List<Object> values) {
            addCriterion("images not in", values, "images");
            return (Criteria) this;
        }

        public Criteria andImagesBetween(Object value1, Object value2) {
            addCriterion("images between", value1, value2, "images");
            return (Criteria) this;
        }

        public Criteria andImagesNotBetween(Object value1, Object value2) {
            addCriterion("images not between", value1, value2, "images");
            return (Criteria) this;
        }

        public Criteria andReplyIsNull() {
            addCriterion("reply is null");
            return (Criteria) this;
        }

        public Criteria andReplyIsNotNull() {
            addCriterion("reply is not null");
            return (Criteria) this;
        }

        public Criteria andReplyEqualTo(String value) {
            addCriterion("reply =", value, "reply");
            return (Criteria) this;
        }

        public Criteria andReplyNotEqualTo(String value) {
            addCriterion("reply <>", value, "reply");
            return (Criteria) this;
        }

        public Criteria andReplyGreaterThan(String value) {
            addCriterion("reply >", value, "reply");
            return (Criteria) this;
        }

        public Criteria andReplyGreaterThanOrEqualTo(String value) {
            addCriterion("reply >=", value, "reply");
            return (Criteria) this;
        }

        public Criteria andReplyLessThan(String value) {
            addCriterion("reply <", value, "reply");
            return (Criteria) this;
        }

        public Criteria andReplyLessThanOrEqualTo(String value) {
            addCriterion("reply <=", value, "reply");
            return (Criteria) this;
        }

        public Criteria andReplyLike(String value) {
            addCriterion("reply like", value, "reply");
            return (Criteria) this;
        }

        public Criteria andReplyNotLike(String value) {
            addCriterion("reply not like", value, "reply");
            return (Criteria) this;
        }

        public Criteria andReplyIn(List<String> values) {
            addCriterion("reply in", values, "reply");
            return (Criteria) this;
        }

        public Criteria andReplyNotIn(List<String> values) {
            addCriterion("reply not in", values, "reply");
            return (Criteria) this;
        }

        public Criteria andReplyBetween(String value1, String value2) {
            addCriterion("reply between", value1, value2, "reply");
            return (Criteria) this;
        }

        public Criteria andReplyNotBetween(String value1, String value2) {
            addCriterion("reply not between", value1, value2, "reply");
            return (Criteria) this;
        }

        public Criteria andReplyAtIsNull() {
            addCriterion("reply_at is null");
            return (Criteria) this;
        }

        public Criteria andReplyAtIsNotNull() {
            addCriterion("reply_at is not null");
            return (Criteria) this;
        }

        public Criteria andReplyAtEqualTo(Date value) {
            addCriterion("reply_at =", value, "replyAt");
            return (Criteria) this;
        }

        public Criteria andReplyAtNotEqualTo(Date value) {
            addCriterion("reply_at <>", value, "replyAt");
            return (Criteria) this;
        }

        public Criteria andReplyAtGreaterThan(Date value) {
            addCriterion("reply_at >", value, "replyAt");
            return (Criteria) this;
        }

        public Criteria andReplyAtGreaterThanOrEqualTo(Date value) {
            addCriterion("reply_at >=", value, "replyAt");
            return (Criteria) this;
        }

        public Criteria andReplyAtLessThan(Date value) {
            addCriterion("reply_at <", value, "replyAt");
            return (Criteria) this;
        }

        public Criteria andReplyAtLessThanOrEqualTo(Date value) {
            addCriterion("reply_at <=", value, "replyAt");
            return (Criteria) this;
        }

        public Criteria andReplyAtIn(List<Date> values) {
            addCriterion("reply_at in", values, "replyAt");
            return (Criteria) this;
        }

        public Criteria andReplyAtNotIn(List<Date> values) {
            addCriterion("reply_at not in", values, "replyAt");
            return (Criteria) this;
        }

        public Criteria andReplyAtBetween(Date value1, Date value2) {
            addCriterion("reply_at between", value1, value2, "replyAt");
            return (Criteria) this;
        }

        public Criteria andReplyAtNotBetween(Date value1, Date value2) {
            addCriterion("reply_at not between", value1, value2, "replyAt");
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