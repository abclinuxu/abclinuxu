package cz.abclinuxu.data.view;

import java.util.List;
import java.util.ArrayList;

/**
 * Holder for information about one comment marked as solution.
 * User: literakl
 * Date: 31.8.2009
 */
public final class Solution {
    int id;
    List<Integer> voters;

    /**
     * Constructs new solution
     * @param id id of the comment
     */
    public Solution(int id) {
        this.id = id;
    }

    /**
     * Gets list of uid of users that marked this comment as solution
     * @return list of user ids
     */
    public List<Integer> getVoters() {
        return voters;
    }

    /**
     * Sets list of uid of users that marked this comment as solution
     * @param voters list of user ids
     */
    public void setVoters(List<Integer> voters) {
        this.voters = voters;
    }

    /**
     * Adds new voters.
     * @param uid id of user that has voted for this comment
     */
    public void addVoter(int uid) {
        if (voters == null)
            voters = new ArrayList<Integer>(3);
        voters.add(uid);
    }

    /**
     * @return number of votes
     */
    public int getVotes() {
        return voters.size();
    }

    /**
     * @return comment id
     */
    public int getId() {
        return id;
    }

    @Override
    /**
     * Equals is expected to work only on solutions from same question, otherwise there will be unwanted conflicts.
     */
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        Solution solution = (Solution) o;

        return id == solution.id;
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public String toString() {
        return "Solution{" + "id=" + id + ", voters=" + voters + '}';
    }

    public void removeVoter(int uid) {
        if (voters == null)
            return;
        voters.remove(Integer.valueOf(uid));
    }
}
