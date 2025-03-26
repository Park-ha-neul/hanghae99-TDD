package io.hhplus.tdd.point.unit;

import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.UserPoint;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class PointServiceUnitTest {

    @Mock
    private UserPointTable userPointTable;

    @Mock
    private UserPoint userPoint;

    @Test
    @DisplayName("특정 유저의 포인트 조회 기능 테스트")
    void getUserPoint(){
        // given
        Long userId = 1L;
        when(userPointTable.selectById(userId)).thenReturn(new UserPoint(1L, 100L, 171733234));

        // when
        UserPoint result = userPointTable.selectById(1L);

        //then
        Assertions.assertAll(
                () -> Assertions.assertEquals(userId, result.id()),
                () -> Assertions.assertEquals(100L, result.point())
        );
    }
}
