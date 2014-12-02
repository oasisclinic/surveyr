package models;

import org.jongo.MongoCollection;
import uk.co.panaxiom.playjongo.PlayJongo;

import java.util.Random;

public class PinCode {

    private static final MongoCollection collection = PlayJongo.getCollection("pins");
    private static final int minPinCode = 1000;
    private static final int maxPinCode = 9999;

    private int pinCode;

    public PinCode generate() {
        do {
            this.pinCode = new Random().nextInt((maxPinCode - minPinCode) + 1) + minPinCode;
        } while (exists(pinCode));
        return this;
    }

    public static boolean exists(int pin) {
        return collection.findOne("{pinCode: #}", pin).as(PinCode.class) != null;
    }

    public PinCode save() {
        collection.save(this);
        return this;
    }

    public static void remove(int pin) {
        collection.remove("{pinCode: #}", pin);
    }

    public int getPinCode() {
        return pinCode;
    }

}
