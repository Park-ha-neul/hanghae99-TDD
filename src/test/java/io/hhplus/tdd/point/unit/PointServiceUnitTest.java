package io.hhplus.tdd.point.unit;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.PointHistory;
import io.hhplus.tdd.point.TransactionType;
import io.hhplus.tdd.point.UserPoint;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class PointServiceUnitTest {

    @Mock
    private UserPointTable userPointTable;

    @Mock
    private PointHistoryTable pointHistoryTable;

    @Mock
    private UserPoint userPoint;

    @Test
    @DisplayName("포인트 조회")
    void getUserPoint(){
        UserPoint mockUserPoint = new UserPoint(1L, 100L, 1777);
        when(userPointTable.selectById(1L)).thenReturn(mockUserPoint);

        UserPoint result = userPointTable.selectById(1L);

        assertThat(result).isEqualTo(mockUserPoint);
    }

    @Test
    @DisplayName("포인트 이력 조회")
    void getUserHistory(){
        PointHistory mockPointHistory = new PointHistory(1L, 1L, 100L, TransactionType.CHARGE, 1777);
        when(pointHistoryTable.selectAllByUserId(1L)).thenReturn(Collections.singletonList(mockPointHistory));

        List<PointHistory> result = pointHistoryTable.selectAllByUserId(1L);

        assertThat(result.get(0)).isEqualTo(mockPointHistory);
    }

    @Test
    @DisplayName("포인트 충전")
    void chargePoint(){
        UserPoint userPoint = new UserPoint(1L, 100L, 1777);

        Long updatedAmount = userPoint.chargePoint(100L);

        assertThat(updatedAmount).isEqualTo(200L);
    }

    @Test
    @DisplayName("포인트 충전 오류: 충전할 포인트가 음수인 경우")
    void chargePoint_ThrowException_WhenPointLessThanZero(){
        UserPoint userPoint = new UserPoint(1L, 100L, 1777);

        // 예외 발생 여부 검증
        IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            userPoint.chargePoint(-100L);
        });

        // 예외 메시지 검증
        Assertions.assertTrue(exception.getMessage().contains("충전할 포인트는 0보다 커야 합니다."));
    }

    @Test
    @DisplayName("포인트 충전 오류: 1회 최대 충전량을 넘은 경우")
    void chargePoint_ThrowException_WhenPointMoreThanLimit(){
        UserPoint userPoint = new UserPoint(1L, 100L, 1777);

        // 예외 발생 여부 검증
        IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            userPoint.chargePoint(100001L);
        });

        // 예외 메시지 검증
        Assertions.assertTrue(exception.getMessage().contains("1회 최대 충전 포인트는 100000점 입니다."));
    }

    @Test
    @DisplayName("포인트 충전 오류: 최대 포인트 한도를 넘은 경우")
    void chargePoint_ThrowException_WhenExceedMaxPointLimit(){
        UserPoint userPoint = new UserPoint(1L, 1000000L, 1777);

        // 예외 발생 여부 검증
        IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            userPoint.chargePoint(100L);
        });

        // 예외 메시지 검증
        Assertions.assertTrue(exception.getMessage().contains("사용자 최대 포인트 한도(1000000점)를 초과하였습니다."));
    }

    @Test
    @DisplayName("유저 테이블 업데이트")
    void userPointTableUpdate(){
        UserPoint updatedUserPoint = new UserPoint(1L, 200L, 1777);
        when(userPointTable.insertOrUpdate(1L, 200L)).thenReturn(updatedUserPoint);

        UserPoint result = userPointTable.insertOrUpdate(1L, 200L);

        assertThat(result.point()).isEqualTo(200L);
    }

    @Test
    @DisplayName("이력 테이블 업데이트")
    void historyTableUpdate(){
        PointHistory updatedHistory = new PointHistory(1L, 1L, 200L, TransactionType.CHARGE, 1777);
        when(pointHistoryTable.insert(1L, 200L, TransactionType.CHARGE, 1777)).thenReturn(updatedHistory);

        PointHistory result = pointHistoryTable.insert(1L, 200L, TransactionType.CHARGE, 1777);

        assertThat(result).isEqualTo(updatedHistory);
    }

    @Test
    @DisplayName("포인트 사용")
    void usePoint(){
        UserPoint userPoint = new UserPoint(1L, 200L, 1777);

        long updatedAmount = userPoint.usePoint(100L);

        assertThat(updatedAmount).isEqualTo(100L);
    }

    @Test
    @DisplayName("포인트 사용 오류: 사용할 포인트가 음수인 경우")
    void usePoint_ThrowException_WhenPointLessThanZero(){
        UserPoint userPoint = new UserPoint(1L, 200L, 1777);

        // 예외 발생 여부 검증
        IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            userPoint.usePoint(-100L);
        });

        // 예외 메시지 검증
        Assertions.assertTrue(exception.getMessage().contains("사용할 포인트는 0보다 커야 합니다."));
    }

    @Test
    @DisplayName("포인트 사용 오류: 포인트가 부족한 경우")
    void usePoint_ThrowException_WhenNotEnoughPoint(){
        UserPoint userPoint = new UserPoint(1L, 200L, 1777);

        // 예외 발생 여부 검증
        IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            userPoint.usePoint(300L);
        });

        // 예외 메시지 검증
        Assertions.assertTrue(exception.getMessage().contains("포인트가 부족합니다."));
    }
}
