/*
 * User: literakl
 * Date: 16.7.2003
 * Time: 19:12:54
 */
package cz.abclinuxu.persistance;

import cz.abclinuxu.data.User;
import org.dom4j.Document;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;

/**
 * This class measures, how much memory DOM4J tree
 * eats.
 */
public class DOM4JSize {

    public static void main(String[] args) throws IOException {
        User user = (User) PersistanceFactory.getPersistance().findById(new User(1));
        String string = user.getDataAsString();
        Document document = user.getData();

        ByteArrayOutputStream bos = new ByteArrayOutputStream(1000);
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(document);
        oos.flush();

        System.out.println("String: "+string);
        System.out.println("String length: "+string.length());
        System.out.println("DOM4J tree size: " + bos.size());
    }
    // DOM4J 1.4
    // <data><personal><sex>man</sex></personal>
    // <communication><email><newsletter>no</newsletter><weekly_summary>no</weekly_summary></email></communication>
    // <profile><photo>/images/faces/leos.jpeg</photo><home_page>http://www.penguin.cz/~literakl/index.html</home_page>
    // <about_myself>Zakladatel portálu AbcLinuxu.Linux pou¾ívám od roku 1995, v roce 1999 jsem zalo¾il slu¾bu
    // Linux Hardware na Penguinovi, tého¾ roku jsem zaèal publikovat linuxové èlánky pøevá¾nì v ComputerWorldu,
    // LinuxWorldu a té¾ na Rootovi. Roku 2002 jsem naprogramoval a zalo¾il tento portál a posléze s partnery i firmu
    // AbcLinuxu s.r.o., která zastøe¹uje provoz portálu a poskytuje øe¹ení a slu¾by zalo¾ené na Linuxu.</about_myself>
    // </profile><settings><emoticons>yes</emoticons><login_cookie_validity>58035200</login_cookie_validity>
    // </settings><system><last_login_date>2003-07-12 21:432003-07-12 22:22</last_login_date></system></data>
    //
    // String length: 952
    // DOM4J tree size: 2673
}
