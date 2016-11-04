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
public class DatabaseDetails{

    String databaseName;
    String[] tables;
    String[][] attributes;

    public DatabaseDetails(){
        this.databaseName = "";
        this.tables = new String[10];
        this.attributes = new String[10][10];
    }

    public DatabaseDetails(String databaseName, String[] tables, String[][] attributes){
        this.databaseName = databaseName;
        this.tables = tables;
        this.attributes = attributes;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    public String[] getTables() {
        return tables;
    }

    public void setTables(String[] tables) {
        this.tables = tables;
    }

    public String[][] getAttributes() {
        return attributes;
    }

    public void setAttributes(String[][] attributes) {
        this.attributes = attributes;
    }



    @Override
    public String toString(){
        String s="";

        s = s+ " |Database name: "+databaseName+" | ";
        for (int i = 0; i < tables.length ; i++) {
            s = s+ " |Table: "+tables[i]+" | ";
            for (int j = 0; j < attributes.length; j++) {
                s= s+ " |Attribute: "+attributes[i][j]+" | " ;

            }

        }

        return s;
    }
}
