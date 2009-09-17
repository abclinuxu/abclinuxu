package cz.abclinuxu.utils;

import cz.abclinuxu.persistence.cache.SolutionCache;
import cz.abclinuxu.persistence.SQLTool;
import cz.abclinuxu.persistence.Persistence;
import cz.abclinuxu.persistence.PersistenceFactory;
import cz.abclinuxu.data.view.Solution;
import cz.abclinuxu.data.Item;

import java.util.List;
import java.util.Date;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Utility class for working with comments marked as solution
 * User: literakl
 * Date: 31.8.2009
 */
public class SolutionTool {
    private static Persistence persistence = PersistenceFactory.getPersistence();
    private static SolutionCache cache = SolutionCache.getInstance();
    private static SQLTool sqlTool = SQLTool.getInstance();


    public static List<Solution> get(Item diz) {
        List<Solution> solutions = cache.get(diz);
        if (solutions == null) {
            solutions = sqlTool.getSolutions(diz);
            cache.put(diz, solutions);
        }
        return solutions;
    }

    public static int add(Item diz, int thread, int uid) {
        boolean newSolution = true;
        int votes = 1;

        List<Solution> solutions = new ArrayList(get(diz));
        for (Solution solution : solutions) {
            if (solution.getId() == thread) {
                newSolution = false;
                if (solution.getVoters().contains(uid)) {
                    return -1; // error, user is already there
                } else {
                    solution.addVoter(uid);
                    votes = solution.getVotes();
                }
            }
        }

        if (newSolution) {
            Solution solution = new Solution(thread);
            solution.addVoter(uid);
            solutions.add(solution);

            int count = 0;

            if (diz.getNumeric1() != null)
                count = diz.getNumeric1();

            diz.setNumeric1(count + 1);
            Date updated = diz.getUpdated();
            persistence.update(diz);
            sqlTool.setUpdatedTimestamp(diz, updated);
        }

        sqlTool.insertSolutionVote(diz, thread, uid);
        cache.put(diz, solutions);

        return votes;
    }

    /**
     * Resets cache.
     */
    public static void clearCache() {
        cache.clear();
    }

    public static int remove(Item diz, int thread, int uid) {
        List<Solution> solutions = new ArrayList(get(diz));

        Iterator<Solution> iter = solutions.iterator();
        while (iter.hasNext()) {
            Solution solution = iter.next();
        
            if (solution.getId() == thread) {
                if (!solution.getVoters().contains(uid)) {
                    return -1; // error, the user didn't vote
                } else {
                    int votes;
                    solution.removeVoter(uid);
                    sqlTool.removeSolutionVote(diz, thread, uid);

                    votes = solution.getVotes();

                    if (votes == 0) {
                        iter.remove();

                        diz.setNumeric1(diz.getNumeric1() - 1);
                        Date updated = diz.getUpdated();
                        persistence.update(diz);
                        sqlTool.setUpdatedTimestamp(diz, updated);
                    }

                    cache.put(diz, solutions);
                    return votes;
                }
            }
        }

        return -1;
    }
}
