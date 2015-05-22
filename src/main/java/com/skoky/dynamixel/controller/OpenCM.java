package com.skoky.dynamixel.controller;

import com.skoky.dynamixel.Controller;
import com.skoky.dynamixel.Servo;
import com.skoky.dynamixel.err.ResponseParsingException;
import com.skoky.dynamixel.err.SerialLinkError;
import com.skoky.dynamixel.port.SerialPort;
import com.skoky.dynamixel.raw.Data;
import com.skoky.dynamixel.raw.Packet;
import com.skoky.dynamixel.raw.PacketV2;
import com.skoky.dynamixel.servo.LedColor;
import com.skoky.dynamixel.servo.ReturnLevel;
import com.skoky.dynamixel.servo.xl320.ServoXL320;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * Created by skoky on 9.5.15.
 */
public class OpenCM implements Controller {
    Logger log = Logger.getGlobal();

    private final Packet packet;
    protected final SerialPort port;
    public ServosV2 servoList;

    public OpenCM(SerialPort port) {
        packet = new PacketV2();
        this.port = port;
        LogManager.getLogManager().reset();
        ConsoleHandler handler = new ConsoleHandler();
        handler.setLevel(Level.ALL);
        log.setLevel(Level.INFO);
        log.addHandler(handler);
     //   log.severe("Logger created. Level:" + log.getLevel());
    }

    @Override
    public SerialPort getPort() {
        return port;
    }

    @Override
    public List<Servo> listServos() {

        byte[] ping = packet.buildPing();
        byte[] pingResponse;
        List<Servo> servos = new ArrayList<>();
        try {
            pingResponse = port.sendAndReceive(ping,100);
            List<Data> responses = packet.parse(pingResponse);
            if (responses != null)
                for (Data d : responses) {
                    log.fine(d.toString());
                    if (d.params[1] == 1 && d.params[2] == 27)
                        servos.add(new ServoXL320(d.servoId, this));
                    else
                        log.info("Unknown servo on the bus");
                }

        } catch (SerialLinkError serialLinkError) {
            serialLinkError.printStackTrace();
        } catch (ResponseParsingException e) {
            e.printStackTrace();
        }
        return servos;
    }

    @Override
    public boolean resetServos() {
        //TBD
        return false;
    }

    @Override
    public boolean rebootDevice() {
        byte[] rebootRequest = packet.buildReboot();
        port.sendAndReceive(rebootRequest,0);
        return true;
    }

    @Override
    public void setVerbose() {
        log.setLevel(Level.ALL);
    }

    @Override
    public void setServoList(List<Servo> servos) {
        servoList = new ServosV2();
        servoList.setServos(servos, port);

    }

    @Override
    public String toString() {
        return "OpenCM{" +
                ", port=" + port +
                '}';
    }

}
