package com.maxexplode;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.logging.Logger;

public class CollisionSimulator extends VehicleSimulator {

    private static final Logger logger = Logger.getLogger(CollisionSimulator.class.getName());

    @Override
    public void readInputAndSimulate(String path) {
        try {
            List<String> lines = Files.readAllLines(Paths.get(path));

            if (lines.size() >= 3) {
                String gridDimensions = lines.getFirst();

                String[] gridCoordinates = gridDimensions.trim().split(" ");
                if (gridCoordinates.length != 2) {
                    throw new RuntimeException("Incorrect grid coordinates");
                }
                setGrid(new Simulator.Position(Integer.parseInt(gridCoordinates[0]), Integer.parseInt(gridCoordinates[1])));

                int batchSize = 3;

                if ((lines.size() - 1) % 3 != 0) {
                    throw new RuntimeException("Incorrect input");
                }

                int batches = ((lines.size() - 1) / 3);

                List<Simulator.Scenario> scenarios = new ArrayList<>();

                for (int i = 0; i < batches; i++) {
                    int start = i * batchSize;
                    List<String> batch = lines.subList(start, start + batchSize);
                    String vehicleName = batch.get(0);
                    String position = batch.get(1);
                    String commands = batch.get(2);
                    scenarios.add(createScenario(vehicleName, position, commands));
                }

                checkCollision(scenarios);

            } else {
                System.out.println("The file does not contain enough information.");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void checkCollision(List<Simulator.Scenario> scenarios) {

        if (scenarios.size() <= 1) {
            return;
        }

        Map<String, Map<Simulator.Position, Integer>> positionMap = new HashMap<>();

        Map<Simulator.Position, List<Simulator.Collision>> collisionMap = new HashMap<>();

        for (Simulator.Scenario scenario : scenarios) {
            Map<Simulator.Position, Integer> positionsMap = simulate(scenario, true);
            if (!positionMap.isEmpty()) {
                positionMap.forEach((s, positions) -> {
                    Set<Simulator.Position> keys = positionsMap.keySet();
                    keys.retainAll(positions.keySet());
                    if (!keys.isEmpty()) {
                        keys.forEach(position -> {
                            if (!collisionMap.containsKey(position)) {
                                collisionMap.put(position, new ArrayList<>());
                            }
                            collisionMap.get(position).add(new Simulator.Collision(s, positions.get(position)));
                            collisionMap.get(position).add(new Simulator.Collision(scenario.vehicle().name(), positions.get(position)));
                        });
                    }
                });
            }
            positionMap.put(scenario.vehicle().name(), positionsMap);
        }

        if (collisionMap.isEmpty()) {
            System.out.println("no collision");
        } else {
            //Since need to print the position in the middle for each output
            collisionMap.forEach((position, collisions) -> {
                List<String> vehicles = new ArrayList<>();
                Set<Integer> steps = new HashSet<>();
                collisions.forEach(collision -> {
                    vehicles.add(collision.vehicle());
                    steps.add(collision.step());
                });
                vehicles.forEach(vehicle -> System.out.printf("%s ", vehicle));
                System.out.println();
                System.out.printf("%s %s%n", position.width(), position.height());
                steps.forEach(step -> System.out.printf("%s ", step));
            });
        }
    }
}
