package io.hhplus.tdd.point;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@DisplayName("포인트 서비스 통합 테스트")
public class PointServiceTest {

    public static final Long USER_ID = 1L;
    public static final Long AMOUNT = 1000L;
    public static final TransactionType type = TransactionType.CHARGE;

    @Autowired
    private UserPointTable userPointTable;

    @Autowired
    private PointHistoryTable pointHistoryTable;

    @Autowired
    private PointService pointService;


    @BeforeEach
    void setUp(){
        userPointTable.insertOrUpdate(USER_ID, AMOUNT);
        pointHistoryTable.insert(USER_ID, AMOUNT, type, 1777);
    }

    @Nested
    @DisplayName("포인트 조회")
    class getUserPoint{

        @Test
        @DisplayName("정상 케이스: 포인트 조회")
        void getUserPoint_Success_WhenUserIdIsValid(){
            //when
            UserPoint result = pointService.getUserPointById(USER_ID);

            Assertions.assertAll(
                    () -> assertThat(result.id()).isEqualTo(USER_ID),
                    () -> assertThat(result.point()).isEqualTo(AMOUNT)
            );
        }

        @Test
        @DisplayName("비정상 케이스: userId가 음수인 경우 IllegalArgumentException 오류 발생")
        void getUserPoint_IllegalArgumentException_WhenUserIdLessThanZero(){
            Long userId = -1L;
            Long amount = 1000L;

            userPointTable.insertOrUpdate(userId, amount);

            IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class, () -> {
                pointService.getUserPointById(userId);
            });

            Assertions.assertTrue(exception.getMessage().contains("userId는 음수일 수 없습니다."));
        }
    }

    @Nested
    @DisplayName("포인트 이력 조회")
    class getUserPointHistory{
        @Test
        @DisplayName("정상 케이스: 포인트 이력 조회")
        void getUserPointHistory_Success_WhenUserIdIsValid(){
            List<PointHistory> result = pointService.getUserHistoryById(USER_ID);

            Assertions.assertAll(
                    () -> assertThat(result.get(0).userId()).isEqualTo(USER_ID),
                    () -> assertThat(result.get(0).amount()).isEqualTo(AMOUNT),
                    () -> assertThat(result.get(0).type()).isEqualTo(type)
            );
        }

        @Test
        @DisplayName("비정상 케이스: userId가 음수인 경우 IllegalArgumentException 오류 발생")
        void getUserPointHistory_IllegalArgumentException_WhenUserIdLessThanZero(){
            Long userId = -1L;
            pointHistoryTable.insert(userId, AMOUNT, TransactionType.CHARGE, 1777);

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
        @DisplayName("정상 케이스: 포인트 충전")
        void chargePoint_Success_WhenPointIsValid(){
            Long chargePoint = 100L;

            UserPoint result = pointService.chargeUserPoint(USER_ID, chargePoint);

            assertThat(result.point()).isEqualTo(AMOUNT + chargePoint);
        }

        @Test
        @DisplayName("비정상 케이스: 포인트가 음수인 경우 IllegalArgumentException 오류 발생")
        void chargePoint_IllegalArgumentException_WhenPointLessThanZero(){
            Long chargePoint = -100L;

            IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class, () -> {
                pointService.chargeUserPoint(USER_ID, chargePoint);
            });

            Assertions.assertTrue(exception.getMessage().contains("충전할 포인트는 0보다 커야 합니다."));
        }

        @Test
        @DisplayName("비정상 케이스: 1회 최대 충전 포인트를 초과한 경우 IllegalArgumentException 오류 발생")
        void chargePoint_IllegalArgumentException_WhenPointExceededLimit(){
            Long chargePoint = 200000L;

            IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class, () -> {
                pointService.chargeUserPoint(USER_ID, chargePoint);
            });

            Assertions.assertTrue(exception.getMessage().contains("1회 최대 충전 포인트는 100000점 입니다."));
        }

        @Test
        @DisplayName("비정상 케이스: 최대 포인트 한도를 넘은 경우 IllegalArgumentException 오류 발생")
        void chargePoint_IllegalArgumentException_WhenUserPointExceededLimit(){
            Long chargePoint = 1000L;
            userPointTable.insertOrUpdate(USER_ID, 1000000L);

            IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class, () -> {
                pointService.chargeUserPoint(USER_ID, chargePoint);
            });

            Assertions.assertTrue(exception.getMessage().contains("사용자 최대 포인트 한도(1000000점)를 초과하였습니다."));
        }
    }

    @Nested
    @DisplayName("포인트 사용")
    class usePoint{
        @Test
        @DisplayName("정상 케이스: 포인트 사용")
        void usePoint_Success_WhenPointIsValid(){
            Long usePoint = 100L;

            UserPoint result = pointService.usePoint(USER_ID, usePoint);

            assertThat(result.point()).isEqualTo(AMOUNT - usePoint);
        }

        @Test
        @DisplayName("비정상 케이스: 사용할 포인트가 음수인 경우 IllegalArgumentException 오류 발생")
        void usePoint_IllegalArgumentException_WhenPointLessThanZero(){
            Long usePoint = -100L;

            IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class, () -> {
                pointService.usePoint(USER_ID, usePoint);
            });

            Assertions.assertTrue(exception.getMessage().contains("사용할 포인트는 0보다 커야 합니다."));
        }

        @Test
        @DisplayName("비정상 케이스: 잔고가 부족한 경우 IllegalArgumentException 오류 발생")
        void usePoint_IllegalArgumentException_WhenPointNotEnough(){
            Long usePoint = 2000L;

            IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class, () -> {
                pointService.usePoint(USER_ID, usePoint);
            });

            Assertions.assertTrue(exception.getMessage().contains("포인트가 부족합니다."));
        }
    }
}
