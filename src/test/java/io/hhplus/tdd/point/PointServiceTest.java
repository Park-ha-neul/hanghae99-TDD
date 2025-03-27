package io.hhplus.tdd.point;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@DisplayName("포인트 서비스 통합 테스트")
public class PointServiceTest {

    @Autowired
    private UserPointTable userPointTable;

    @Autowired
    private PointHistoryTable pointHistoryTable;

    @Autowired
    private PointService pointService;

    @Nested
    @DisplayName("포인트 조회")
    class getUserPoint{
        @Test
        @DisplayName("정상 케이스: 포인트 조회")
        void getUserPoint_Success_WhenUserIdIsValid(){
            Long userId = 1L;
            Long amount = 1000L;

            userPointTable.insertOrUpdate(1L, amount);

            //when
            UserPoint result = pointService.getUserPointById(userId);

            Assertions.assertAll(
                    () -> assertThat(result.id()).isEqualTo(userId),
                    () -> assertThat(result.point()).isEqualTo(amount)
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
            Long userId = 1L;

            pointHistoryTable.insert(userId, 1000L, TransactionType.USE, 1777);

            List<PointHistory> result = pointService.getUserHistoryById(userId);

            System.out.println("userID : " + result.get(0).userId());
            System.out.println("amount : " + result.get(0).amount());
            System.out.println("type : " + result.get(0).type());

            Assertions.assertAll(
                    () -> assertThat(result.get(0).userId()).isEqualTo(userId),
                    () -> assertThat(result.get(0).amount()).isEqualTo(1000L),
                    () -> assertThat(result.get(0).type()).isEqualTo(TransactionType.USE)
            );
        }

        @Test
        @DisplayName("비정상 케이스: userId가 음수인 경우 IllegalArgumentException 오류 발생")
        void getUserPointHistory_IllegalArgumentException_WhenUserIdLessThanZero(){
            Long userId = -1L;
            pointHistoryTable.insert(userId, 100L, TransactionType.CHARGE, 1777);

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
            Long userId = 1L;
            Long amount = 100L;
            userPointTable.insertOrUpdate(userId, 1000L);

            UserPoint result = pointService.chargeUserPoint(userId, amount);

            assertThat(result.point()).isEqualTo(1000L + amount);
        }

        @Test
        @DisplayName("비정상 케이스: 포인트가 음수인 경우 IllegalArgumentException 오류 발생")
        void chargePoint_IllegalArgumentException_WhenPointLessThanZero(){
            Long userId = 1L;
            Long amount = -100L;
            userPointTable.insertOrUpdate(userId, 1000L);

            IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class, () -> {
                pointService.chargeUserPoint(userId, amount);
            });

            Assertions.assertTrue(exception.getMessage().contains("충전할 포인트는 0보다 커야 합니다."));
        }

        @Test
        @DisplayName("비정상 케이스: 1회 최대 충전 포인트를 초과한 경우 IllegalArgumentException 오류 발생")
        void chargePoint_IllegalArgumentException_WhenPointExceededLimit(){
            Long userId = 1L;
            Long amount = 200000L;
            userPointTable.insertOrUpdate(userId, 1000L);

            IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class, () -> {
                pointService.chargeUserPoint(userId, amount);
            });

            Assertions.assertTrue(exception.getMessage().contains("1회 최대 충전 포인트는 100000점 입니다."));
        }

        @Test
        @DisplayName("비정상 케이스: 최대 포인트 한도를 넘은 경우 IllegalArgumentException 오류 발생")
        void chargePoint_IllegalArgumentException_WhenUserPointExceededLimit(){
            Long userId = 1L;
            Long amount = 1000L;
            userPointTable.insertOrUpdate(userId, 1000000L);

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
        @DisplayName("정상 케이스: 포인트 사용")
        void usePoint_Success_WhenPointIsValid(){
            Long userId = 1L;
            Long amount = 1000L;
            userPointTable.insertOrUpdate(userId, 2000L);

            UserPoint result = pointService.usePoint(userId, amount);

            assertThat(result.point()).isEqualTo(1000L);
        }

        @Test
        @DisplayName("비정상 케이스: 사용할 포인트가 음수인 경우 IllegalArgumentException 오류 발생")
        void usePoint_IllegalArgumentException_WhenPointLessThanZero(){
            Long userId = 1L;
            Long amount = -100L;
            userPointTable.insertOrUpdate(userId, 1000L);

            IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class, () -> {
                pointService.usePoint(userId, amount);
            });

            Assertions.assertTrue(exception.getMessage().contains("사용할 포인트는 0보다 커야 합니다."));
        }

        @Test
        @DisplayName("비정상 케이스: 잔고가 부족한 경우 IllegalArgumentException 오류 발생")
        void usePoint_IllegalArgumentException_WhenPointNotEnough(){
            Long userId = 1L;
            Long amount = 2000L;
            userPointTable.insertOrUpdate(userId, 1000L);

            IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class, () -> {
                pointService.usePoint(userId, amount);
            });

            Assertions.assertTrue(exception.getMessage().contains("포인트가 부족합니다."));
        }
    }
}
