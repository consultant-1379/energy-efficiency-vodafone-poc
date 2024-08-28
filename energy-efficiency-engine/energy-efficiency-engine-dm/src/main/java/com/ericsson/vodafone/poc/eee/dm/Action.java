package com.ericsson.vodafone.poc.eee.dm;

import com.ericsson.vodafone.poc.eee.dm.utils.ActionPoint;

public class Action {

    private ActionPoint actionPoint;
    private CommandToApply commandToApply;

    public Action(final ActionPoint actionPoint, final CommandToApply commandToApply) {
        this.actionPoint = actionPoint;
        this.commandToApply = commandToApply;
    }

    public ActionPoint getActionPoint() {
        return actionPoint;
    }
    public void setActionPoint(ActionPoint actionPoint) {
        this.actionPoint = actionPoint;
    }

    public CommandToApply getCommandToApply() {
        return commandToApply;
    }
    public void setCommandToApply(CommandToApply commandToApply) {
        this.commandToApply = commandToApply;
    }
}
