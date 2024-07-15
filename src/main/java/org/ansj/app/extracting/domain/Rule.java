package org.ansj.app.extracting.domain;

import java.util.List;
import java.util.Map;

/**
 * Created by Ansj on 20/09/2017.
 */
public class Rule {
	private List<Token> tokens;
	private Map<String, int[]> groups;
	private Map<String, String> attr;
	private double weight;
	private String ruleStr ;

    /*
	public Rule(String ruleStr ,List<Token> tokens, Map<String, int[]> groups, Map<String, String> attr, double weight) {
		this.ruleStr = ruleStr ;
		this.tokens = tokens;
		this.groups = groups;
		this.attr = attr ;
		this.weight = weight;
	}
     */
	private Rule(Builder builder) {
		this.tokens = builder.tokens;
		this.groups = builder.groups;
		this.attr = builder.attr;
		this.weight = builder.weight;
		this.ruleStr = builder.ruleStr;
	}


	public void setTokens(List<Token> tokens) {
		this.tokens = tokens;
	}

	public void setGroups(Map<String, int[]> groups) {
		this.groups = groups;
	}

	public void setWeight(double weight) {
		this.weight = weight;
	}

	public List<Token> getTokens() {
		return tokens;
	}

	public Map<String, int[]> getGroups() {
		return groups;
	}

	public Map<String, String> getAttr() {
		return attr;
	}

	public double getWeight() {
		return weight;
	}

	public String getRuleStr() {
		return ruleStr;
	}

	public static class Builder {
		private List<Token> tokens;
		private Map<String, int[]> groups;
		private Map<String, String> attr;
		private double weight;
		private String ruleStr;

		public Builder setTokens(List<Token> tokens) {
			this.tokens = tokens;
			return this;
		}

		public Builder setGroups(Map<String, int[]> groups) {
			this.groups = groups;
			return this;
		}

		public Builder setAttr(Map<String, String> attr) {
			this.attr = attr;
			return this;
		}

		public Builder setWeight(double weight) {
			this.weight = weight;
			return this;
		}

		public Builder setRuleStr(String ruleStr) {
			this.ruleStr = ruleStr;
			return this;
		}

		public Rule build() {
			return new Rule(this);
		}
	}

}
