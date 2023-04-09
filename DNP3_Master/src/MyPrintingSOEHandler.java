//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

//package com.automatak.dnp3.mock;


import com.automatak.dnp3.AnalogInput;
import com.automatak.dnp3.AnalogOutputStatus;
import com.automatak.dnp3.BinaryInput;
import com.automatak.dnp3.BinaryOutputStatus;
import com.automatak.dnp3.Counter;
import com.automatak.dnp3.DNPTime;
import com.automatak.dnp3.DoubleBitBinaryInput;
import com.automatak.dnp3.FrozenCounter;
import com.automatak.dnp3.HeaderInfo;
import com.automatak.dnp3.IndexedValue;
import com.automatak.dnp3.ResponseInfo;
import com.automatak.dnp3.SOEHandler;

public class MyPrintingSOEHandler implements SOEHandler {
    private static final MyPrintingSOEHandler instance = new MyPrintingSOEHandler();




    public static SOEHandler getInstance() {
        return instance;
    }


    private MyPrintingSOEHandler() {
    }

    public void beginFragment(ResponseInfo info) {
        System.out.println("begin response: " + info.toString());
    }

    public void endFragment(ResponseInfo info) {
        System.out.println("end response: " + info.toString());
    }

    public void processBI(HeaderInfo info, Iterable<IndexedValue<BinaryInput>> values) {
        System.out.println(info);
        values.forEach((meas) -> {
            System.out.println(meas + "Ahoj BinaryInput");
        });
    }

    public void processDBI(HeaderInfo info, Iterable<IndexedValue<DoubleBitBinaryInput>> values) {
        System.out.println(info);
        values.forEach((meas) -> {
            System.out.println(meas + "Ahoj DoubleBinaryInput");
        });
    }

    public void processAI(HeaderInfo info, Iterable<IndexedValue<AnalogInput>> values) {
        String stringOut = "";
        System.out.println(info);
        stringOut = values.toString();
        values.forEach((meas) -> {
            System.out.println(meas + "Ahojx AnalogInput");
        });
    }

    public void processC(HeaderInfo info, Iterable<IndexedValue<Counter>> values) {
        System.out.println(info);
        values.forEach((meas) -> {
            System.out.println(meas + "Ahoj Counter");
        });
    }

    public void processFC(HeaderInfo info, Iterable<IndexedValue<FrozenCounter>> values) {
        System.out.println(info);
        values.forEach((meas) -> {
            System.out.println(meas + "Ahoj FrozenCounter");
        });
    }

    public void processBOS(HeaderInfo info, Iterable<IndexedValue<BinaryOutputStatus>> values) {
        System.out.println(info);
        values.forEach((meas) -> {
            System.out.println(meas + "Ahoj BinaryOutputStatus");
        });
    }

    public void processAOS(HeaderInfo info, Iterable<IndexedValue<AnalogOutputStatus>> values) {
        System.out.println(info);
        values.forEach((meas) -> {
            System.out.println(meas + "Ahoj AnalogOutputStatus");
        });
    }

    public void processDNPTime(HeaderInfo info, Iterable<DNPTime> values) {
        System.out.println(info);
        values.forEach((meas) -> {
            System.out.println(meas);
        });
    }
}
