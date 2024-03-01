package com.maxexplode;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.logging.Level;
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
                    throw new SimulatorException("Incorrect grid coordinates");
                }
                setGrid(new Simulator.Position(Integer.parseInt(gridCoordinates[0]), Integer.parseInt(gridCoordinates[1])));
                List<String> input = lines.subList(1, lines.size());
                if (input.size() % 3 != 0) {
                    throw new SimulatorException("Incorrect input");
                }
                int batchSize = 3;

                int batches = ((input.size()) / 3);

                List<Simulator.Scenario> scenarios = new ArrayList<>();

                for (int i = 0; i < batches; i++) {
                    int start = i * batchSize;
                    List<String> batch = input.subList(start, start + batchSize);
                    String vehicleName = batch.get(0);
                    String position = batch.get(1);
                    String commands = batch.get(2);
                    scenarios.add(createScenario(vehicleName, position, commands));
                }

                checkCollision(scenarios);

            } else {
                logger.log(Level.INFO,"Invalid input file");
            }
        } catch (IOException e) {
            throw new SimulatorException(e);
        }
    }

    private void checkCollision(List<Simulator.Scenario> scenarios) {

        if (scenarios.size() <= 1) {
            logger.log(Level.WARNING, "At least more than one scenario is required");
            return;
        }

        Map<String, Map<Simulator.Position, Integer>> vehiclePositionMap = new HashMap<>();

        Map<Simulator.Position, List<Simulator.Collision>> collisionMap = new HashMap<>();

        for (Simulator.Scenario scenario : scenarios) {
            Simulator.SimulatorResponse simulatorResponse = simulate(scenario);
            if (!vehiclePositionMap.isEmpty()) {
                vehiclePositionMap.forEach((vehicle, positions) -> {
                    Set<Simulator.Position> keys = simulatorResponse.positionMap().keySet();
                    keys.retainAll(positions.keySet());
                    if (!keys.isEmpty()) {
                        keys.forEach(position -> {
                            if (!collisionMap.containsKey(position)) {
                                collisionMap.put(position, new ArrayList<>());
                            }
                            collisionMap.get(position).add(new Simulator.Collision(vehicle, positions.get(position)));
                            collisionMap.get(position).add(new Simulator.Collision(scenario.vehicle().name(), positions.get(position)));
                        });
                    }
                });
            }
            vehiclePositionMap.put(scenario.vehicle().name(), simulatorResponse.positionMap());
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
                System.out.printf("%s %s\n", position.width(), position.height());
                steps.forEach(step -> System.out.printf("%s ", step));
            });
        }
    }
}
