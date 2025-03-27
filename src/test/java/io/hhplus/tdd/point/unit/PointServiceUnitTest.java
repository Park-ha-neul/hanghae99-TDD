package io.hhplus.tdd.point.unit;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.PointHistory;
import io.hhplus.tdd.point.PointService;
import io.hhplus.tdd.point.TransactionType;
import io.hhplus.tdd.point.UserPoint;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@DisplayName("포인트 서비스 단위 테스트")
public class PointServiceUnitTest {

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
    @DisplayName("포인트 조회")
    class getUserPoint{
        @Test
        @DisplayName("정상 케이스: 사용자 포인트 조회 정보가 mockData 와 동일한지 확인")
        void getUserPoint_Success_WhenUserIdIsValid(){
            UserPoint mockUserPoint = createMockUserPoint(1L);
            when(userPointTable.selectById(1L)).thenReturn(mockUserPoint);

            UserPoint result = pointService.getUserPointById(1L);

            assertThat(result).isEqualTo(mockUserPoint);
        }

        @Test
        @DisplayName("비정상 케이스: userId가 음수인 경우 IllegalArgumentException 오류 확인")
        void getUserPoint_ThrowsIllegalArgumentException_WhenUserIdLessThanZero(){
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
    class getUserPointHistory{
        @Test
        @DisplayName("정상 케이스: 사용자 포인트 이력 조회 정보가 mockData 와 동일한지 확인")
        void getUserPointHistory_Success_WhenUserIdIsValid(){
            Long userId = 1L;
            Long amount = 100L;
            List<PointHistory> mockUserPoint = createMockPointHistory(userId, amount);
            when(pointHistoryTable.selectAllByUserId(userId)).thenReturn(mockUserPoint);

            List<PointHistory> result = pointService.getUserHistoryById(userId);

            assertThat(result).isEqualTo(mockUserPoint);
        }

        @Test
        @DisplayName("비정상 케이스: userId가 음수인 경우 IllegalArgumentException 오류 확인")
        void getUserPointHistory_ThrowsIllegalArgumentException_WhenUserIdLessThanZero(){
            Long userId = -1L;

            IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class, () -> {
                pointService.getUserHistoryById(userId);
            });

            Assertions.assertTrue(exception.getMessage().contains("userId는 음수일 수 없습니다."));
        }
    }

    @Nested
    @DisplayName("포인트 충전")
    class chargePoint{
        @Test
        @DisplayName("정상 케이스: mockData 포인트의 충전한 포인트만큼 충전되었는지 확인")
        void chargePoint_Success_WhenPointIsValid(){
            Long userId = 1L;
            Long amount = 100L;
            UserPoint mockUserPoint = createMockUserPoint(userId);
            when(userPointTable.selectById(userId)).thenReturn(mockUserPoint);

            UserPoint result = pointService.chargeUserPoint(userId, amount);

            assertThat(result.point()).isEqualTo(mockUserPoint.point() + amount);
        }

        @Test
        @DisplayName("비정상 케이스: 충전할 포인트가 음수인 경우 IllegalArgumentException 오류 발생 확인")
        void chargePoint_ThrowsIllegalArgumentException_WhenPointLessThanZero(){
            Long userId = 1L;
            Long amount = -100L;
            UserPoint mockUserPoint = createMockUserPoint(userId);
            when(userPointTable.selectById(userId)).thenReturn(mockUserPoint);

            IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class, () -> {
                pointService.chargeUserPoint(userId, amount);
            });

            Assertions.assertTrue(exception.getMessage().contains("충전할 포인트는 0보다 커야 합니다."));
        }

        @Test
        @DisplayName("비정상 케이스: 1회 최대 충전 포인트를 초과한 경우 ThrowsIllegalArgumentException 오류 발생 확인")
        void chargePoint_ThrowsIllegalArgumentException_WhenPointMoreThan100000(){
            Long userId = 1L;
            Long amount = 200000L;
            UserPoint mockUserPoint = createMockUserPoint(userId);
            when(userPointTable.selectById(userId)).thenReturn(mockUserPoint);

            IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class, () -> {
                pointService.chargeUserPoint(userId, amount);
            });

            Assertions.assertTrue(exception.getMessage().contains("1회 최대 충전 포인트는 100000점 입니다."));
        }

        @Test
        @DisplayName("비정상 케이스: 사용자 최대 포인트 한도를 초과함에도 더 충전한 경우 ThrowsIllegalArgumentException 오류 발생 확인")
        void chargePoint_ThrowsIllegalArgumentException_WhenUserPointMoreThan1000000(){
            Long userId = 1L;
            Long amount = 100L;
            UserPoint mockUserPoint = new UserPoint(userId, 1000000, 1777);
            when(userPointTable.selectById(userId)).thenReturn(mockUserPoint);

            IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class, () -> {
                pointService.chargeUserPoint(userId, amount);
            });

            Assertions.assertTrue(exception.getMessage().contains("사용자 최대 포인트 한도(1000000점)를 초과하였습니다."));
        }
    }

    @Nested
    @DisplayName("포인트 사용")
    class usePoint{
        @Test
        @DisplayName("정상 케이스: 포인트 사용 후 mockData 포인트에서 사용한 만큼 포인트가 남았는지 확인")
        void usePoint_Success_WhenPointIsInvalid(){
            Long userId = 1L;
            Long amount = 100L;
            UserPoint mockUserPoint = createMockUserPoint(userId);
            when(userPointTable.selectById(userId)).thenReturn(mockUserPoint);

            UserPoint result = pointService.usePoint(userId, amount);

            assertThat(result.point()).isEqualTo(mockUserPoint.point() - amount);
        }

        @Test
        @DisplayName("비정상 케이스: 포인트가 음수인 경우 IllegalArgumentException 오류 발생 확인")
        void usePoint_IllegalArgumentException_WhenPointLessThanZero(){
            Long userId = 1L;
            Long amount = -100L;
            UserPoint mockUserPoint = createMockUserPoint(userId);
            when(userPointTable.selectById(userId)).thenReturn(mockUserPoint);

            IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class, () -> {
                pointService.usePoint(userId, amount);
            });

            Assertions.assertTrue(exception.getMessage().contains("사용할 포인트는 0보다 커야 합니다."));
        }

        @Test
        @DisplayName("비정상 케이스: 포인트가 부족한 경우 IllegalArgumentException 오류 발생 확인")
        void usePoint_IllegalArgumentException_WhenPointNotEnough(){
            Long userId = 1L;
            Long amount = 10000L;
            UserPoint mockUserPoint = createMockUserPoint(userId);
            when(userPointTable.selectById(userId)).thenReturn(mockUserPoint);

            IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class, () -> {
                pointService.usePoint(userId, amount);
            });

            Assertions.assertTrue(exception.getMessage().contains("포인트가 부족합니다."));
        }
    }
}
