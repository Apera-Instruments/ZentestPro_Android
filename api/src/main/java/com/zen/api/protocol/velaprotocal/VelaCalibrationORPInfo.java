package com.zen.api.protocol.velaprotocal;

import java.util.Locale;

/**
 * VelaCalibrationORPInfo
 * Data0='C'(0x43): 校正信息(上传), Data1=3: ORP
 *
 * Layout (indexes are DataN):
 *  Data0: 0x43 ('C')
 *  Data1: 0x03 (ORP)
 *
 *  Data2..3 (u16) 日期:
 *      Bit0..4:  day (1..31)
 *      Bit5..8:  month (1..12)
 *      Bit9..14: year (0..63)  -> interpret as (2000 + value)
 *
 *  Data4..5 (u16) 时间:
 *      Bit0..5:  minute (0..59)
 *      Bit6..10: hour   (0..23)
 *
 *  Data6..7 (u16): ORP零点 + 1000  (units: mV)
 *      Decode: zero_mV = (u16 - 1000)
 */
public class VelaCalibrationORPInfo extends VelaCalibrationBaseInfo<VelaCalibrationORPInfo> {

    private int zeroOffset_mV; // decoded (raw-1000)

    @Override protected int expectedGroup() { return 3; }

    @Override
    protected VelaCalibrationORPInfo parseBody(byte[] d) {
        this.zeroOffset_mV = u16(d[6], d[7]) - 1000;
        return this;
    }

    public int getZeroOffset_mV(){ return zeroOffset_mV; }

    @Override public String toString() {
        return String.format(Locale.US,
                "VelaCalibrationORPInfo{time=%s,zeroOffset=%dmV}", getDateTime(), zeroOffset_mV);
    }
}