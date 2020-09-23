package com.github.basdxz.journeymappartyhelper.model;

import com.github.basdxz.journeymappartyhelper.Util;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.*;

public class ChatFriendlyWaypointTest {

    @Before
    public void setUp(){
        byte[] bytes = {2, 2};
        byte[] correctionPayloadBytes = Arrays.copyOfRange(bytes,1, 3);

    }

    //TODO: This test literally only tests "Arrays.copyOfRange" which is an internal Method, that doesnt need to be tested
    @Test
    public void constructors() {
        byte[] main = {1, 2, 3, 4, 5};
        int from = 1;
        int to = 1;
        byte[] part = Arrays.copyOfRange(main, 0, 3);
        byte[] part2 = Arrays.copyOfRange(main, 3, 5);

        int a = 2;
        int b = 2;
        assertEquals(a, b);
    }
}