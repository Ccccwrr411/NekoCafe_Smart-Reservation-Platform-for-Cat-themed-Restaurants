package cn.edu.bjfu.nekocafe.service.impl;

import cn.edu.bjfu.nekocafe.dto.ReviewSubmitDTO;
import cn.edu.bjfu.nekocafe.entity.MemberExt;
import cn.edu.bjfu.nekocafe.entity.Reservations;
import cn.edu.bjfu.nekocafe.mapper.MemberExtMapper;
import cn.edu.bjfu.nekocafe.mapper.PointsLogMapper;
import cn.edu.bjfu.nekocafe.mapper.ReservationsMapper;
import cn.edu.bjfu.nekocafe.mapper.ReviewsMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ReviewServiceImplTest {
  private ReviewServiceImpl service;
  private ReviewsMapper reviewsMapper;
  private ReservationsMapper reservationsMapper;
  private PointsLogMapper pointsLogMapper;
  private MemberExtMapper memberExtMapper;

  @BeforeEach
  void setUp() {
    service = new ReviewServiceImpl();
    reviewsMapper = mock(ReviewsMapper.class);
    reservationsMapper = mock(ReservationsMapper.class);
    pointsLogMapper = mock(PointsLogMapper.class);
    memberExtMapper = mock(MemberExtMapper.class);
    ReflectionTestUtils.setField(service, "reviewsMapper", reviewsMapper);
    ReflectionTestUtils.setField(service, "reservationsMapper", reservationsMapper);
    ReflectionTestUtils.setField(service, "pointsLogMapper", pointsLogMapper);
    ReflectionTestUtils.setField(service, "memberExtMapper", memberExtMapper);
  }

  @Test
  void submitReview() {
    ReviewSubmitDTO dto = new ReviewSubmitDTO();
    dto.setOrderId("ORD0000000007");
    dto.setRating(5);
    dto.setContent("很棒");
    Reservations reservation = new Reservations();
    reservation.setReservationId(7L);
    reservation.setUserId(9L);
    reservation.setStoreId(1);
    reservation.setStatus("COMPLETED");
    when(reservationsMapper.selectByPrimaryKey(7L)).thenReturn(reservation);
    when(reviewsMapper.countByExample(any())).thenReturn(0L);
    MemberExt member = new MemberExt();
    member.setUserId(9L);
    member.setTotalPoints(20);
    when(memberExtMapper.selectByPrimaryKey(9L)).thenReturn(member);

    Map<String, Object> result = service.submitReview(9L, dto);

    assertEquals("published", result.get("status"));
    assertEquals(10, result.get("pointsEarned"));
    assertEquals(30, member.getTotalPoints());
    verify(reviewsMapper).insertSelective(any());
    verify(pointsLogMapper).insertSelective(any());
  }
}
