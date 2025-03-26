package io.hhplus.tdd.point;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class SampleTest {

    @BeforeAll
    static void setUp(){
        System.out.println("## Before All call");
    }

    @Test
    void test1(){
        System.out.println("## Test1 호출");
    }
}
