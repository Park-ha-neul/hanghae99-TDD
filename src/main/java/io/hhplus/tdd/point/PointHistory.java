package io.hhplus.tdd.point;

public record PointHistory(
        long id,
        long userId,
        long amount,
        TransactionType type,
        long updateMillis
) {

//    // 포인트 사용 이력 쌓는 비즈니스 로직
//    public PointHistory insertHistory(Long ){
//
//    }
}
