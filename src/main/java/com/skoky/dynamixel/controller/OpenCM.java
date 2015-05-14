package com.skoky.dynamixel.controller;

import com.skoky.dynamixel.Controller;
import com.skoky.dynamixel.Servo;
import com.skoky.dynamixel.port.SerialPort;
import com.skoky.dynamixel.raw.Packet;
import com.skoky.dynamixel.raw.PacketV1;
import com.skoky.dynamixel.raw.PacketV2;
import com.skoky.dynamixel.servo.ServoAX12A;
import com.skoky.dynamixel.servo.ServoXL320;
import org.apache.commons.codec.binary.Hex;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by skoky on 9.5.15.
 */
public class OpenCM implements Controller {

    private final Packet packet;
    protected final SerialPort port;

    public OpenCM(SerialPort port) {
        packet = new PacketV2();
        this.port = port;
    }

    @Override
    public SerialPort getPort() {
        return port;
    }

    @Override
    public List<Servo> listServos() {

//        for (int i=0;i<20;i++) {
        byte[] ping = packet.buildPing(1);
        byte[] pingResponse = port.sendAndReceive(ping);
        System.out.println("Response:" + Hex.encodeHexString(pingResponse));
        List<PacketV2.Data> responses = packet.parse(pingResponse);
        List<Servo> servos = new ArrayList<>();
        if (responses != null)
            for (PacketV2.Data d : responses) {
                System.out.println(d.toString());

                if (d.params[1] == 85 && d.params[2] == 128)
                    servos.add(new ServoXL320(d.servoId, this));
            }
//        }
//        port.close();
        return servos;
    }

    public enum Protocols {
        V1,
        V2;
    }

    @Override
    public String toString() {
        return "OpenCM{" +
                ", port=" + port +
                '}';
    }
}
