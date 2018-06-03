package model;

import java.util.UUID;

public class Token implements Comparable<Token> {
	private String id;
	private Fact belongTo;
	private Type tokenType;
	private double weight;
	private boolean isRetracted;
	private double age;

	public Token(Token token) {
		this.id = token.getId();
		this.belongTo = token.getBelongTo();
		this.tokenType = token.getTokenType();
		this.weight = token.getWeight();
		this.isRetracted = token.getRetracted();
		this.age = 0;
	}

	public Token(Fact belongTo, double weight) {
		this.id = UUID.randomUUID().toString();
		this.belongTo = belongTo;
		this.tokenType = belongTo.getType();
		this.weight = weight;
		this.isRetracted = belongTo.ifRetracted();
		this.age = 0;

	}

	public void increaseAgeByOne() {
		this.age++;
	}

	/*
	 * Getter And Setter
	 */
	public String getId() {
		return id;
	}

	public boolean isRetracted() {
		return isRetracted;
	}

	public double getAge() {
		return age;
	}

	public void setAge(double age) {
		this.age = age;
	}

	public boolean getRetracted() {
		return this.isRetracted;
	}

	public void setRetracted(boolean isRetracted) {
		this.isRetracted = isRetracted;
	}

	public boolean ifRetracted() {
		return this.isRetracted;
	}

	public double getWeight() {
		return weight;
	}

	public void setWeight(double weight) {
		this.weight = weight;
	}

	public Type getCorresFactType() {
		return this.belongTo.getType();
	}

	public String getCorresFactId() {
		return this.belongTo.getId();
	}

	public Fact getBelongTo() {
		return belongTo;
	}

	public void setBelongTo(Fact belongTo) {
		this.belongTo = belongTo;
	}

	public Type getTokenType() {
		return tokenType;
	}

	public void setTokenType(Type tokenType) {
		this.tokenType = tokenType;
	}

	@Override
	public int compareTo(Token token) {
		return Double.compare(this.weight, token.weight);
	}

	@Override
	public String toString() {
		return "Token [id=" + id + ", belongTo=" + belongTo + ", tokenType=" + tokenType + ", weight=" + weight
				+ ", isRetracted=" + isRetracted  + ", age=" + age + "]";
	}
}
