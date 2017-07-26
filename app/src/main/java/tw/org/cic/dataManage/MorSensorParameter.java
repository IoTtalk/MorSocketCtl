package tw.org.cic.dataManage;

/**
 * Created by wllai on 2016/5/6.
 */
public class MorSensorParameter {

    public static short[] sensorlist = new short[3];

    /**
     * SENSOR ID
     */
    public static final short NFCID = (byte) 0xAC; //0xAC 172
    public static final short WiFiID = (byte) 0xA6; //0xA6
    public static final short CURRENTID = (byte) 0x02; //0x02 2
    public static final short POWERID = (byte) 0xAA; //0xAA 170
    public static final short POWER_POWER_ID = (byte) 0x2C; //0x2C 44
    public static final short POWER_CHARGING_ID = (byte) 0x14; //0x14 20
    public static final short IMUID = (byte) 0xD0; //0xD0
    public static final short THID = (byte) 0x80; //0x80
    public static final short UVID = (byte) 0xC0; //0xC0
    public static final short ColorID = (byte) 0x52; //0x52
    public static final short AlcoholID = (byte) 0xA2; //0xA2
    public static final short PIRID = (byte) 0xA8; //0xA8
    public static final short SpO2ID = (byte) 0xA0; //0xA0
    public static final short MicID = (byte) 0xA4; //0xA4
    public static final short IRDID = (byte) 0xC4; //0xC4
    public static final short USDID = (byte) 0xC6; //0xC6
    public static final short IRIID = (byte) 0xC8; //0xC8
    public static final short COID = (byte) 0x84; //0x84
    public static final short CO2ID = (byte) 0x86; //0x86
    public static final short PRESSURE_BMP_ID = (byte) 0xEE; //0xEE
    public static final short PRESSURE_LPS_ID = (byte) 0x88; //0x88

    public static final short IMU_LENGTH = 18; //Gryo_6 Acc_6 Mag_6
    public static final short TH_LENGTH = 4; //TEMP_2 HUMI_2
    public static final short UV_LENGTH = 2; //UV_2
    public static final short Color_LENGTH = 8; //RED_2 GREEN_2 BLUE_2 ?_2
    public static final short Alcohol_LENGTH = 2; //Alcohol_2
    public static final short PIR_LENGTH = 2; //PIR_2
    public static final short SpO2_LENGTH = 26; //Seq_2 + IR_6 IRA_6 RED_6 REDA_6
    public static final short Mic_LENGTH = 18; //Seq_2 + MIC_16
    public static final short IRI_LENGTH = 162; //Seq_2 + MIC_160
    public static final short CO_LENGTH = 2; //CO_2
    public static final short CO2_LENGTH = 2; //CO2_2
    public static final short USD_LENGTH = 2; //USD_2
    public static final short IRD_LENGTH = 2; //IRD_2
    public static final short PRESSURE_BMP_LENGTH = 2; //Pressure BMP_2
    public static final short PRESSURE_LPS_LENGTH = 5; //Pressure LPS_4

    //Head_8+
    public static final short IMU_TOTAL_LENGTH = 26; //Head_8 + Gryo_6 Acc_6 Mag_6
    public static final short TH_TOTAL_LENGTH = 12; //Head_8 + TEMP_2 HUMI_2
    public static final short UV_TOTAL_LENGTH = 10; //Head_8 + UV_2
    public static final short Color_TOTAL_LENGTH = 16; //Head_8 + RED_2 GREEN_2 BLUE_2 ?_2
    public static final short Alcohol_TOTAL_LENGTH = 10; //Head_8 + Alcohol_2
    public static final short PIR_TOTAL_LENGTH = 10; //Head_8 + PIR_2
    public static final short SpO2_TOTAL_LENGTH = 1412; //Head_8 + (54count)Seq_2 + IR_6 IRA_6 RED_6 REDA_6
    public static final short Mic_TOTAL_LENGTH = 1412; //Head_8 + (78count)Seq_2 + MIC_16
    public static final short IRI_TOTAL_LENGTH = 170; //Head_8 + (60count)Seq_2 + MIC_160
    public static final short CO_TOTAL_LENGTH = 10; //Head_8 + CO_2
    public static final short CO2_TOTAL_LENGTH = 10; //Head_8 + CO2_2
    public static final short USD_TOTAL_LENGTH = 10; //Head_8 + USD_2
    public static final short IRD_TOTAL_LENGTH = 10; //Head_8 + IRD_2
    public static final short PRESSURE_BMP_TOTAL_LENGTH = 10; //Head_8 + Pressure BMP_2
    public static final short PRESSURE_LPS_TOTAL_LENGTH = 13; //Head_8 + Pressure LPS_4
    public static final short WiFi_TOTAL_LENGTH = 0; //0x__

    /**
     * BLUETOOTH LOW ENERGY ---IN---
     */
    /* sensor data and status report */
    public static final int IN_ECHO = (byte) 0x01; //0x01
    public static final int IN_SENSOR_LIST = (byte) 0x02; //0x02 Internal use
    public static final int IN_MORSENSOR_VERSION = (byte) 0x03; //0x03 Internal use
    public static final int IN_FIRMWARE_VERSION = (byte) 0x04; //0x04 Internal use
    public static final int IN_OLD_FIRMWARE_VERSION = (byte) 0x05; //0x05 Internal use

    public static final int IN_SENSOR_VERSION = (byte) 0x11; //0x11
    public static final int IN_REGISTER_CONTENT = (byte) 0x12; //0x12 Internal use
    public static final int IN_LOST_SENSOR_DATA = (byte) 0x13; //0x13
    public static final int IN_TRANSMISSION_MODE = (byte) 0x14; //0x14 Internal use

    public static final int IN_SET_TRANSMISSION_MODE = (byte) 0x21; //0x21
    public static final int IN_STOP_TRANSMISSION = (byte) 0x22; //0x22
    public static final int IN_SET_REGISTER_CONTENT = (byte) 0x23; //0x23

    public static final int IN_MODIFY_LED_STATE = (byte) 0x31; //0x31
    public static final int IN_IN_MCU_LED_D2 = (byte) 0x01; //0x01
    public static final int IN_IN_MCU_LED_D3 = (byte) 0x02; //0x02
    public static final int IN_IN_COLOR_SENSOR_LED = (byte) 0x03; //0x03

    public static final int IN_SET_AP_SSID = (byte) 0x41; //0x41
    public static final int IN_SET_AP_PASSWORD = (byte) 0x42; //0x42
    public static final int IN_SET_WIFI_SERVER_IP = (byte) 0x43; //0x43
    public static final int IN_SET_TCP_CONNECTION = (byte) 0x44; //0x44
    public static final int IN_IN_SET_CLOSE = (byte) 0x00; //0x00
    public static final int IN_IN_SET_OPEN = (byte) 0x01; //0x01

    public static final int IN_BLE_FILE_DATA_SIZE = (byte) 0xF1; //0xF1
    public static final int IN_BLE_FILE_DATA = (byte) 0xF2; //0xF2
    public static final int IN_BLE_SENSOR_DATA = (byte) 0xF3; //0xF3
    public static final int IN_ERROR = (byte) 0xE1; //0xE1

    /**
     * BLUETOOTH LOW ENERGY ---OUT---
     */
    /* output commands */
    public static final int OUT_ECHO = (byte) 0x01; //0x01
    public static final int OUT_SENSOR_LIST = (byte) 0x02; //0x02 Internal use
    public static final int OUT_MORSENSOR_VERSION = (byte) 0x03; //0x03 Internal use
    public static final int OUT_FIRMWARE_VERSION = (byte) 0x04; //0x04 Internal use
    public static final int OUT_SENSOR_VERSION = (byte) 0x11; //0x11
    public static final int OUT_REGISTER_CONTENT = (byte) 0x12; //0x12 Internal use
    public static final int OUT_LOST_SENSOR_DATA = (byte) 0x13; //0x13
    public static final int OUT_TRANSMISSION_MODE = (byte) 0x14; //0x14 Internal use
    public static final int OUT_SET_TRANSMISSION_MODE = (byte) 0x21; //0x21
        //Third level commands for 0x21
        public static final int OUT_OUT_SET_TRANSMISSION_SINGLE = (byte) 0x00; //0x00
        public static final int OUT_OUT_SET_TRANSMISSION_CONTINUOUS = (byte) 0x01; //0x01
    public static final int OUT_STOP_TRANSMISSION = (byte) 0x22; //0x22
    public static final int OUT_SET_REGISTER_CONTENT = (byte) 0x23; //0x23
    public static final int OUT_STOP_WIFI_TRANSMISSION = (byte) 0x24; //0x24
    public static final int OUT_MODIFY_LED_STATE = (byte) 0x31; //0x31
        //Second level commands for 0x31
        public static final int OUT_OUT_MODIFY_MCU_LED_D2 = (byte) 0x01; //0x01
        public static final int OUT_OUT_MODIFY_MCU_LED_D3 = (byte) 0x02; //0x02
        public static final int OUT_OUT_MODIFY_COLOR_SENSOR_LED = (byte) 0x03; //0x03
            //Third level commands for 0x01~0x03
            public static final int OUT_OUT_OUT_MODIFY_LED_OFF = (byte) 0x00; //0x00
            public static final int OUT_OUT_OUT_MODIFY_LED_ON = (byte) 0x01; //0x01
    public static final int OUT_SET_AP_SSID = (byte) 0x41; //0x41
    public static final int OUT_SET_AP_PASSWORD = (byte) 0x42; //0x42
    public static final int OUT_SET_WIFI_SERVER_IP = (byte) 0x43; //0x43
    public static final int OUT_SET_TCP_CONNECTION = (byte) 0x44; //0x44
        public static final int OUT_OUT_SET_CLOSE = (byte) 0x00; //0x00
        public static final int OUT_OUT_SET_OPEN = (byte) 0x01; //0x01
    public static final int OUT_BLE_FILE_DATA_SIZE = (byte) 0xF1; //0xF1
    public static final int OUT_BLE_FILE_DATA = (byte) 0xF2; //0xF2
    public static final int OUT_BLE_SENSOR_DATA = (byte) 0xF3; //0xF3

    /**
     * WIFI ---IN---
     */
    public static final int IN_WIFI_FILE_DATA_SIZE = (byte) 0xA1; //0xA1
    public static final int IN_WIFI_FILE_DATA = (byte) 0xA2; //0xA2
    public static final int IN_WIFI_SENSOR_DATA = (byte) 0xA3; //0xA3


    /**
     * WIFI ---OUT---
     */
    public static final int OUT_WIFI_FILE_DATA_SIZE = (byte) 0xA1; //0xA1
    public static final int OUT_WIFI_FILE_DATA = (byte) 0xA2; //0xA2
    public static final int OUT_WIFI_SENSOR_DATA = (byte) 0xA3; //0xA3


    /**
     * SEND COMMAND ID(BLE)
     */
    public static final int NULL_COMMAND = 200;
    public static final int SEND_NEW_FIRMWAVE_VERSION = 0;
    public static final int SEND_OLD_FIRMWAVE_VERSION = 1;
    public static final int SEND_MORSENSOR_ID = 2;
    public static final int SEND_MORSENSOR_VERSION = 3;
    public static final int SEND_MORSENSOR_BLE_SENSOR_DATA = 4;
    public static final int SEND_MORSENSOR_BLE_SENSOR_DATA_UV = 5;
    public static final int SEND_MORSENSOR_COLOR_LED_ON = 6;
    public static final int SEND_MORSENSOR_SPO2_LED_ON = 7;
    public static final int SEND_MORSENSOR_BLE_STOP_TRANSMISSION = 7;
    public static final int SEND_MORSENSOR_REGISTER = 106;
    public static final int SEND_MORSENSOR_BLE_FILE_DATA = 107;
    public static final int SEND_MORSENSOR_BLE_FILE_DATA_SIZE = 108;
    public static final int SEND_MORSENSOR_POWER_PERCENTAGE = 109;
    public static final int SEND_MORSENSOR_POWER_CHARGING = 110;

    public static final int SEND_MORSENSOR_WIFI_STOP_TRANSMISSION = 200;
    public static final int SEND_MORSENSOR_SET_AP_SSID = 201;
    public static final int SEND_MORSENSOR_SET_AP_SSID_2 = 202;
    public static final int SEND_MORSENSOR_SET_AP_PASSWORD = 203;
    public static final int SEND_MORSENSOR_SET_AP_PASSWORD_2 = 204;
    public static final int SEND_MORSENSOR_SET_WIFI_SERVER_IP = 205;
    public static final int SEND_MORSENSOR_SET_TCP_CONNECTION_ON = 206;
    public static final int SEND_MORSENSOR_SET_TCP_CONNECTION_OFF = 207;

    /**
     * SEND COMMAND ID(WIFI)
     */
    public static final int SEND_MORSENSOR_WIFI_FILE_DATA = (byte) 0xA1;
    public static final int SEND_MORSENSOR_WIFI_FILE_DATA_SIZE = (byte) 0xA2;
    public static final int SEND_MORSENSOR_WIFI_SENSOR_DATA = (byte) 0xA3;

    public static short searchSensor(short[] sensorList) {
//        if (sensorList.length == 4 || sensorList.length == 3) return IMUID;
        for (short sensor : sensorList) {
            switch (sensor) {
                case AlcoholID:
                    return AlcoholID;
                case ColorID:
                    return ColorID;
                case THID:
                    return THID;
                case UVID:
                    return UVID;
                case SpO2ID:
                    return SpO2ID;
                case MicID:
                    return MicID;
                case PIRID:
                    return PIRID;
                case CO2ID:
                    return CO2ID;
                case COID:
                    return COID;
                case IRDID:
                    return IRDID;
                case USDID:
                    return USDID;
                case IRIID:
                    return IRIID;
                case PRESSURE_BMP_ID:
                    return PRESSURE_BMP_ID;
                case PRESSURE_LPS_ID:
                    return PRESSURE_LPS_ID;
            }
        }
        return IMUID;
    }

    public static short searchSensorLength(short sensor) {
        switch (sensor) {
            case MorSensorParameter.IMUID:
                return IMU_LENGTH;
            case MorSensorParameter.THID:
                return TH_LENGTH;
            case MorSensorParameter.UVID:
                return UV_LENGTH;
            case MorSensorParameter.ColorID:
                return Color_LENGTH;
            case MorSensorParameter.AlcoholID:
                return Alcohol_LENGTH;
            case MorSensorParameter.PIRID:
                return PIR_LENGTH;
            case MorSensorParameter.SpO2ID:
                return SpO2_LENGTH;
            case MorSensorParameter.MicID:
                return Mic_LENGTH;
            case MorSensorParameter.IRDID:
                return IRD_LENGTH;
            case MorSensorParameter.IRIID:
                return IRI_LENGTH;
            case MorSensorParameter.USDID:
                return USD_LENGTH;
            case MorSensorParameter.COID:
                return CO_LENGTH;
            case MorSensorParameter.CO2ID:
                return CO2_LENGTH;
            case MorSensorParameter.PRESSURE_BMP_ID:
                return PRESSURE_BMP_LENGTH;
            case MorSensorParameter.PRESSURE_LPS_ID:
                return PRESSURE_LPS_LENGTH;
        }
        return 0;
    }
    /**
     public static String searchSensorName(short[] sensorList) {
     if (sensorList.length == 1) return "0xD0 IMU";
     for (short sensor : sensorList) {
     switch (sensor) {
     case AlcoholID:
     return "0xA2\nAlcohol";
     case ColorID:
     return "0x52\nColor";
     case THID:
     return "0x80\nTemp/Humi";
     case UVID:
     return "0xC0\nUV";
     case SpO2ID:
     return "0xA0\nSpO2";
     }
     }
     return "";
     }

     public static String SensorName_DECtoHEX(short sensor) {
     switch (sensor) {
     case IMUID:
     return "D0";
     case AlcoholID:
     return "A2";
     case ColorID:
     return "52";
     case THID:
     return "80";
     case UVID:
     return "C0";
     case SpO2ID:
     return "A0";
     }
     return "";
     }

     public static short searchSensorTotalLength(short sensor) {
     switch (sensor) {
     case MorSensorParameter.IMUID:
     return IMU_TOTAL_LENGTH;
     case MorSensorParameter.THID:
     return TH_TOTAL_LENGTH;
     case MorSensorParameter.UVID:
     return UV_TOTAL_LENGTH;
     case MorSensorParameter.ColorID:
     return Color_TOTAL_LENGTH;
     case MorSensorParameter.AlcoholID:
     return Alcohol_TOTAL_LENGTH;
     case MorSensorParameter.PIRID:
     return PIR_TOTAL_LENGTH;
     case MorSensorParameter.SpO2ID:
     return SpO2_TOTAL_LENGTH;
     case MorSensorParameter.MicID:
     return Mic_TOTAL_LENGTH;
     case MorSensorParameter.IRDID:
     return IRD_TOTAL_LENGTH;
     case MorSensorParameter.IRIID:
     return IRI_TOTAL_LENGTH;
     case MorSensorParameter.USDID:
     return USD_TOTAL_LENGTH;
     case MorSensorParameter.COID:
     return CO_TOTAL_LENGTH;
     case MorSensorParameter.CO2ID:
     return CO2_TOTAL_LENGTH;
     }
     return 0;
     }
     */
}
