package org.ansj.app.extracting;

import org.ansj.app.extracting.domain.Rule;
import org.ansj.app.extracting.domain.Token;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class ExtractingTaskTest {

    /*
    @Test
    public void getIndex() {
        List<Token> tokens = new ArrayList<>();
        Rule rule = new Rule("a", tokens, null, null, 1.0);
        ExtractingTask extractingTask = new ExtractingTask(null, rule, 1, null);
        System.out.println(extractingTask.getIndex());
        Assert.assertEquals(extractingTask.getIndex(), 1);
    }
     */

    @Test
    public void getIndex() {
        List<Token> tokens = new ArrayList<>();
        Rule rule = new Rule.Builder()
                .setRuleStr("a")
                .setTokens(tokens)
                .setGroups(null)
                .setAttr(null)
                .setWeight(1.0)
                .build();
        ExtractingTask extractingTask = new ExtractingTask(null, rule, 1, null);
        System.out.println(extractingTask.getIndex());
        Assert.assertEquals(extractingTask.getIndex(), 1);
    }

}
