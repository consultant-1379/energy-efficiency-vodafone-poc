/*
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package com.ericsson.cli.plugin;

import com.google.common.base.Predicate;

public class CliPromptChecker implements Predicate<String> {

    private String postfixPrompt;

    public String getPostfixPrompt() {
        return postfixPrompt;
    }

    public void setPostfixPrompt(String postfixPrompt) {
        this.postfixPrompt = postfixPrompt;
    }

    public CliPromptChecker postfixPrompt(String postfixPrompt) {
        this.postfixPrompt = postfixPrompt;
        return this;
    }

    @Override
    public boolean apply(String input) {
        return input.endsWith(postfixPrompt);
    }
}
