package cz.abclinuxu.migrate;

import cz.abclinuxu.persistance.SQLTool;
import cz.abclinuxu.persistance.PersistanceFactory;
import cz.abclinuxu.persistance.impl.MySqlPersistance;
import cz.abclinuxu.persistance.versioning.VersioningFactory;
import cz.abclinuxu.persistance.versioning.Versioning;
import cz.abclinuxu.persistance.versioning.VersionInfo;
import cz.abclinuxu.data.Item;
import cz.abclinuxu.data.Relation;
import cz.abclinuxu.utils.freemarker.Tools;

import java.util.List;
import java.util.Iterator;
import java.sql.*;

import org.dom4j.Document;

/**
 * Create first version of existing drivers.
 * User: literakl
 * Date: 29.3.2005
 */
public class CommitDrivers {

    public static void main(String[] args) {
        SQLTool sqlTool = SQLTool.getInstance();
        MySqlPersistance persistance = (MySqlPersistance) PersistanceFactory.getPersistance();
        Versioning versioning = VersioningFactory.getVersioning();

        Connection con = null;
        PreparedStatement statement = null;
        try {
            con = persistance.getSQLConnection();
            statement = con.prepareStatement("update verze set kdy=? where cesta=? and verze=?");

            List drivers = sqlTool.findItemRelationsWithType(Item.DRIVER, null);
            for (Iterator iter = drivers.iterator(); iter.hasNext();) {
                Relation relation = (Relation) iter.next();
                Item item = (Item) persistance.findById(relation.getChild());
                Document document = item.getData();

                System.out.println("Committing driver with id "+relation.getId()+" and name "+Tools.childName(relation));

                String path = Integer.toString(relation.getId());
                String userId = Integer.toString(item.getOwner());
                VersionInfo versionInfo = versioning.commit(document.asXML(), path, userId);

                statement.setTimestamp(1, new Timestamp(item.getUpdated().getTime()));
                statement.setString(2, path);
                statement.setString(3, versionInfo.getVersion());
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            persistance.releaseSQLResources(con, statement, null);
        }
    }
}
