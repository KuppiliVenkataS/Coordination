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

import org.xml.sax.InputSource;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import com.thoughtworks.xstream.*;
import com.thoughtworks.xstream.io.xml.DomDriver;
import com.thoughtworks.xstream.io.xml.StaxDriver;

/**
 * Created by santhilata on 17/2/16.
 */
public class ConfigurationClass {


    NetworkConfiguration networkConfiguration;
    DistributedEnvironment distributedEnvironment;


    public ConfigurationClass(){
        this.distributedEnvironment = new DistributedEnvironment();
        this.networkConfiguration = new NetworkConfiguration();
    }

    public ConfigurationClass(NetworkConfiguration networkConfiguration, DistributedEnvironment distributedEnvironment) {
        this.networkConfiguration = networkConfiguration;
        this.distributedEnvironment = distributedEnvironment;

    }

    public NetworkConfiguration getNetworkConfiguration() {
        return networkConfiguration;
    }

    public void setNetworkConfiguration(NetworkConfiguration networkConfiguration) {
        this.networkConfiguration = networkConfiguration;
    }

    public DistributedEnvironment getDistributedEnvironment() {
        return distributedEnvironment;
    }

    public void setDistributedEnvironment(DistributedEnvironment distributedEnvironment) {
        this.distributedEnvironment = distributedEnvironment;
    }



    public static String formatXml(String xml){

        try{
            Transformer serializer = SAXTransformerFactory.newInstance().newTransformer();

            serializer.setOutputProperty(OutputKeys.INDENT, "yes");
            serializer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

            Source xmlSource = new SAXSource(new InputSource(new ByteArrayInputStream(xml.getBytes())));
            StreamResult res =  new StreamResult(new ByteArrayOutputStream());

            serializer.transform(xmlSource, res);

            return new String(((ByteArrayOutputStream)res.getOutputStream()).toByteArray());

        }catch(Exception e){
            return xml;
        }
    }


    @Override
    public String toString(){

        String s =networkConfiguration.toString()+distributedEnvironment.toString();
        return  s;

    }
}
