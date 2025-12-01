package com.zen.api.protocol.velaprotocal;

import java.util.Locale;

/**
 * VelaCalibrationCondInfo
 * Data0='C'(0x43): 校正信息(上传), Data1=2: 电导
 *
 * Frame map (indexes are DataN):
 *  Data0: 0x43 ('C')
 *  Data1: 0x02 (电导)
 *
 *  Data2..3 (u16) 日期:
 *      Bit0..4:  day (1..31)
 *      Bit5..8:  month (1..12)
 *      Bit9..14: year (0..63)  --> interpret as (2000 + value)
 *
 *  Data4..5 (u16) 时间:
 *      Bit0..5:  minute (0..59)
 *      Bit6..10: hour   (0..23)
 *
 *  Data6:
 *      Bit6..5 : 溶液标准 0=中国, 1=USA
 *      Bit2..0 : 校准点 (bit0:84.0, bit1:1413, bit2:12.88)   // per spec (USA order)
 *
 *  Data7..8  (u16): 温度(0.1℃)
 *
 *  Data9 ..10 (u16): 常数1 (0–200 µS)
 *  Data11..12 (u16): 常数2 (200–2000 µS)
 *  Data13..14 (u16): 常数3 (2–20 mS)
 */
public class VelaCalibrationCondInfo extends VelaCalibrationBaseInfo<VelaCalibrationCondInfo> {

    public enum Std { CN, USA, UNKNOWN }

    private Std standard;
    private boolean p84, p1413, p1288;
    private int pointsCount;
    private double tempC;
    private int const1_u16, const2_u16, const3_u16;

    @Override protected int expectedGroup() { return 2; }

    @Override
    protected VelaCalibrationCondInfo parseBody(byte[] d) {
        int b6 = u8(d[6]);
        int std = (b6 >> 5) & 0x3;
        this.standard = (std==0)?Std.CN : (std==1)?Std.USA : Std.UNKNOWN;

        this.p1288 = (b6 & (1<<2))!=0; // 12.88 mS
        this.p1413 = (b6 & (1<<1))!=0; // 1413 µS
        this.p84   = (b6 & (1<<0))!=0; // 84 µS
        this.pointsCount = (p84?1:0)+(p1413?1:0)+(p1288?1:0);

        this.tempC = u16(d[7], d[8]) / 10.0;

        this.const1_u16 = u16(d[9],  d[10]); // 0–200 µS
        this.const2_u16 = u16(d[11], d[12]); // 200–2000 µS
        this.const3_u16 = u16(d[13], d[14]); // 2–20 mS
        return this;
    }

    // getters / pretty
    public Std getStandard(){ return standard; }
    public boolean has84(){ return p84; }
    public boolean has1413(){ return p1413; }
    public boolean has1288(){ return p1288; }
    public int getPointsCount(){ return pointsCount; }
    public double getTempC(){ return tempC; }
    public int getConst1_uS(){ return const1_u16; }
    public int getConst2_uS(){ return const2_u16; }
    public int getConst3_mS(){ return const3_u16; }

    @Override public String toString() {
        return String.format(Locale.US,
                "VelaCalibrationCondInfo{time=%s,std=%s,pts=[%s%s%s],T=%.1f,const1=%dµS,const2=%dµS,const3=%dmS}",
                getDateTime(), standard,
                p84?"84µS,":"", p1413?"1413µS,":"", p1288?"12.88mS":"",
                tempC, const1_u16, const2_u16, const3_u16);
    }
}