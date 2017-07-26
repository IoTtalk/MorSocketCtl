package tw.org.cic.dataManage;

import android.media.MediaPlayer;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;

//import tw.org.cic.morsenser_wifi_tool.WiFiMainActivity;

/**
 * Created by wllai on 2016/3/24.
 */
public class DataTransform {
    private static final String TAG = "DataTransform";
    /**
     static short[] commandID = new short[3];
     static short[] sensorID = new short[3];
     static short[] length_H = new short[3];
     static short[] length_L = new short[3];
     static byte[][] allData = new byte[3][2048]; //第1次收到資料
     static byte[][] allData2 = new byte[3][2048]; //第2次收到資料
     static short[] allDataLength = new short[3];
     static short sensorTotalLength = 0;
     private static short checkHeadLost = 0;
     private static boolean[] checkEnd = new boolean[]{true, true, true};
     */
    static short sensorLength = 0;
    static float data[] = new float[9];
    static float data2[] = new float[9];
    public static int receviewCount = 0;
    private static short HeadStarting = 4;

    public static void Decode(byte[] values, int mClient, int rawDataLength) {
        Log.i(TAG, "mClient:" + mClient + " values[0]:" + values[0] + " values[1]:" + values[1]);
        receviewCount++;
        sensorLength = MorSensorParameter.searchSensorLength(MorSensorParameter.sensorlist[mClient]);

        if (values[0] == (byte) 0xA1 && values[1] == MorSensorParameter.MicID) //Mic size
            MicSize(values, mClient);
        else if (values[0] == (byte) 0xA1 && values[1] == MorSensorParameter.SpO2ID) //SpO2 Size
            TransformSpO2_first(mClient, values);
        else if (values[0] == (byte) 0xA1 && values[1] == MorSensorParameter.IRIID) { //IRI Size
            //WiFiMainActivity.exHandler.sendMessage(WiFiMainActivity.exHandler.obtainMessage(WiFiMainActivity.SEND, mClient, MorSensorParameter.IRIID));
        }
        else if (values[0] == (byte) 0x81 && values[1] == MorSensorParameter.POWERID && values[3] == MorSensorParameter.POWER_POWER_ID) { //Power
            data[0] = values[6];
//            SensorViewActivity.DisplaySensorData(MorSensorParameter.POWER_POWER_ID, data, mClient, values);
        } else if (values[0] == (byte) 0x81 && values[1] == MorSensorParameter.POWERID && values[3] == MorSensorParameter.POWER_CHARGING_ID) { //Charging
            data[0] = (short) convertTwoBytesToIntUnsigned(values[5], values[6]);
//            SensorViewActivity.DisplaySensorData(MorSensorParameter.POWER_CHARGING_ID, data, mClient, values);
        } else if (values[0] == (byte) 0xA2 || values[0] == (byte) 0xA3) { //recevice data
            StringBuilder stringBuilder = new StringBuilder(values.length);
            for (byte byteChar : values)
                stringBuilder.append(String.format("%02X", byteChar));

            //check end head
            for (int i = HeadStarting; i < rawDataLength - HeadStarting; i += sensorLength) {
                if (values[i] + values[i + 1] + values[i + 2] + values[i + 3] + values[i + 4] + values[i + 5] != 0)
                    transformSensorData(stringBuilder.toString(), values, i, values[1], mClient);
            }
//            Log.e(TAG, "stringBuilder.toString().length():" + stringBuilder.toString().length() + "\nstringBuilder.toString():" + stringBuilder.toString());
        }
    }

    private static float RedCalibration = 1;
    private static float GreenCalibration = 1;
    private static float BlueCalibration = 1;
    static float a, b, a_p, ReadConter = 0;

    private static void transformSensorData(String rawdata, byte[] values, int i, int sensorID, int viewCount) {
//        Log.d(TAG, "Transform Sensor Data - sensorID:" + sensorID + " viewCount:" + viewCount);
        switch (sensorID) {
            case MorSensorParameter.IMUID:
                //Gryo: value[2][3] / 32.8
                data[0] = convertTwoBytesToShortUnsigned(values[i], values[i+1]) / 32.8f; //Gryo x
                data[1] = convertTwoBytesToShortUnsigned(values[i+2], values[i+3]) / 32.8f; //Gryo y
                data[2] = convertTwoBytesToShortUnsigned(values[i+4], values[i+5]) / 32.8f; //Gryo z
                //Acc: value[8][9] / 4096
                data[3] = convertTwoBytesToShortUnsigned(values[i+6], values[i+7]) / 4096.0f; //Acc x
                data[4] = convertTwoBytesToShortUnsigned(values[i+8], values[i+9]) / 4096.0f; //Acc y
                data[5] = convertTwoBytesToShortUnsigned(values[i+10], values[i+11]) / 4096.0f; //Acc z
                //Mag: value[15][14] / 3.41 / 100 (??:MagZ ???-1)
                data[7] = convertTwoBytesToShortUnsigned(values[i+13], values[i+12]) / 3.41f / 100f; //Mag x
                data[6] = convertTwoBytesToShortUnsigned(values[i+15], values[i+14]) / 3.41f / 100f; //Mag y
                data[8] = convertTwoBytesToShortUnsigned(values[i+17], values[i+16]) / 3.41f / -100f; //Mag z

//                SensorViewActivity.DisplaySensorData(MorSensorParameter.IMUID, data, viewCount, values);
                break;
            case MorSensorParameter.THID:
                data[0] = (float) (convertTwoBytesToIntUnsigned(values[i], values[i + 1]) * 175.72 / 65536.0 - 46.85);
                data[1] = (float) (convertTwoBytesToIntUnsigned(values[i + 2], values[i + 3]) * 125.0 / 65536.0 - 6.0);
//                SensorViewActivity.DisplaySensorData(MorSensorParameter.THID, data, viewCount, values);
                break;
            case MorSensorParameter.UVID:
                data2[0] = (float) (convertTwoBytesToIntUnsigned(values[i + 1], values[i]) / 100.0);
//                SensorViewActivity.DisplaySensorData(MorSensorParameter.UVID, data2, viewCount, values);
                break;
            case MorSensorParameter.ColorID:
                ReadConter++;
                data[0] = values[i + 1] & 0xFF;
                data[1] = values[i + 3] & 0xFF;
                data[2] = values[i + 5] & 0xFF;

                if (ReadConter == 1) {
                    RedCalibration = 255f / data[0];
                    GreenCalibration = 255f / data[1];
                    BlueCalibration = 255f / data[2];
                }

                data[0] *= RedCalibration;
                data[1] *= GreenCalibration;
                data[2] *= BlueCalibration;
                if (data[0] > 255) {
                    data[0] = 255;
                }
                if (data[1] > 255) {
                    data[1] = 255;
                }
                if (data[2] > 255) {
                    data[2] = 255;
                }

//                SensorViewActivity.DisplaySensorData(MorSensorParameter.ColorID, data, viewCount, values);
                break;
            case MorSensorParameter.AlcoholID:
                ReadConter++;
                float Alc_out = convertTwoBytesToIntUnsigned(values[i], values[i + 1]) / 4096.0f * 3.3f;

                //歸零校正
                if (ReadConter == 1) {
                    a = 1f / (1.8f - Alc_out);
                    b = 1.8f * (1f - a);
                }

                a_p = a * Alc_out + b;
                data[2] = a;
                data[3] = b;
                data[4] = a_p;

                //Check Voltage
                data[1] = Alc_out;

                if (Alc_out >= 1.8) { //y = -2.9427*Alc_out^2 + 15.225*Alc_out - 17.551
                    data[0] = -2.9427f * Alc_out * Alc_out + 15.225f * Alc_out - 17.551f;
                } else { //y = 0.1586*Alc_out^2 - 0.1199*Alc_out
                    data[0] = 0.1586f * a_p * a_p - 0.1199f * a_p;
                }
//                SensorViewActivity.DisplaySensorData(MorSensorParameter.AlcoholID, data, viewCount, values);
                break;
            case MorSensorParameter.PIRID:
                data[0] = convertTwoBytesToIntUnsigned(values[i], values[i + 1]) / 4096.0f * 3.3f;
//                SensorViewActivity.DisplaySensorData(MorSensorParameter.PIRID, data, viewCount, values);
                break;
            case MorSensorParameter.SpO2ID:
                TransformSpO2(rawdata, values, i, viewCount);
                break;
            case MorSensorParameter.MicID:
                TransformMic(values, i);
//                SensorViewActivity.DisplaySensorData(MorSensorParameter.MicID, data, viewCount, values);
                break;
            case MorSensorParameter.IRDID:
                float Voltage = convertTwoBytesToIntUnsigned(values[i], values[i + 1]);
                if (Voltage >= 1886)
                    data[0] = 10;
                else if (Voltage >= 1054)
                    data[0] = 20 - (Voltage - 1054) / 832 * 10;
                else if (Voltage >= 768)
                    data[0] = 30 - (Voltage - 768) / 286 * 10;
                else if (Voltage >= 642)
                    data[0] = 40 - (Voltage - 642) / 126 * 10;
                else if (Voltage >= 561)
                    data[0] = 50 - (Voltage - 561) / 81 * 10;
                else if (Voltage >= 512)
                    data[0] = 60 - (Voltage - 512) / 49 * 10;
                else if (Voltage >= 472)
                    data[0] = 70 - (Voltage - 472) / 40 * 10;
                else if (Voltage >= 441)
                    data[0] = 80 - (Voltage - 441) / 31 * 10;
                else if (Voltage >= 414)
                    data[0] = 90 - (Voltage - 414) / 27 * 10;
                else if (Voltage >= 300)
                    data[0] = 150 - (Voltage - 300) / 114 * 60;
                else if (Voltage < 300)
                    data[0] = 150;
//                SensorViewActivity.DisplaySensorData(MorSensorParameter.IRDID, data, viewCount, values);
                break;
            case MorSensorParameter.USDID:
                data[0] = convertTwoBytesToIntUnsigned(values[i], values[i + 1]) / 6.4f * 2.54f;
                Log.e(TAG, "data[0]:"+data[0] +" data:"+ ((float) (values[2] << 8 | values[3] & 0xFF) / 6.4f * 2.54f));
//                SensorViewActivity.DisplaySensorData(MorSensorParameter.USDID, data, viewCount, values);
                break;
            case MorSensorParameter.PRESSURE_LPS_ID:
                data[0] = (float) ((values[i+2] & 0xFF) << 16 | (values[i+1] & 0xFF) << 8 | (values[i] & 0xFF)) / 4096f * 0.75f; //壓力
                data[1] = convertTwoBytesToShortUnsigned(values[i+4], values[i+3]) / 480f + 42.5f; //溫度
//                SensorViewActivity.DisplaySensorData(MorSensorParameter.PRESSURE_LPS_ID, data, viewCount, values);
                break;
            case MorSensorParameter.PRESSURE_BMP_ID:
                ReadConter++;
                float Alc_out2;
                Alc_out2 = (float) (convertTwoBytesToIntUnsigned(values[i], values[i+1]) / 4096.0 * 3.3);
                if (values[2] < 0 && values[3] < 0) //check overflow(ALC_out = 3.3 * raw_data / 4096)
                    Alc_out2 = (float) (convertTwoBytesToIntUnsigned(values[i], values[i+1]) / 4096.0 * 3.3);
                else if (values[2] < 0) //check overflow(ALC_out = 3.3 * raw_data / 4096)
                    Alc_out2 = (float) (convertTwoBytesToIntUnsigned(values[i], values[i+1]) / 4096.0 * 3.3);
                else if (values[3] < 0) //check overflow(ALC_out = 3.3 * raw_data / 4096)
                    Alc_out2 = (float) (convertTwoBytesToIntUnsigned(values[i], values[i+1]) / 4096.0 * 3.3);

                //歸零校正
                if (ReadConter == 1) {
                    a = 1f / (1.8f - Alc_out2);
                    b = 1.8f * (1f - a);
                }
                a_p = a * Alc_out2 + b;

                data[0] = Alc_out2;
                if (data[0] < 0) data[0] = 0;
//                SensorViewActivity.DisplaySensorData(MorSensorParameter.PRESSURE_BMP_ID, data, viewCount, values);
                break;
            case MorSensorParameter.IRIID:
                data[0] = convertTwoBytesToIntUnsigned(values[i], values[i + 1]);
                TransformIRI(values, i, viewCount);
//                SensorViewActivity.DisplaySensorData(MorSensorParameter.IRIID, data, viewCount, values);
                break;
            case MorSensorParameter.COID: //CO = (76.294 * Voltage / 1000 - 508.63) / 1.0424;
                //Voltage = 76.294 * value / 1000000;
                //CO = (Voltage * 1000 - 508.63) / 1.0424
                //CO = (76.294 * value / 1000 - 508.63) / 1.0424
                Voltage = 76.294f * convertTwoBytesToIntUnsigned(values[i], values[i + 1]) / 1000000f;
                data[0] = (Voltage * 1000 - 508.63f) / 1.0424f;
                //=(76.294*HEX2DEC(B3)/1000000)
                //=((76.294*HEX2DEC(B3)/1000)-508.63)/1.0424

//                SensorViewActivity.DisplaySensorData(MorSensorParameter.COID, data, viewCount, values);
                break;
            case MorSensorParameter.CO2ID:
                Voltage = convertTwoBytesToIntUnsigned(values[i], values[i + 1]);
                if (Voltage <= 1800)
                    data[0] = (float) (1.18 * Math.pow(10, 6) * Math.exp(-0.00427 * Voltage));
                else if (Voltage > 1800 && Voltage <= 2200)
                    data[0] = (float) (3.118 * Math.pow(10, 6) * Math.exp(-0.004099 * Voltage));
                else if (Voltage > 2200 && Voltage <= 2700)
                    data[0] = (float) (1.147 * Math.pow(10, 7) * Math.exp(-0.003789 * Voltage));
                else if (Voltage > 2700 && Voltage <= 2900)
                    data[0] = (float) (2.884 * Math.pow(10, 7) * Math.exp(-0.003787 * Voltage));
                else if (Voltage > 2700 && Voltage <= 2900)
                    data[0] = (float) (1.43 * Math.pow(10, 8) * Math.exp(-0.004074 * Voltage));

//                SensorViewActivity.DisplaySensorData(MorSensorParameter.CO2ID, data, viewCount, values);
                break;
        }
    }



    private static void MicSize(byte[] values, int mClient) {
        index = convertTwoBytesToIntUnsigned(values[2], values[3]);
        if (index > filesize) {
            filesize = index;
            wavedata = new byte[filesize * MorSensorParameter.Mic_LENGTH];
            //WiFiMainActivity.exHandler.sendMessage(WiFiMainActivity.exHandler.obtainMessage(WiFiMainActivity.SEND, mClient, MorSensorParameter.MicID));
        }
    }

    static int index;
    static int head;
    static int MicCount = 0;
    static int filesize = 0;
    static byte[] wavedata = null;
    private static ArrayList<Integer> lostlist = new ArrayList<Integer>();

    public static void TransformMic(byte[] values, int i) {
        head = convertTwoBytesToIntUnsigned(values[0], values[1]) & 0xFFFF;
        if (head == 0xA2A4) {
            index = convertTwoBytesToIntUnsigned(values[i], values[i + 1]);
            if (index != MicCount) {
                for (int lost = MicCount; lost < index; lost++)
                    lostlist.add(lost);
                System.arraycopy(values, i + 2, wavedata, index * 16, 16);
                MicCount = index + 1;
            } else {
                System.arraycopy(values, i + 2, wavedata, MicCount * 16, 16);
                MicCount++;
            }

            data[0] = (float) index / (float) filesize * 100f;
            if ((MicCount > filesize) || (MicCount == filesize)) {
                MicCount = 0;
                filesize = 0;
                writeDateTOFile();//write to file
                Log.i(TAG, "write mic file");
            }
//            Log.i(TAG, "filesize:" + filesize + " index:" + index + " MicCount:" + MicCount);
        }

        if (head == 0x13A4) {
            if ((values[1] & 0xff) == (byte) 0xA4) {
                System.arraycopy(values, i - 2, wavedata, (lostlist.get(0)) * 16, 16);
                lostlist.remove(0);
            }
        }
    }

    static String filename; //MIC

    private static void writeDateTOFile() {
        //filename = audiofilefunc.getWavFilePath();
        Log.e(TAG, "writeDateTOFile");
        // new一个byte数组用来存一些字节数据，大小为缓冲区大小
        // byte[] audiodata = new byte[16];
        FileOutputStream fos = null;
        try {
            File file = new File(filename);
            if (file.exists()) {
                file.delete();
            }
            fos = new FileOutputStream(file);// 建立一个可存取字节的文件
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (fos != null) {
            try {
                fos.write(wavedata);
            } catch (IOException e) {
                e.printStackTrace();
            }
            //   }
        }
        try {
            if (fos != null)
                fos.close();// 关闭写入流

            MediaPlayer mp = new MediaPlayer();
            try {
                //                    mp.reset();
                mp.setDataSource(filename);
                mp.prepare();
                mp.start();

            } catch (Exception e) {
                e.printStackTrace();
                Log.e(TAG, e.toString());
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void TransformSpO2_first(int mClient, byte[] rawdata) {
        Log.i(TAG, "SpO2 Initial");
        SpO2count = 0;
        IR = 0;
        RED = 0;
        SpO2Data = 0;
        HeartRateData = 0;
        Red_DC = 0;
        Red_AC = 0;
        IR_DC = 0;
        IR_AC = 0;
        Red_DC_Raw = 0;
        Red_AC_Raw_Max = 0;
        Red_AC_Raw_Min = 0;
        IR_DC_Raw = 0;
        IR_AC_Raw_Max = 0;
        IR_AC_Raw_Min = 0;
        SpO2TotalCount = convertTwoBytesToIntUnsigned(rawdata[2], rawdata[3]);
        RawIRdata = new double[SpO2TotalCount];
        Log.d(TAG, "SpO2TotalCount:" + SpO2TotalCount);

        for (int i = 0; i < 1024; i++) {
            RawIRdata[i] = 0;
        }

       //WiFiMainActivity.exHandler.sendMessage(WiFiMainActivity.exHandler.obtainMessage(WiFiMainActivity.SEND, mClient, MorSensorParameter.SpO2ID));
    }


    static int SpO2count = 0, SpO2TotalCount = 0;
    static int[] LED = new int[4]; //IR IRA RED REDA
    static float IR = 0, RED = 0, SpO2Data = 0, HeartRateData = 0;
    static float Red_DC = 0, Red_AC = 0, IR_DC = 0, IR_AC = 0;
    static float Red_DC_Raw = 0, Red_AC_Raw_Max = 0, Red_AC_Raw_Min = 0;
    static float IR_DC_Raw = 0, IR_AC_Raw_Max = 0, IR_AC_Raw_Min = 0;
    static double[] RawIRdata;
    static ArrayList<Integer> spo2Lost = new ArrayList<Integer>();

    public static void TransformSpO2(String value, byte[] values, int i, int viewCount) {
        int sequence = convertTwoBytesToIntUnsigned(values[i], values[i + 1]);
        if (SpO2TotalCount <= SpO2count)
            return;
        //Lost
        if (sequence != SpO2count)
            spo2Lost.add(SpO2count);

        //LED = SpO2Transform.RawDataToData(SpO2Transform.ASCIItoHex(value, i * 2 + 4));

        data[0] = (float) LED[0] * 1.2f / (float) Math.pow(2, 21); //RED
        data[1] = (float) LED[1] * 1.2f / (float) Math.pow(2, 21); //RED A
        //data[2] = SpO2Transform.FixedToFloatLED(LED[2]); //IR
        //data[3] = SpO2Transform.FixedToFloatLED(LED[3]); //IR A

        Red_DC_Raw += data[0];
        IR_DC_Raw += data[2];

        //first init
        if (SpO2count == 0) {
            Red_AC_Raw_Max = data[0];
            Red_AC_Raw_Min = data[0];
            IR_AC_Raw_Max = data[2];
            IR_AC_Raw_Min = data[2];
        }

        if (Red_AC_Raw_Max < data[0]) {
            Red_AC_Raw_Max = data[0];
        }
        if (Red_AC_Raw_Min > data[0]) {
            Red_AC_Raw_Min = data[0];
        }
        if (IR_AC_Raw_Max < data[2]) {
            IR_AC_Raw_Max = data[2];
        }
        if (IR_AC_Raw_Min > data[2]) {
            IR_AC_Raw_Min = data[2];
        }

        // 均方根、四捨五入小數第五位( round(sqrt(value/count) * x) / x)
        // round:四捨五入 sqrt:開根號 SpO2count:資料數 x:小數第幾位(10的倍數)
        //Red_AC = SpO2Transform.FixedToFloatAC(Red_AC_Raw_Max, Red_AC_Raw_Min);
        //Red_DC = SpO2Transform.FixedToFloatDC(Red_DC_Raw, SpO2count + 1);
        //IR_AC = SpO2Transform.FixedToFloatAC(IR_AC_Raw_Max, IR_AC_Raw_Min);
        //IR_DC = SpO2Transform.FixedToFloatDC(IR_DC_Raw, SpO2count + 1);

        IR = IR_AC / IR_DC;
        RED = Red_AC / Red_DC;

        RawIRdata[SpO2count] = data[2]; //IR
        if (data[2] == 0) {
            RawIRdata[SpO2count] = 0;
        }

        //data[4] = SpO2Data = (int) (SpO2Transform.FixedToFloatSpO2(RED, IR) * 100f) / 100f;
        data[5] = HeartRateData = 0; //Heart Rate
        data[6] = SpO2count;

        if (data[6] == SpO2TotalCount - 1) { //data[0]:RED data[1]:REDA data[2]:IR data[3]:IRA data[4]:SpO2 data[5]:HR data[6]:count
          //  data[5] = HeartRateData = (int) (SpO2Transform.FixedToFloatHeartRate(RawIRdata) * 10f) / 10f;
            Log.d(TAG, "SpO2count:" + data[6] + " SpO2Data:" + SpO2Data + " HeartRateData:" + HeartRateData + " IR:" + IR + " RED:" + RED + " RawIRdata[0]" + RawIRdata[0] + " RawIRdata[1]" + RawIRdata[1]);
        }
//        SensorViewActivity.DisplaySensorData(MorSensorParameter.SpO2ID, data, viewCount, values);
        SpO2count++;
    }

    public static String LostData = "";
    public static int lostCount;
    public static int pixelCount = 0, receviewIRICount = 0;
    public static float[] pixels = new float[4800]; //80*60

    private static void TransformIRI(byte[] values, int i, int viewCount) {
        index = convertTwoBytesToIntUnsigned(values[i], values[i + 1]);
//        Log.i(TAG, "index:" + index + " receviewIRICount:" + receviewIRICount + " values[2]:" + values[2] + " values[3]:" + values[3]);

        if (receviewIRICount == index) {
            for (int j = 0; j < 80; j++) {
                pixels[pixelCount] = convertTwoBytesToIntUnsigned(values[j * 2 + (i + 2)], values[j * 2 + (i + 3)]);
                pixelCount++;
            }
            receviewIRICount++;
        } else {
            for (int jj = 0; jj < 80; jj++) {
                pixels[pixelCount] = 0;
                pixelCount++;
            }
            LostData += (receviewIRICount + " ");
            receviewIRICount++;
        }

//        if (index == 59) //0~59(60packet)
//            SensorViewActivity.DisplaySensorData(MorSensorParameter.IRIID, pixels, viewCount, values);
    }


    private static final int CONTENT_SIZE = 18;
    static byte[] RawCommand = new byte[20];

    public static byte[] checkLengthASCII(int check, String data, byte cmd) {
        byte[] b = data.getBytes(Charset.forName("UTF-8"));
        if (check == 1 && b.length <= CONTENT_SIZE) { //小於18
            RawCommand[0] = cmd;
            RawCommand[1] = 0x01;
            for (int i = 0; i < b.length; i++)
                RawCommand[i + 2] = b[i];
            return RawCommand;
        } else if (check == 1) { //大於18
            RawCommand[0] = cmd;
            RawCommand[1] = 0x01;
            for (int i = 0; i < CONTENT_SIZE; i++)
                RawCommand[i + 2] = b[i];
            return RawCommand;
        } else if (check == 2) { //大於18
            RawCommand[0] = cmd;
            RawCommand[1] = 0x02;
            for (int i = 0; i < b.length - CONTENT_SIZE; i++)
                RawCommand[i + 2] = b[i + CONTENT_SIZE];
            return RawCommand;
        } else {
            return RawCommand;
        }
    }


    public static int convertTwoBytesToIntUnsigned(byte b1, byte b2)      // unsigned
    {
        return (b1 & 0xFF) << 8 | (b2 & 0xFF);
    }

    public static short convertTwoBytesToShortUnsigned(byte b1, byte b2)      // unsigned
    {
        return (short)((b1 & 0xFF) << 8 | (b2 & 0xFF));
    }

    public static int convertTwoBytesToIntSigned(byte b1, byte b2)      //signed
    {
        return (b2 << 8) | (b1 & 0xFF);
    }


    // HexString to Byte[]
    public static byte[] hexToBytes(String hexString) {
        char[] hex = hexString.toCharArray();
        //轉rawData長度減半
        int length = hex.length / 2;
        byte[] rawData = new byte[length];
        for (int i = 0; i < length; i++) {
            //先將hex資料轉10進位數值
            int high = Character.digit(hex[i * 2], 16);
            int low = Character.digit(hex[i * 2 + 1], 16);
            //將第一個值的二進位值左平移4位,ex: 00001000 => 10000000 (8=>128)
            //然後與第二個值的二進位值作聯集ex: 10000000 | 00001100 => 10001100 (137)
            int value = (high << 4) | low;
            //與FFFFFFFF作補集
            if (value > 127)
                value -= 256;
            //最後轉回byte就OK
            rawData[i] = (byte) value;
        }
        return rawData;
    }
}
