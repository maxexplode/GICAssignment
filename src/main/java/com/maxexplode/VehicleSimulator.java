package com.maxexplode;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiPredicate;
import java.util.logging.Level;
import java.util.logging.Logger;


public class VehicleSimulator {

    private static final Logger logger = Logger.getLogger(VehicleSimulator.class.getName());

    private static final Map<Simulator.Key, Simulator.Val> directionMap = new HashMap<>();

    private Simulator.Position grid;

    public static final String DELIMITER = " ";

    static {
        directionMap.put(new Simulator.Key(Simulator.Direction.N, Simulator.Command.L), new Simulator.Val(Simulator.Direction.W, (x, y) -> y.getAndDecrement()));
        directionMap.put(new Simulator.Key(Simulator.Direction.N, Simulator.Command.R), new Simulator.Val(Simulator.Direction.E, (x, y) -> y.getAndIncrement()));
        directionMap.put(new Simulator.Key(Simulator.Direction.W, Simulator.Command.L), new Simulator.Val(Simulator.Direction.S, (x, y) -> x.getAndDecrement()));
        directionMap.put(new Simulator.Key(Simulator.Direction.W, Simulator.Command.R), new Simulator.Val(Simulator.Direction.N, (x, y) -> x.getAndIncrement()));
        directionMap.put(new Simulator.Key(Simulator.Direction.S, Simulator.Command.L), new Simulator.Val(Simulator.Direction.E, (x, y) -> y.getAndIncrement()));
        directionMap.put(new Simulator.Key(Simulator.Direction.S, Simulator.Command.R), new Simulator.Val(Simulator.Direction.W, (x, y) -> y.getAndDecrement()));
        directionMap.put(new Simulator.Key(Simulator.Direction.E, Simulator.Command.L), new Simulator.Val(Simulator.Direction.N, (x, y) -> x.getAndIncrement()));
        directionMap.put(new Simulator.Key(Simulator.Direction.E, Simulator.Command.R), new Simulator.Val(Simulator.Direction.S, (x, y) -> x.getAndDecrement()));

        directionMap.put(new Simulator.Key(Simulator.Direction.N, null), new Simulator.Val(Simulator.Direction.N, (x, y) -> x.getAndIncrement()));
        directionMap.put(new Simulator.Key(Simulator.Direction.S, null), new Simulator.Val(Simulator.Direction.S, (x, y) -> x.getAndDecrement()));
        directionMap.put(new Simulator.Key(Simulator.Direction.E, null), new Simulator.Val(Simulator.Direction.E, (x, y) -> y.getAndIncrement()));
        directionMap.put(new Simulator.Key(Simulator.Direction.W, null), new Simulator.Val(Simulator.Direction.W, (x, y) -> y.getAndDecrement()));
    }

    public void readInputAndSimulate(String path) {
        try {
            List<String> lines = Files.readAllLines(Paths.get(path));

            if (lines.size() >= 3) {
                String gridDimensions = lines.get(0);
                String startPosition = lines.get(1);
                String movementInstructions = lines.get(2);

                logger.log(Level.INFO, "Grid dimensions: {0}", gridDimensions);
                logger.log(Level.INFO, "Starting position and orientation: {0}", startPosition);
                logger.log(Level.INFO, "Commands: {0}", movementInstructions);

                String[] gridCoordinates = gridDimensions.trim().split(" ");
                if (gridCoordinates.length != 2) {
                    throw new SimulatorException("Incorrect grid coordinates");
                }

                setGrid(new Simulator.Position(Integer.parseInt(gridCoordinates[0]), Integer.parseInt(gridCoordinates[1])));

                Simulator.Scenario scenario = createScenario(null, startPosition, movementInstructions);

                Simulator.SimulatorResponse simulatorResponse = simulate(scenario);

                Simulator.Position endPosition = simulatorResponse.currentPosition();

                logger.log(Level.INFO, "Current position height,width %s,%s and facing direction %s".formatted(endPosition.height(), endPosition.width(),
                        simulatorResponse.direction()));

            } else {
                logger.log(Level.INFO,"Invalid input file");
            }
        } catch (IOException e) {
            throw new SimulatorException(e);
        }
    }

    protected Simulator.Scenario createScenario(String vehicleName, String startPosition, String movementInstructions) {
        String[] positionCoordinates = startPosition.trim().split(DELIMITER);
        if (positionCoordinates.length != 3) {
            throw new SimulatorException("Incorrect start position coordinates");
        }
        return new Simulator.Scenario(
                new Simulator.Vehicle
                        (
                                vehicleName,
                                new Simulator.Position
                                        (
                                                Integer.parseInt(positionCoordinates[0]),
                                                Integer.parseInt(positionCoordinates[1])),
                                Simulator.Direction.valueOf(positionCoordinates[2])),
                movementInstructions);
    }

    protected Simulator.SimulatorResponse simulate(Simulator.Scenario scenario) {
        Map<Simulator.Position, Integer> positionMap = new HashMap<>();
        Simulator.Vehicle vehicle = scenario.vehicle();

        String commands = scenario.commands();
        AtomicInteger height = new AtomicInteger(vehicle.currentPosition().height());
        AtomicInteger width = new AtomicInteger(vehicle.currentPosition().width());

        BiPredicate<Integer, Integer> gridFunction = (currentHeight, currentWidth) -> currentHeight < grid.height()
                && currentWidth < grid.height();

        int stepCounter = 2;

        //Assuming no collisions at start
        //positionList.put(vehicle.currentPosition(), stepCounter++);

        char[] commandsArray = commands.toCharArray();
        Simulator.Direction currentDirection = vehicle.facingDirection();
        Simulator.Val currentVal = directionMap.get(new Simulator.Key(currentDirection, null));
        for (char commandString : commandsArray) {
            Simulator.Command command = Simulator.Command.valueOf(String.valueOf(commandString));
            switch (command) {
                case F -> {
                    if (currentVal != null) {
                        if (!gridFunction.test(height.get(), width.get())) {
                            throw new SimulatorException("Out of the box");
                        }
                        currentVal.consumer().accept(height, width);
                        currentDirection = currentVal.facingDirection();
                        positionMap.put(new Simulator.Position(width.get(), height.get()), stepCounter++);
                    }
                }
                case L, R -> {
                    currentVal = directionMap.get(new Simulator.Key(currentDirection, command));
                    currentDirection = currentVal.facingDirection();
                    //doNothing
                }
            }
        }
        return new Simulator.SimulatorResponse(positionMap, new Simulator.Position(width.get(), height.get()), currentDirection);
    }

    protected void setGrid(Simulator.Position grid) {
        this.grid = grid;
    }
}
