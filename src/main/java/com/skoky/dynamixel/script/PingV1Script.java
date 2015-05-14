package com.skoky.dynamixel.script;

import com.skoky.dynamixel.Controller;
import com.skoky.dynamixel.Servo;
import com.skoky.dynamixel.controller.OpenCM;
import com.skoky.dynamixel.controller.USB2Dynamixel;
import com.skoky.dynamixel.port.SerialPortFactory;
import com.skoky.dynamixel.servo.ServoAX12A;
import com.skoky.dynamixel.servo.ServoXL320;

import java.util.Arrays;
import java.util.List;

/**
 * Created by skoky on 9.5.15.
 */
public class PingV1Script {

    public static void main(String[] args) {


        Controller controller = new USB2Dynamixel(SerialPortFactory.get("/dev/ttyUSB0"));
        System.out.println("Servos:" + Arrays.toString(controller.listServos().toArray()));

       List<Servo> servos = controller.listServos();
        Servo servo = servos.get(0);
        int model = servo.getModelNumber();
        System.out.println("Model number:" + model);

        int position = servo.getPresentPosition();
        System.out.println("Position:" + position);

        servo.setPresentPosition(1);

        position = servo.getPresentPosition();
        System.out.println("Position:" + position);

    }
}
