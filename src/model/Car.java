package model;

import di.annotations.Auto;

public class Car {
    @Auto
    private Engine engine;
    private Gear gear;

    public Car() {
    }

    public Car(Engine engine, Gear gear) {
        this.engine = engine;
        this.gear = gear;
    }

    public Engine getEngine() {
        return engine;
    }

    public void setEngine(Engine engine) {
        this.engine = engine;
    }

    public Gear getGear() {
        return gear;
    }

    public void setGear(Gear gear) {
        this.gear = gear;
    }

    @Override
    public String toString() {
        return "Car{" +
                "engine=" + engine +
                ", gear=" + gear +
                '}';
    }
}
