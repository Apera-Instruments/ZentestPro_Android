package com.zen.api.protocol.velaprotocal;

import java.util.Locale;

/**
 * VelaCalibrationPhInfo
 * Data0='C'(0x43): 校正信息(上传) → Data1=1: pH
 *
 * Layout (indexes are DataN):
 *  Data0: 0x43 ('C')
 *  Data1: 0x01 (pH)
 *  Data2..3 (u16): 日期
 *      Bit0..4:  day (1..31)
 *      Bit5..8:  month (1..12)
 *      Bit9..14: year (0..63) → interpret as (2000 + value)
 *  Data4..5 (u16): 时间
 *      Bit0..5:  minute (0..59)
 *      Bit6..10: hour   (0..23)
 *  Data6:
 *      Bit6..5: buffer standard (0=中国,1=USA,2=NIST)
 *      Bit4..0: flags for calibration points (bit0:1.68, bit1:4.00, bit2:7.00, bit3:10.01, bit4:12.45)
 *  Data7..8 (u16): calibration temperature in 0.1°C
 *  Data9..10 (u16): pH_offset0 * 10 + 1000
 *  Data11..12 (u16): pH_slope0  * 1000
 *  Data13..14 (u16): pH_offset1 * 10 + 1000
 *  Data15..16 (u16): pH_slope1  * 1000
 */
public class VelaCalibrationPhInfo extends VelaCalibrationBaseInfo<VelaCalibrationPhInfo> {

    public enum BufferStd { CN, USA, NIST, UNKNOWN }

    private BufferStd bufferStd;
    private boolean pH_1_68, pH_4_00, pH_7_00, pH_10_01, pH_12_45;
    private int pointsCount;
    private double tempC;
    private double offset0, slope0, offset1, slope1;

    @Override protected int expectedGroup() { return 1; }

    @Override
    protected VelaCalibrationPhInfo parseBody(byte[] d) {
        int b6 = u8(d[6]);
        int std = (b6 >> 5) & 0x3;
        this.bufferStd = (std==0)?BufferStd.CN : (std==1)?BufferStd.USA : (std==2)?BufferStd.NIST : BufferStd.UNKNOWN;

        this.pH_1_68  = (b6 & (1<<0))!=0;
        this.pH_4_00  = (b6 & (1<<1))!=0;
        this.pH_7_00  = (b6 & (1<<2))!=0;
        this.pH_10_01 = (b6 & (1<<3))!=0;
        this.pH_12_45 = (b6 & (1<<4))!=0;
        this.pointsCount = (pH_1_68?1:0)+(pH_4_00?1:0)+(pH_7_00?1:0)+(pH_10_01?1:0)+(pH_12_45?1:0);

        this.tempC = u16(d[7], d[8]) / 10.0;

        int off0 = u16(d[9],  d[10]);
        int slp0 = u16(d[11], d[12]);
        int off1 = u16(d[13], d[14]);
        int slp1 = u16(d[15], d[16]);

        this.offset0 = (off0 - 1000) / 10.0;
        this.slope0  =  slp0 / 1000.0;
        this.offset1 = (off1 - 1000) / 10.0;
        this.slope1  =  slp1 / 1000.0;

        return this;
    }

    // getters / pretty
    public BufferStd getBufferStd(){ return bufferStd; }
    public boolean has1_68(){ return pH_1_68; }
    public boolean has4_00(){ return pH_4_00; }
    public boolean has7_00(){ return pH_7_00; }
    public boolean has10_01(){ return pH_10_01; }
    public boolean has12_45(){ return pH_12_45; }
    public int getPointsCount(){ return pointsCount; }
    public double getTempC(){ return tempC; }
    public double getOffset0(){ return offset0; }
    public double getSlope0(){ return slope0; }
    public double getOffset1(){ return offset1; }
    public double getSlope1(){ return slope1; }

    @Override public String toString() {
        return String.format(Locale.US,
                "VelaCalibrationPhInfo{time=%s,std=%s,pts=[%s%s%s%s%s],T=%.1f,off0=%.2f,slp0=%.3f,off1=%.2f,slp1=%.3f}",
                getDateTime(), bufferStd,
                pH_1_68?"1.68,":"", pH_4_00?"4.00,":"", pH_7_00?"7.00,":"",
                pH_10_01?"10.01,":"", pH_12_45?"12.45":"",
                tempC, offset0, slope0, offset1, slope1);
    }
}