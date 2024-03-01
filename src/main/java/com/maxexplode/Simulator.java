package com.maxexplode;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;

public class Simulator {

    protected record Collision(String vehicle, int step) {
    }

    protected record Scenario(
            Vehicle vehicle,
            String commands
    ) {
    }

    protected record Position(
            int width, int height
    ) {
    }

    protected record Vehicle(
            String name,
            Position currentPosition,

            Direction facingDirection
    ) {
    }

    protected record Key(
            Direction current,
            Command command
    ) {
    }

    protected record Val(
            Direction facingDirection,
            BiConsumer<AtomicInteger, AtomicInteger> consumer) {
    }

    protected enum Direction {
        N, W, S, E
    }

    protected enum Command {
        F, L, R
    }
}