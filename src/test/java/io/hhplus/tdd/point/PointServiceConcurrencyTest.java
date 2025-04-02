package io.hhplus.tdd.point;

import io.hhplus.tdd.database.UserPointTable;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@DisplayName("동시성 통합 테스트")
public class PointServiceConcurrencyTest {

    private static final Long USER_ID = 1L;
    private static final Long INITAIL_AMOUNT = 1000L;
    private static final int THREAD_COUNT = 10;

    @Autowired
    private PointService pointService;

    @Autowired
    private UserPointTable userPointTable;

    @BeforeEach
    void setUp(){
        userPointTable.insertOrUpdate(USER_ID, INITAIL_AMOUNT);
    }

    @Nested
    @DisplayName("동시성 테스트: 포인트 충전")
    class chargePoint{
        @Test
        @DisplayName("포인트 충전을 동시에 여러번 했을 경우 누락되지 않고 처리된다.")
        void chargePoint_Success_WHenMultipleRequests() throws InterruptedException {
            Long chargePoint = 1000L;
            ExecutorService executorService = Executors.newFixedThreadPool(THREAD_COUNT);
            CountDownLatch latch = new CountDownLatch(THREAD_COUNT);

            // 10개의 포인트 사용 요청
            for (int i = 0; i < THREAD_COUNT; i++) {
                executorService.submit(() -> {
                    try {
                        pointService.chargeUserPoint(USER_ID, chargePoint);
                    } finally {
                        latch.countDown();
                    }
                });
            }

            latch.await(); // 모든 스레드가 끝날 때까지 대기
            executorService.shutdown();

            //then
            UserPoint result = userPointTable.selectById(USER_ID);
            assertThat(result.point()).isEqualTo(INITAIL_AMOUNT + (chargePoint *THREAD_COUNT ));
        }

        @Test
        @DisplayName("포인트 충전을 동시에 여러 번 하던 중 사용자 최대 포인트 한도를 초과한 경우 IllegalArgumentException 오류 발생")
        void chargePoint_IllegalArgumentException_WhenMultipleRequestsUserPointExceededLimit() throws InterruptedException {
            Long chargePoint = 1000L;
            userPointTable.insertOrUpdate(USER_ID, 991000L); // 초기 포인트 설정

            ExecutorService executorService = Executors.newFixedThreadPool(THREAD_COUNT);
            CountDownLatch latch = new CountDownLatch(THREAD_COUNT);
            List<Throwable> exceptions = new ArrayList<>();  // 예외를 저장할 리스트

            // 10개의 포인트 사용 요청
            for (int i = 0; i < THREAD_COUNT; i++) {
                executorService.submit(() -> {
                    try {
                        pointService.chargeUserPoint(USER_ID, chargePoint);
                    } catch (Throwable t) {
                        synchronized (exceptions) {
                            exceptions.add(t);  // 예외를 리스트에 저장
                        }
                    } finally {
                        latch.countDown();
                    }
                });
            }

            latch.await(); // 모든 스레드가 끝날 때까지 대기
            executorService.shutdown();

            // 예외 발생 확인
            assertThat(exceptions).hasSizeGreaterThan(0);  // 예외가 하나라도 발생했어야 함
            assertThat(exceptions.get(0).getMessage()).contains("사용자 최대 포인트 한도(1000000점)를 초과하였습니다.");

            //then
            UserPoint result = userPointTable.selectById(USER_ID);
            assertThat(result.point()).isEqualTo(1000000L);
        }
    }

    @Nested
    @DisplayName("동시성 테스트: 포인트 사용")
    class usePoint{
        @Test
        @DisplayName("포인트 사용을 여러 번 했을 경우 잔고가 부족하지 않으면 누락되지 않고 처리된다.")
        void usePoint_Success_WhenMultipleRequests() throws InterruptedException {
            Long usePoint = 100L;
            ExecutorService executorService = Executors.newFixedThreadPool(THREAD_COUNT);
            CountDownLatch latch = new CountDownLatch(THREAD_COUNT);

            // 10개의 포인트 사용 요청
            for (int i = 0; i < THREAD_COUNT; i++) {
                executorService.submit(() -> {
                    try {
                        pointService.usePoint(USER_ID, usePoint);
                    } finally {
                        latch.countDown();
                    }
                });
            }

            latch.await(); // 모든 스레드가 끝날 때까지 대기
            executorService.shutdown();

            //then
            UserPoint result = userPointTable.selectById(USER_ID);
            assertThat(result.point()).isEqualTo(INITAIL_AMOUNT - (usePoint *THREAD_COUNT ));
        }

        @Test
        @DisplayName("포인트 사용을 여러 번 진행하던 중 잔고가 부족하면 IllegalArgumentException 오류 발생")
        void usePoint_IllegalArgumentException_WhenMultipleRequestsPointsNotEnough() throws InterruptedException {
            Long usePoint = 1000L;
            userPointTable.insertOrUpdate(USER_ID, 5000L); // 초기 포인트 설정

            ExecutorService executorService = Executors.newFixedThreadPool(THREAD_COUNT);
            CountDownLatch latch = new CountDownLatch(THREAD_COUNT);
            List<Throwable> exceptions = new ArrayList<>();  // 예외를 저장할 리스트

            // 10개의 포인트 사용 요청
            for (int i = 0; i < THREAD_COUNT; i++) {
                executorService.submit(() -> {
                    try {
                        pointService.usePoint(USER_ID, usePoint);
                    } catch (Throwable t) {
                        synchronized (exceptions) {
                            exceptions.add(t);  // 예외를 리스트에 저장
                        }
                    } finally {
                        latch.countDown();
                    }
                });
            }

            latch.await(); // 모든 스레드가 끝날 때까지 대기
            executorService.shutdown();

            // 예외 발생 확인
            assertThat(exceptions).hasSizeGreaterThan(0);  // 예외가 하나라도 발생했어야 함
            assertThat(exceptions.get(0).getMessage()).contains("포인트가 부족합니다.");

            //then
            UserPoint result = userPointTable.selectById(USER_ID);
            assertThat(result.point()).isEqualTo(0L);
        }
    }
}
