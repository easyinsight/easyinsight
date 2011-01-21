package com.easyinsight.solutions;

import com.easyinsight.goals.GoalTreeDescriptor;

/**
 * User: James Boe
 * Date: Feb 15, 2009
 * Time: 12:00:55 AM
 */
public class SolutionGoalTreeDescriptor extends GoalTreeDescriptor {
    private long solutionID;
    private String solutionName;

    public SolutionGoalTreeDescriptor() {
    }

    public SolutionGoalTreeDescriptor(long goalTreeID, String goalTreeName, int role, long solutionID, String solutionName, String iconImage) {
        // TODO: add urlKey
        super(goalTreeID, goalTreeName, role, iconImage, null, 0);
        this.solutionID = solutionID;
        this.solutionName = solutionName;
    }

    public long getSolutionID() {
        return solutionID;
    }

    public void setSolutionID(long solutionID) {
        this.solutionID = solutionID;
    }

    public String getSolutionName() {
        return solutionName;
    }

    public void setSolutionName(String solutionName) {
        this.solutionName = solutionName;
    }
}
