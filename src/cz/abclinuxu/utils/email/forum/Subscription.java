/*
 * User: literakl
 * Date: 31.1.2004
 * Time: 20:42:00
 */
package cz.abclinuxu.utils.email.forum;

/**
 * Value holder for one subscribed user.
 */
public class Subscription {
    Integer id;
    String email;

    public Subscription(Integer id, String email) {
        this.id = id;
        this.email = email;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean equals(Object o) {
        if ( this==o ) return true;
        if ( !(o instanceof Subscription) ) return false;

        final Subscription subscription = (Subscription) o;

        if ( !email.equals(subscription.email) ) return false;
        if ( !id.equals(subscription.id) ) return false;

        return true;
    }

    public int hashCode() {
        int result;
        result = id.hashCode();
        result = 29*result+email.hashCode();
        return result;
    }
}
