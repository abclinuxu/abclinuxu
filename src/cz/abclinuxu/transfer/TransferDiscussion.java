/*
 * User: literakl
 * Date: Feb 3, 2002
 * Time: 8:06:45 PM
 * (c)2001-2002 Tinnio
 */
package cz.abclinuxu.transfer;

import cz.abclinuxu.persistance.Persistance;
import cz.abclinuxu.persistance.PersistanceFactory;
import cz.abclinuxu.data.*;

import java.sql.*;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;

public class TransferDiscussion {

    Connection conHw;
    Persistance persistance;

    static {
        try { Class.forName("org.gjt.mm.mysql.Driver"); } catch (Exception e) { e.printStackTrace(); }
    }

    public static void main(String[] args) throws Exception {
        TransferDiscussion transfer = new TransferDiscussion();
        transfer.conHw = DriverManager.getConnection("jdbc:mysql://localhost/hardware?user=literakl&password=lkaretil&useUnicode=true&characterEncoding=ISO-8859-2");
        transfer.persistance = PersistanceFactory.getPersistance();

        transfer.tranfer();
    }

    protected void tranfer() throws Exception {
        Statement statement = conHw.createStatement();
        PreparedStatement prepared = conHw.prepareStatement("select vlakno,kdo,kdy,predmet,obsah from odpovedi where otazka=? order by cislo asc");

        Category tmp = new Category(-1);
        Relation upper = new Relation();
        upper.setParent(tmp);

        ResultSet rs = statement.executeQuery("select * from otazky");
        while (rs.next() ) {
            Item question = getQuestion(rs);

            prepared.setInt(1,question.getId());
            ResultSet rs2 = prepared.executeQuery();

            persistance.create(question);
            upper.setChild(question);
            persistance.create(upper);

            while ( rs2.next() ) {
                Record response = getResponse(rs2);
                persistance.create(response);
                Relation child = new Relation(question,response,upper.getId());
                persistance.create(child);
            }
            rs2.close();
        }
    }

    protected Item getQuestion(ResultSet rs) throws SQLException {
        Item item = new Item(rs.getInt(1),Item.DISCUSSION);
        item.setOwner(rs.getInt(2));
        item.setUpdated(new Date(rs.getTimestamp(3).getTime()));

        Document document = DocumentHelper.createDocument();
        DocumentHelper.makeElement(document, "data/title").setText(rs.getString(4));
        DocumentHelper.makeElement(document, "data/text").setText(FixRecords.fixLines(rs.getString(5)));

        item.setData(document);
        return item;
    }

    protected Record getResponse(ResultSet rs) throws SQLException {
        Record record = new Record(0,Record.DISCUSSION);
        record.setOwner(rs.getInt(2));
        record.setUpdated(new Date(rs.getTimestamp(3).getTime()));

        Document document = DocumentHelper.createDocument();
        DocumentHelper.makeElement(document, "data/title").setText(rs.getString(4));
        DocumentHelper.makeElement(document, "data/text").setText(FixRecords.fixLines(rs.getString(5)));
        DocumentHelper.makeElement(document, "data/old").setText(""+rs.getInt(1));

        record.setData(document);
        return record;
    }
}
