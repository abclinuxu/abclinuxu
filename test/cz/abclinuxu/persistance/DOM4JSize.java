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
    // <about_myself>Zakladatel port�lu AbcLinuxu.Linux pou��v�m od roku 1995, v roce 1999 jsem zalo�il slu�bu
    // Linux Hardware na Penguinovi, t�ho� roku jsem za�al publikovat linuxov� �l�nky p�ev�n� v ComputerWorldu,
    // LinuxWorldu a t� na Rootovi. Roku 2002 jsem naprogramoval a zalo�il tento port�l a posl�ze s partnery i firmu
    // AbcLinuxu s.r.o., kter� zast�e�uje provoz port�lu a poskytuje �e�en� a slu�by zalo�en� na Linuxu.</about_myself>
    // </profile><settings><emoticons>yes</emoticons><login_cookie_validity>58035200</login_cookie_validity>
    // </settings><system><last_login_date>2003-07-12 21:432003-07-12 22:22</last_login_date></system></data>
    //
    // String length: 952
    // DOM4J tree size: 2673
}
