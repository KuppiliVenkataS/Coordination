package project.Network;

import org.omg.CORBA.PUBLIC_MEMBER;

/**
 * Created by santhilata on 15/04/15.
 */
public interface NetworkConstants {
    //LAN constants
     /*
        The following values are typical for a LAN:

            Transmission rate: R = 100 Mbits/second
            Bit transmission time: 1/R = 10 nsec/bit
            Distance: D = 0.1 miles

        Then we have

            dtrans = 1Kbits * 10 ns/bit = 10 µsec
            dprop = 0.1 mi * 10 µsec/mi = 1 µsec

            The queue delay dqueue depends on the load factor.
            If the load factor is less than 0.1 then the queue delay is almost always smaller than the transmission delay.
            If the load factor is less than 0.5 then the queue delay is no more than 2 or 3 times the transmission delay.

            For modern network equipment, the nodal processing delay is insignificant. The above calculations show that the transmission delay
            is the most significant component of the nodal delay for a lightly loaded link, but the queue delay dominates for a heavily loaded link.
         */
    public static final double LAN_TRANSMISSION_RATE = 10000; // millisec for 1GB
    public static final double LAN_PROPAGATION_DELAY = 1000; //millisec
    public static final double LAN_HIGH_LOADFACTOR = 0.5;
    public static final double LAN_LOW_LOADFACTOR = 0.1;
    public static final double LAN_PROCESSING_DELAY = 0.0; // for modern networks this value is not significant



     /**
        The following values are typical for a WAN:

            Transmission rate: R = 10 Gbits/second
            Bit transmission time: 1/R = 0.1 nsec/bit
            Distance: D = 100 miles

            Then we have

                dtrans = 1Kbits * 0.1 ns/bit = 100 nsec
                dprop = 100 mi * 10 µsec/mi = 1 msec

            The queue delay dqueue depends on the load factor, but is small compared to the propagation delay unless the load factor is close to 1.0.

            For modern network equipment, the nodal processing delay is insignificant.
            The above calculations show that the propagation delay is the most significant component of the nodal delay.
         */

    public static final double WAN_TRANSMISSION_RATE = 100000000 ; // millisec for 1GB
    public static final double WAN_PROPAGATION_DELAY = 1; //millisec
    public static final double WAN_HIGH_LOADFACTOR = 1.0;
    public static final double WAN_LOW_LOADFACTOR = 0.1;
    public static final double WAN_PROCESSING_DELAY = 0.0; // for modern networks this value is not significant


    /**
     *  Load limits on LAN and WAN
     */
    public static final double LAN_HEAVYLOAD = 20; //GB
    public static final double LAN_LIGHTLOAD = 0; //GB
    public static final double WAN_HEAVYLOAD = 250; //GB
    public static final double WAN_LIGHTLOAD = 0; //GB
    public static final int LAN_QUERY_LOAD_LIMIT = 100;
    public static final int WAN_QUERY_LOAD_LIMIT =1000;





}
