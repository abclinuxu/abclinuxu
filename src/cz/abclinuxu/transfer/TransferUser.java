/*
 * User: literakl
 * Date: Jan 23, 2002
 * Time: 11:21:54 PM
 * (c)2001-2002 Tinnio
 */
package cz.abclinuxu.transfer;

import cz.abclinuxu.data.User;

import java.sql.*;

import org.dom4j.*;

/**
 * Simple routine for conversion of users from linux hardware tables.
 */
public class TransferUser {

    static {
        try { Class.forName("org.gjt.mm.mysql.Driver"); } catch (Exception e) { e.printStackTrace(); }
    }

    /**
     * main routine
     */
    public void transfer(int start) throws Exception {
        Connection conHw = DriverManager.getConnection("jdbc:mysql://localhost/hardware?user=literakl");
        Connection conAbc = DriverManager.getConnection("jdbc:mysql://localhost/abc?user=literakl");

        PreparedStatement statement = conHw.prepareStatement("select cislo,login,jmeno,email from uzivatel where cislo>?");
        statement.setInt(1,start);
        ResultSet resultSet = statement.executeQuery();

        while ( resultSet.next() ) {
            User user = getUser(resultSet);
            try {
                createUser(user,conAbc);
            } catch (Exception e) {
                System.out.println("User not created: " + user);
                e.printStackTrace();
            }
        }
    }

    /**
     * @return User instance initialized from Linux hardware user
     */
    public User getUser(ResultSet resultSet) throws SQLException {
        User user = new User(resultSet.getInt(1));
        user.setLogin(resultSet.getString(2));
        user.setName(resultSet.getString(3));
        user.setEmail(resultSet.getString(4));
        user.setPassword(generatePassword());

        Document document = DocumentHelper.createDocument();
        Element root = document.addElement("data");
        root.addElement("news").addText("yes");
        root.addElement("sex").addText("man");
        root.addElement("ads").addText("yes");
        user.setData(document);

        return user;
    }

    /**
     * Stores user to database in AbcLinuxu format.
     */
    public void createUser(User user, Connection con) throws SQLException {
        PreparedStatement statement = con.prepareStatement("insert into uzivatel values(?,?,?,?,?,?)");
        statement.setInt(1,user.getId());
        statement.setString(2,user.getLogin());
        statement.setString(3,user.getName());
        statement.setString(4,user.getEmail());
        statement.setString(5,user.getPassword());
        statement.setBytes(6,user.getDataAsString().getBytes());
        statement.executeUpdate();
    }

    /**
     * Generates human readable random password. Each password
     * contains from four upto seven characters in range A-Z.
     */
    private String generatePassword() {
        int max = 6;
        StringBuffer sb = new StringBuffer();

        for (int i=0;i<max;i++) {
            char c = (char)('A' + (int)(26*Math.random()));
            sb.append(c);
        }
        return sb.toString();
    }

    public static void main(String[] args) throws Exception {
        org.apache.log4j.Category.getDefaultHierarchy().disableAll();
        TransferUser transfer = new TransferUser();
        if ( args==null || args.length==0 ) {
            System.out.println("Usage: java cz.abclinuxu.transfer.TransferUser last");
            System.out.println("where last is id of user, who has been transfered last time.");
            System.exit(1);
        }
        int last = Integer.parseInt(args[0]);
        transfer.transfer(last);
    }
}
