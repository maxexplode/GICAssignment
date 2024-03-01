package com.maxexplode;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class VehicleSimulatorTest {

    @Test
    public void testSimulator(){
        VehicleSimulator vehicleSimulator = new VehicleSimulator();
        vehicleSimulator.setGrid(new Simulator.Position(10, 10));
        Simulator.Scenario scenario = new Simulator.Scenario(
                new Simulator.Vehicle(null, new Simulator.Position(1, 2), Simulator.Direction.N),
                "FFRFFFRRLF"
        );
        Simulator.SimulatorResponse simulatorResponse = vehicleSimulator.simulate(scenario);
        Simulator.Position position = simulatorResponse.currentPosition();
        Assertions.assertEquals(3, position.height());
        Assertions.assertEquals(4, position.width());
        Assertions.assertEquals(Simulator.Direction.S, simulatorResponse.direction());
    }
}
