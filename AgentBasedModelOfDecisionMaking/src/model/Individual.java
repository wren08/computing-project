package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Random;
import java.util.UUID;

public class Individual {
	private String id;
	private ArrayList<Token> tokens;

	// An array to contain all the negative fact retracted(allow duplication)
	private ArrayList<Fact> negFactRetracted;

	public Individual() {
		this.tokens = new ArrayList<Token>();
		this.id = UUID.randomUUID().toString();
		this.negFactRetracted = new ArrayList<Fact>();

	}

	public Individual(Fact fact) {
		this.tokens = new ArrayList<Token>();
		this.id = UUID.randomUUID().toString();
		ArrayList<Token> predefinedTokens = new ArrayList<Token>();

		// number of predefined tokens
		int noOfPre = ModelConfig.random.nextInt(ModelConfig.MAX_NO_OF_SAMPLE); // max
																				// pre
																				// token:
																				// 6

		// add only one kind of tokens
		double weight = Math.random() * ModelConfig.MAX_TOKEN_WEIGHT;
		for (int i = 0; i < noOfPre; i++) {
			Token newToken = new Token(fact, weight);
			predefinedTokens.add(newToken);
		}

		this.tokens = predefinedTokens;
		this.negFactRetracted = new ArrayList<Fact>();

	}

	public void addPredefinedTokens(Fact fact) {
		ArrayList<Token> predefinedTokens = new ArrayList<Token>();

		int noOfPre = ModelConfig.random.nextInt(ModelConfig.MAX_NO_OF_SAMPLE); // max
																				// pre
																				// token:
																				// 6

		// add only one kind of tokens
		double weight = Math.random() * ModelConfig.MAX_TOKEN_WEIGHT;
		for (int i = 0; i < noOfPre; i++) {
			// double weight = 6;
			Token newToken = new Token(fact, weight);
			predefinedTokens.add(newToken);
		}

		this.tokens = predefinedTokens;

	}

	// retract misinformation tokens in memory
	public void setRetract(ArrayList<Fact> retractFacts) {
		System.out.println("retracted");
		for (Token token : this.tokens) {
			if (retractFacts.contains(token.getBelongTo())) {
				if (!token.ifRetracted()) {
					token.setRetracted(true);
					token.setWeight(token.getWeight() * ModelConfig.RETRACTION_FACTOR);

				}
			}
		}
	}

	// Retrieve tokens from memory
	public ArrayList<Token> sampleTokens() {
		int sampleNo = ModelConfig.random.nextInt(ModelConfig.MAX_NO_OF_SAMPLE);
		ArrayList<Token> sampledTokens = new ArrayList<Token>();

		// Shuffle
		Collections.shuffle(this.tokens);

		for (int i = 0; i < sampleNo && i < this.tokens.size(); i++) {
			if (sampledTokens.size() < sampleNo) {
				Token sampleToken = new Token(this.tokens.get(i));
				// Token sampleToken = new Token(shuffledTokens.get(i));
				sampledTokens.add(sampleToken);
			} else {
				break;
			}

		}

		return sampledTokens;
	}

	// decide the transmission tokens
	public ArrayList<Token> decideTransmission() {
		ArrayList<Token> sampledTokens = this.sampleTokens();

		return sampledTokens;

	}

	// Decide the attitude towards vaccination
	public double decideAttitude() {
		double score = 0;
		ArrayList<Token> sampledTokens = sampleTokens();

		for (int i = 0; i < sampledTokens.size(); i++) {
			Token sampledToken = sampledTokens.get(i);
			if (sampledToken.getTokenType() == Type.POSITIVE) {
				score += sampledToken.getWeight();
			} else {
				if (sampledToken.ifRetracted()) {
					score += sampledToken.getWeight();
				} else {
					score += -sampledToken.getWeight();
				}
			}
		}

		return score;
	}

	// token with high weight has a low probability been replaced
	public static double computeProbByWeight(double x) {
		return (1 / (1 + Math.pow(Math.E, (x - ModelConfig.MAX_TOKEN_WEIGHT / 2))));
	}

	public void addFactByWeight(ArrayList<Fact> facts) {
		for (Fact fact : facts) {
			ArrayList<Token> newTokens = new ArrayList<Token>();
			int no = (int) Math.ceil(Math.random() * ModelConfig.MAX_TOKENS_ONE_FACT);
			double weight = 12;
			for (int i = 0; i < no; i++) {
				newTokens.add(new Token(fact, weight));
			}
			addTokensByWeight(newTokens);
		}

	}

	// replace tokens by weight
	public void addTokensByWeight(ArrayList<Token> newTokens) {
		int emptySlots = ModelConfig.MAX_NO_OF_TOKENS_IN_HEAD - this.tokens.size();

		for (int i = 0; i < newTokens.size(); i++) {
			Token newToken = new Token(newTokens.get(i));
			if (emptySlots > 0) {
				this.tokens.add(newTokens.get(i));
				emptySlots--;
			} else {
				int idx = ModelConfig.random.nextInt(this.tokens.size());
				Token candidate = this.tokens.get(idx);
				if (Math.random() < this.computeProbByWeight(candidate.getWeight())) {
					newTokens.get(i).setAge(0);
					this.tokens.set(idx, newTokens.get(i));
				}
			}
		}
	}

	// Select one fact from a fact list randomly according to the weight
	public static Token selectOneFactRandomly(ArrayList<Token> list) {
		// Compute the total weight of all items together
		double totalWeight = 0.0;
		for (Token i : list) {
			totalWeight += i.getWeight();
		}

		// Now choose a random item
		int randomIndex = -1;
		double random = Math.random() * totalWeight;
		for (int i = 0; i < list.size(); i++) {
			random -= list.get(i).getWeight();

			if (random <= 0.0) {
				randomIndex = i;
				break;
			}
		}

		if (randomIndex == -1)
			return null;
		else
			return list.get(randomIndex);
	}

	public void addRectractFacts(ArrayList<Fact> retracts) {
		for (Fact fact : retracts) {
			this.negFactRetracted.add(fact);
		}
	}

	public ArrayList<Token> getTokens() {
		return tokens;
	}

	public ArrayList<Token> getPositiveTokens() {
		ArrayList<Token> posTokens = new ArrayList<Token>();
		for (Token token : this.tokens) {
			if (token.getCorresFactType() == Type.POSITIVE) {
				posTokens.add(token);
			}
		}
		return posTokens;

	}

	public ArrayList<Token> getNegativeTokens() {
		ArrayList<Token> negTokens = new ArrayList<Token>();
		for (Token token : this.tokens) {
			if (token.getCorresFactType() == Type.NEGATIVE) {
				negTokens.add(token);
			}
		}
		return negTokens;

	}

	public String getID() {
		return this.id;
	}

}
