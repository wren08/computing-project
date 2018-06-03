package model;

import java.util.Random;

public class ModelConfig {
	public static final Random random = new Random();
	public static final int NO_OF_NODES = 500;
	public static final int MAX_TICKS = 100;
	public static final double FORCE_RATE = 0.2;
	public static final double START_RATE = 0.5;
	public static final double AFFECT_RATE = 0.5;
	public static final double RETRACT_RATE = 1;
	public static final double TRANS_RATE = 0.5;
	public static final double NEIGHBOUR_RATE = 0.5;
	
	public static final int MAX_TOKENS_ONE_FACT = 4;
	public static final int MAX_NO_OF_TOKENS_IN_HEAD = 12;
	public static final int MAX_NO_OF_SAMPLE = 6;
	public static final int MAX_NO_OF_PRE_TOKENS = 6;
	public static final double RETRACTION_FACTOR = 0.1;
	public static final int MAX_TOKEN_WEIGHT = 12;
	
	
	// Line-Chart related constant
	public static final int WINDOW_WIDTH = 1920;
	public static final int WINDOW_HEIGHT = 600;
	
}

