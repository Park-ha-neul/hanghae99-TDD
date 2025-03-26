package io.hhplus.tdd.point;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PointService {

    @Autowired
    private UserPointTable userPointTable; // 유저 포인트 조회, 유저 포인트 삽입
    private PointHistoryTable pointHistoryTable; // 포인트 내역 쌓기, 이력 조회

    /**
     * 포인트 조회 서비스
     * Exception 처리 항목
     *  1. userId가 음수인 경우
    */
    public UserPoint getUserPointById(Long userId) {
        if(userId <= 0){
            throw new IllegalArgumentException("userId는 음수일 수 없습니다.");
        }
        return userPointTable.selectById(userId);

    }

    /**
     * 포인트 이력 조회 서비스
     * Exception 처리 항목
     *  1. userId가 음수인 경우
     */
    public List<PointHistory> getUserHistoryById(Long userId) {
        if(userId <= 0){
            throw new IllegalArgumentException("userId는 음수일 수 없습니다.");
        }
        return pointHistoryTable.selectAllByUserId(userId);
    }

    /**
     * 포인트 충전 서비스
     * Exception 처리 항목
     *  1. 충전 포인트가 음수인 경우
     *  2. 1회 충전 포인트보다 더 많은 포인트를 충전할 경우 (1회 충전 제한: 100000)
     *  3. 최대 잔고가 넘었는데 더 충전을 하려고 하는 경우 (사용자 최대 포인트 한도: 1000000)
     */
    public UserPoint chargeUserPoint(Long userId, Long amount) {
        // 1. 사용자 조회(없으면 새로 생성)
        UserPoint userPoint = userPointTable.selectById(userId);

        // 2. 포인트 충전
        Long updatedAmount = userPoint.chargePoint(amount);

        // 3. 유저 테이블, 이력 테이블 업데이트
        userPointTable.insertOrUpdate(userId, updatedAmount);
        pointHistoryTable.insert(userId, amount, TransactionType.CHARGE, System.currentTimeMillis());

        return new UserPoint(userId, updatedAmount, System.currentTimeMillis());
    }

    /**
     * 포인트 사용 서비스
     * Exception 처리 항목
     *  1. 사용 포인트가 음수인 경우
     *  2. 포인트가 부족한 경우 (잔고 부족)
     */
    public UserPoint usePoint(Long userId, Long amount) {
        // 1. 사용자 조회(없으면 새로 생성)
        UserPoint userPoint = userPointTable.selectById(userId);

        // 2. 포인트 사용
        Long updatedAmount = userPoint.usePoint(amount);

        // 3. 유저 테이블, 이력 테이블 업데이트
        userPointTable.insertOrUpdate(userId, updatedAmount);
        pointHistoryTable.insert(userId, amount, TransactionType.USE, System.currentTimeMillis());

        return new UserPoint(userId, updatedAmount, System.currentTimeMillis());
    }
}
