package com.ericsson.vodafone.poc.eee.dm;

import java.util.ArrayList;
import java.util.List;

public class Decision {

    private List<Action> actionList = new ArrayList<>();

    public Decision(List<Action> actionList) {
        setActionList(actionList);
    }

    public List<Action> getActionList() {
        return actionList;
    }

    public void setActionList(List<Action> actionList) {
        this.actionList = actionList;
    }

    public void addAction(Action action) {
        actionList.add(action);
    }


}
