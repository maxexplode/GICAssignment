package com.maxexplode;

import java.util.logging.Level;
import java.util.logging.Logger;

public class SimulatorApplication {

    private static final Logger logger = Logger.getLogger(VehicleSimulator.class.getName());

    public static void main(String[] args) {
        logger.log(Level.INFO, "Starting simulator {0}", args);

        if(args.length!=2){
            throw new SimulatorException("Please provide input file path and type (ex : 1-simulate, 2-check for collisions)");
        }

        String path = args[0];
        String type = args[1];

        VehicleSimulator vehicleSimulator = new VehicleSimulator();
        if(type.equals("2")){
            vehicleSimulator = new CollisionSimulator();
        }

        vehicleSimulator.readInputAndSimulate(path);

    }
}
