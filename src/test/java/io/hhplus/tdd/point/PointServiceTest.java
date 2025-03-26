package io.hhplus.tdd.point;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import org.apache.catalina.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("포인트 서비스")
public class PointServiceTest {

    @Mock
    private UserPointTable userPointTable;

    @Mock
    private PointHistoryTable pointHistoryTable;

    private UserPoint createMockUserPoint(Long userId){
        return new UserPoint(userId, 2000L, 17777);
    };

    private List<PointHistory> createMockPointHistory(Long userId, Long amount){
        PointHistory mockPointHistory = new PointHistory(1L, userId, amount, TransactionType.CHARGE, 17777);
        return Collections.singletonList(mockPointHistory);
    }

    @InjectMocks
    private PointService pointService;

    @Nested
    @DisplayName("포인트 조회 기능 테스트")
    class getUserPoint {
        @Test
        @DisplayName("정상 케이스")
        void getUserPointById_Success_WhenUserIdIsValid() {
            // given: 테스트 데이터 및 Mock 설정
            Long userId = 1L;
            UserPoint mockUserPoint = createMockUserPoint(userId);
            when(userPointTable.selectById(userId)).thenReturn(mockUserPoint);

            // when: 서비스 호출
            UserPoint result = pointService.getUserPointById(userId);

            // then: 결과 검증
            Assertions.assertAll(
                    () -> Assertions.assertNotNull(result),
                    () -> Assertions.assertEquals(userId, result.id()),
                    () -> Assertions.assertEquals(mockUserPoint.point(), result.point())
            );
        }

        @Test
        @DisplayName("비정상 케이스: userId가 음수인 경우")
        void getUserPointById_ThrowsException_WhenUserIdLessThanZero() {
            // given
            Long userId = -1L;

            // 예외 발생 여부 검증
            IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class, () -> {
                pointService.getUserPointById(userId);
            });

            // 예외 메시지 검증
            assertThat(exception.getMessage()).isEqualTo("userId는 음수일 수 없습니다.");
        }
    }

    @Nested
    @DisplayName("포인트 이력 조회")
    class getPointHistory {
        @Test
        @DisplayName("정상 케이스")
        void getUserHistoryById_Success_WhenUserIdIsValid() {
            //given
            Long userId = 1L;
            Long amount = 100L;
            List<PointHistory> mockUserPoint = createMockPointHistory(userId, amount);
            when(pointHistoryTable.selectAllByUserId(userId)).thenReturn(mockUserPoint);

            //when
            List<PointHistory> result = pointService.getUserHistoryById(userId);

            //then
            Assertions.assertAll(
                    () -> Assertions.assertNotNull(result),
                    () -> Assertions.assertEquals(mockUserPoint, result)
            );
        }

        @Test
        @DisplayName("비정상 케이스: userId가 음수인 경우")
        void getUserHistoryById_ThrowsException_WhenUserIdLessThanZero(){
            Long userId = -1L;

            // 예외 발생 여부 검증
            IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class, () ->
                pointService.getUserHistoryById(userId)
            );

            // 예외 메세지 검증
            assertThat(exception.getMessage()).isEqualTo("userId는 음수일 수 없습니다.");
        }
    }

    @Nested
    @DisplayName("포인트 충전")
    class chargeUserPoint {
        @Test
        @DisplayName("정상 케이스: 기존 2000 포인트에 1000 충전함")
        void chargeUserPoint_Success_WhenAmountIsValid() {
            // given
            Long userId = 1L;
            Long amount = 1000L;
            UserPoint mockUserPoint = createMockUserPoint(userId);
            when(userPointTable.selectById(userId)).thenReturn(mockUserPoint);

            // when
            UserPoint result = pointService.chargeUserPoint(userId, amount);

            // then
            Assertions.assertNotNull(result);  // 반환값이 null이 아님
            Assertions.assertEquals(userId, result.id());
            Assertions.assertEquals(amount + mockUserPoint.point(), result.point());
        }

        @Test
        @DisplayName("비정상 케이스: 충전할 포인트가 0보다 작은 경우")
        void chargeUserPoint_ThrowsException_WhenAmountLessThanZero(){
            Long userId = 1L;
            Long amount = -1L;
            UserPoint mockUserPoint = createMockUserPoint(userId);
            when(userPointTable.selectById(userId)).thenReturn(mockUserPoint);

            // 예외 발생 여부 검증
            IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class, () -> {
                pointService.chargeUserPoint(userId, amount);
            });

            // 예외 메시지 검증
            Assertions.assertTrue(exception.getMessage().contains("충전할 포인트는 0보다 커야 합니다."));
        }

        @Test
        @DisplayName("비정상 케이스: 1회 충전할 포인트가 100000보다 큰 경우")
        void chargeUserPoint_ThrowsException_WhenAmountMoreThen100000(){
            Long userId = 1L;
            Long amount = 200000L;
            UserPoint mockUserPoint = createMockUserPoint(userId);
            when(userPointTable.selectById(userId)).thenReturn(mockUserPoint);

            // 예외 발생 여부 검증
            IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class, () -> {
                pointService.chargeUserPoint(userId, amount);
            });

            // 예외 메시지 검증
            Assertions.assertTrue(exception.getMessage().contains("최대 충전 포인트는 100000점 입니다."));
        }

        @Test
        @DisplayName("비정상 케이스: 사용자가 최대 포인트 한도(1000000)를 초과하여 충전할 경우")
        void chargeUserPoint_ThrowsException_WhenExceedMaxPointLimit(){
            Long userId = 1L;
            Long amount = 1000L;
            UserPoint mockUserPoint = new UserPoint(userId, 1000000, 17777);
            when(userPointTable.selectById(userId)).thenReturn(mockUserPoint);

            // 예외 발생 여부 검증
            IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class, () -> {
                pointService.chargeUserPoint(userId, amount);
            });

            // 예외 메시지 검증
            Assertions.assertTrue(exception.getMessage().contains("사용자 최대 포인트 한도(1000000점)를 초과하였습니다."));
        }

    }

    @Nested
    @DisplayName("포인트 사용")
    class usePoint{
        @Test
        @DisplayName("정상 케이스: 총 2000 포인트에서 1000 포인트를 사용하는 경우")
        void usePoint_Success_WhenAmountIsValid(){
            //given: 기본 mock 객체를 생성한다. (보유 포인트: 2000)
            Long userId = 1L;
            Long amount = 1000L;
            UserPoint mockUserPoint = createMockUserPoint(userId);
            when(userPointTable.selectById(userId)).thenReturn(mockUserPoint);

            //when: 사용하는 서비스를 호출하는데 1000L를 사용하게끔 설정한다.
            UserPoint updatePoint = pointService.usePoint(userId, amount);

            // then: 남은 포인트가 1000L인지 확인한다.
            Assertions.assertAll(
                    () -> Assertions.assertNotNull(updatePoint),
                    () -> Assertions.assertEquals(mockUserPoint.point() - amount, updatePoint.point())
            );
        }

        @Test
        @DisplayName("비정상 케이스 : 포인트가 0보다 작은 경우")
        void usePoint_ThrowsException_WhenPointLessThanZero(){
            Long userId = 1L;
            Long amount = -10L;

            UserPoint mockUserPoint = createMockUserPoint(userId);
            when(userPointTable.selectById(userId)).thenReturn(mockUserPoint);

            // 예외 발생 여부 검증
            IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class, () -> {
                pointService.usePoint(userId, amount);
            });

            // 예외 메시지 검증
            assertThat(exception.getMessage()).contains("사용할 포인트는 0보다 커야 합니다.");

        }

        @Test
        @DisplayName("비정상 케이스 : 잔고가 부족할 경우")
        void usePoint_ThrowsException_WhenNotEnoughPoint(){
            //given: 기본 mock 객체를 생성한다. (보유 포인트: 2000)
            Long userId = 1L;
            Long amount = 3000L;
            UserPoint mockUserPoint = new UserPoint(1L, 2000L, System.currentTimeMillis());
            when(userPointTable.selectById(userId)).thenReturn(mockUserPoint);

            // 예외 발생 여부 검증
            IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class, () -> {
                pointService.usePoint(userId, amount);
            });

            // 예외 메시지 검증
            assertThat(exception.getMessage()).contains("포인트가 부족합니다.");
        }
    }
}
