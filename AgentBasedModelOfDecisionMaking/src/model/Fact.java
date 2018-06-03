package model;

import java.util.UUID;

enum Type {
	POSITIVE, NEGATIVE, RETRACTION
}

public class Fact {
	private String id;
	private Type type;
	private Fact retractOn;
	private boolean isRetracted;

	public Fact(Type type) {
		this.id = UUID.randomUUID().toString();
		this.type = type;
		this.retractOn = null;
		this.isRetracted = false;
	}

	public Fact(Type type, Fact retractOn) {
		this.id = UUID.randomUUID().toString();
		this.type = type;
		this.retractOn = retractOn;
		this.isRetracted = false;
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public String getId() {
		return id;
	}

	public Fact getRetractOn() {
		return retractOn;
	}

	public boolean ifRetracted() {
		return this.isRetracted;
	}

	public void setRetracted(boolean retracted) {
		this.isRetracted = retracted;
	}

	@Override
	public String toString() {
		return "Fact [id=" + id + ", type=" + type + "]";
	}
}
