/**
 Copyright (C) <2016>  <Santhilata Kuppili Venkata>

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package SimpleGui;

/**
 * Created by santhilata on 17/2/16.
 */
public class NetworkConfiguration {
    private     double cacheSizeinGB;
    private double  LANTransmissionRate;
    private double distCacheToUser;
    private double WANTransmissionRate;
    private double distServerToUser;

    public NetworkConfiguration(){
        this.cacheSizeinGB = 100;
        this.LANTransmissionRate = 10;
        this.distCacheToUser = 0.1;
        this.WANTransmissionRate = 10;
        this.distServerToUser = 100;
    }
    public NetworkConfiguration(double cacheSizeinGB, double LANTransmissionRate,
                                double distCacheToUser, double WANTransmissionRate, double distServerToUser) {
        this.cacheSizeinGB = cacheSizeinGB;
        this.LANTransmissionRate = LANTransmissionRate;
        this.distCacheToUser = distCacheToUser;
        this.WANTransmissionRate = WANTransmissionRate;
        this.distServerToUser = distServerToUser;
    }

    public double getCacheSizeinGB() {
        return cacheSizeinGB;
    }

    public void setCacheSizeinGB(double cacheSizeinGB) {
        this.cacheSizeinGB = cacheSizeinGB;
    }

    public double getLANTransmissionRate() {
        return LANTransmissionRate;
    }

    public void setLANTransmissionRate(double LANTransmissionRate) {
        this.LANTransmissionRate = LANTransmissionRate;
    }

    public double getDistCacheToUser() {
        return distCacheToUser;
    }

    public void setDistCacheToUser(double distCacheToUser) {
        this.distCacheToUser = distCacheToUser;
    }

    public double getWANTransmissionRate() {
        return WANTransmissionRate;
    }

    public void setWANTransmissionRate(double WANTransmissionRate) {
        this.WANTransmissionRate = WANTransmissionRate;
    }

    public double getDistServerToUser() {
        return distServerToUser;
    }

    public void setDistServerToUser(double distServerToUser) {
        this.distServerToUser = distServerToUser;
    }

    @Override
    public String toString(){
        String s = "";

        s = s+" Cache Size in GB: "+ cacheSizeinGB;
        s = s+ " LAN Transmission Rate: "+LANTransmissionRate;
        s = s+ " Distance between cache to user : "+ distCacheToUser;
        s = s+ " WAN Transmission Rate: "+ WANTransmissionRate;
        s = s+ " Distance server to user: "+distServerToUser;
        return  s;
    }
}
