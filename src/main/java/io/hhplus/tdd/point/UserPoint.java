package io.hhplus.tdd.point;

public record UserPoint(
        long id,
        long point,
        long updateMillis
) {

    public static UserPoint empty(long id) {
        return new UserPoint(id, 0, System.currentTimeMillis());
    }

    // 포인트 충전 비즈니스 로직
    public long chargePoint(Long amount){
        if(amount < 0){
            throw new IllegalArgumentException("충전할 포인트는 0보다 커야 합니다.");
        }
        if(amount > 100000){
            throw new IllegalArgumentException("1회 최대 충전 포인트는 100000점 입니다.");
        }
        long updatedAmount = this.point + amount;
        if(updatedAmount > 1000000){
            throw new IllegalArgumentException("사용자 최대 포인트 한도(1000000점)를 초과하였습니다.");
        }

        return updatedAmount;
    }

    // 포인트 사용 비즈니스 로직 (계산식)
    public Long usePoint(Long amount){
        if(amount < 0){
            throw new IllegalArgumentException("사용할 포인트는 0보다 커야 합니다.");
        }
        if(this.point < amount){
            throw new IllegalArgumentException("포인트가 부족합니다.");
        }
        long updatedAmount = this.point - amount;

        return updatedAmount;
    }
}
