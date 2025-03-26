package io.hhplus.tdd.point.unit;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.PointHistory;
import io.hhplus.tdd.point.TransactionType;
import io.hhplus.tdd.point.UserPoint;
import org.apache.catalina.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
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
}
