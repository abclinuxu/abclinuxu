/*
 * User: literakl
 * Date: Jan 23, 2002
 * Time: 11:21:54 PM
 * (c)2001-2002 Tinnio
 */
package cz.abclinuxu.transfer;

import cz.abclinuxu.data.*;
import cz.abclinuxu.persistance.Persistance;
import cz.abclinuxu.persistance.PersistanceFactory;

import java.sql.*;
import java.io.*;

import org.dom4j.*;

/**
 * Simple routine for conversion of items from linux hardware tables.
 */
public class TransferItem {

    Connection conHw,conAbc;
    Persistance persistance;

    static {
        try { Class.forName("org.gjt.mm.mysql.Driver"); } catch (Exception e) { e.printStackTrace(); }
    }

    public static void main(String[] args) throws Exception {
        org.apache.log4j.Category.getDefaultHierarchy().disableAll();

        TransferItem transfer = new TransferItem();
        transfer.conAbc = DriverManager.getConnection("jdbc:mysql://localhost/abc?user=literakl&password=lkaretil&useUnicode=true&characterEncoding=ISO-8859-2");
        transfer.conHw = DriverManager.getConnection("jdbc:mysql://localhost/hardware?user=literakl&password=lkaretil&useUnicode=true&characterEncoding=ISO-8859-2");
        transfer.persistance = PersistanceFactory.getPersistance();

        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        int where = 0, item = 0;

        while ( true ) {
            System.out.print("Zadej id polozky: ");
            item = readInt(reader);
            System.out.print("Zadej id kategorie ["+where+"]: ");
            int tmp = readInt(reader);
            if ( tmp!=0 ) where = tmp;
            transfer.transfer(item,where);
        }
    }

    public void transfer(int cislo, int where) throws Exception {
        PreparedStatement statement = conHw.prepareStatement("select jmeno,ikona,pridal,kdy from druh where cislo=?");
        statement.setInt(1,cislo);
        ResultSet resultSet = statement.executeQuery();
        if ( !resultSet.next() ) {
            System.out.println("Polozka "+cislo+" nenalezena!");
            return;
        }
        Item item = createItem(resultSet);
        resultSet.close();

        statement = conHw.prepareStatement("select * from zaznam where predek=?");
        statement.setInt(1,cislo);
        resultSet = statement.executeQuery();
        storeItem(item);

        Relation upper = (Relation) persistance.findById(new Relation(where));
        Relation relation = new Relation(upper.getChild(),item,upper.getId());
        persistance.create(relation);

        while ( resultSet.next() ) {
            Record record = createRecord(resultSet);
            storeRecord(record);
            Relation lower = new Relation(item,record,relation.getId());
            persistance.create(lower);
        }
        resultSet.close();
    }

    public Item createItem(ResultSet resultSet) throws SQLException {
        Document document = DocumentHelper.createDocument();
        Element root = document.addElement("data");
        root.addElement("name").addText(resultSet.getString(1));
        String icon = resultSet.getString(2);
        if ( icon!=null ) root.addElement("icon").addText("/ikony/hardware/"+icon);

        Item item = new Item(0,Item.MAKE);
        item.setData(document);
        item.setOwner(resultSet.getInt(3));
        item.setUpdated(new Date(resultSet.getTimestamp(4).getTime()));
        return item;
    }

    public Record createRecord(ResultSet resultSet) throws SQLException {
        int pridal = resultSet.getInt(3);
        Date when = new Date(resultSet.getTimestamp(4).getTime());
        String  price = getPrice(resultSet.getInt(5));
        String setup = resultSet.getString(6);
        String tech = resultSet.getString(7);
        String identification = resultSet.getString(8);
        String note = resultSet.getString(9);

        Document document = DocumentHelper.createDocument();
        Element root = document.addElement("data");
        root.addElement("price").addText(price);
        if ( setup!=null && setup.length()>0 ) root.addElement("setup").addText(setup);
        if ( tech!=null && tech.length()>0 ) root.addElement("params").addText(tech);
        if ( identification!=null && identification.length()>0 ) root.addElement("identification").addText(identification);
        if ( note!=null && note.length()>0 ) root.addElement("note").addText(note);

        Record record = new Record(0,Record.HARDWARE);
        record.setData(document);
        record.setOwner(pridal);
        record.setUpdated(when);

        return record;
    }

    public String getPrice(int i) {
        switch (i) {
            case 1: return "verylow";
            case 2: return "low";
            case 3: return "good";
            case 4: return "high";
            case 5: return "toohigh";
            default: return "unknown";
        }
    }

    public void storeItem(Item item) throws SQLException {
        PreparedStatement statement = conAbc.prepareStatement("insert into polozka values(?,?,?,?,?)");
        statement.setInt(1,item.getId());
        statement.setInt(2,item.getType());
        statement.setBytes(3,item.getDataAsString().getBytes());
        statement.setInt(4,item.getOwner());
        statement.setTimestamp(5,new Timestamp(item.getUpdated().getTime()));

        statement.executeUpdate();
        com.mysql.jdbc.PreparedStatement mm = (com.mysql.jdbc.PreparedStatement)statement;
        item.setId((int)mm.getLastInsertID());
    }

    public void storeRecord(Record record) throws SQLException {
        PreparedStatement statement = conAbc.prepareStatement("insert into zaznam values(?,?,?,?,?)");
        statement.setInt(1,record.getId());
        statement.setInt(2,record.getType());
        statement.setBytes(3,record.getDataAsString().getBytes());
        statement.setInt(4,record.getOwner());
        statement.setTimestamp(5,new Timestamp(record.getUpdated().getTime()));

        statement.executeUpdate();
        com.mysql.jdbc.PreparedStatement mm = (com.mysql.jdbc.PreparedStatement)statement;
        record.setId((int)mm.getLastInsertID());
    }

    public static int readInt(BufferedReader reader) throws IOException {
        while ( true ) {
            String tmp = reader.readLine();
            if ( tmp==null || tmp.length()==0 ) return 0;
            try {
                return Integer.parseInt(tmp);
            } catch (NumberFormatException e) { System.out.println("Cislo: ");}
        }
    }
}
