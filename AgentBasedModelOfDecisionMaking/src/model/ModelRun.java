package model;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JFrame;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jgrapht.Graphs;
import org.jgrapht.VertexFactory;
import org.jgrapht.generate.ScaleFreeGraphGenerator;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

public class ModelRun {
	public static ArrayList<String> allNodes;
	public static ArrayList<Individual> allIndividuals;
	public static ArrayList<String> affectNodes;
	public static ArrayList<String> retractNodes;
	public static ArrayList<String> MISpreadNodes;

	public static ArrayList<String> forceNodes;
	public static SimpleWeightedGraph network;

	public static int posFacts_inMem;
	public static int negFacts_inMem;
	public static int posAttitude;
	public static int negAttitude;
	public static double totalScores;
	public static XYSeries pos;
	public static XYSeries neg;
	public static XYSeries scores;

	public static void main(String[] args) {
		int posRun = 0;
		int negRun = 0;
		double avgScr = 0;
		for (int j = 0; j < 10; j++) {
			// Initialise
			ArrayList<Fact> retractFacts = new ArrayList<Fact>();
			ArrayList<Fact> misinformation = new ArrayList<Fact>();
			double totScr = 0;
			double allNeg = 0;
			double allPos = 0;
			allIndividuals = new ArrayList<Individual>();
			allNodes = new ArrayList<String>();
			affectNodes = new ArrayList<String>();
			retractNodes = new ArrayList<String>();
			MISpreadNodes = new ArrayList<String>();
			forceNodes = new ArrayList<String>();
			ArrayList<Individual> copiedIndividuals = new ArrayList<Individual>(allIndividuals);

			Fact misinformation1 = new Fact(Type.NEGATIVE);
			Fact misinformation2 = new Fact(Type.NEGATIVE);
			Fact misinformation3 = new Fact(Type.NEGATIVE);
			misinformation.add(misinformation1);
			// misinformation.add(misinformation2);
			// misinformation.add(misinformation3);

			// create a list of nodes
			for (int i = 0; i < ModelConfig.NO_OF_NODES; i++) {
				Individual newIndividual = new Individual();
				allIndividuals.add(newIndividual);
				allNodes.add(newIndividual.getID());
			}

			Collections.shuffle(allNodes);

			// create the network
			network = createNetwork(ModelConfig.NO_OF_NODES);

			// find the forceful nodes
			// forceNodes = findForceNodes(allNodes, network);

			// add predefined tokens
			for (int m = 0; m < forceNodes.size(); m++) {
				Fact prePos = new Fact(Type.POSITIVE);
				findIndividual(forceNodes.get(m)).addPredefinedTokens(prePos);
			}
			for (int n = 0; n < (int) (ModelConfig.START_RATE * allNodes.size()); n++) {
				if (!isForceNode(allNodes.get(n))) {
					Fact prePos = new Fact(Type.POSITIVE);
					findIndividual(allNodes.get(n)).addPredefinedTokens(prePos);
				}
			}

			pos = new XYSeries("pos");
			neg = new XYSeries("neg");
			scores = new XYSeries("scores");
			createChart();
			int tick = 0;
			while (tick < ModelConfig.MAX_TICKS) {

				Collections.shuffle(allIndividuals);
				System.out.println("@Tick " + (tick));

				// set the nodes receiving misinformation
				if (tick == 0) {
					Collections.shuffle(allNodes);

					for (int i = 0; i < Math.ceil(ModelConfig.AFFECT_RATE * allNodes.size()); i++) {
						if (!isForceNode(allNodes.get(i))) {
							findIndividual(allNodes.get(i)).addFactByWeight(misinformation);
						}
					}

				}

				// set the nodes receiving retraction
				if (tick == 10) {
					// retractFacts.add(preNeg);
					retractFacts.add(misinformation1);
					// retractFacts.add(misinformation2);
					// retractFacts.add(misinformation3);
					Collections.shuffle(allNodes);
					for (int i = 0; i < Math.ceil(ModelConfig.RETRACT_RATE * allNodes.size()); i++) {
						findIndividual(allNodes.get(i)).setRetract(retractFacts);
					}
					for (Fact fact : retractFacts) {
						fact.setRetracted(true);
					}
				}

				// set the force nodes
				// if (tick == 50) {
				// forceNodes = findForceNodes(allNodes, network);
				// }

				transmission(misinformation, retractFacts);
				// transmission(newFact);
				if (tick % 1 == 0) {
					posFacts_inMem = 0;
					negFacts_inMem = 0;
					posAttitude = 0;
					negAttitude = 0;
					totalScores = 0;
					updateInfo();
					System.out.println(j);
					pos.add(tick, posAttitude);
					neg.add(tick, negAttitude);
					scores.add(tick, (totalScores / allIndividuals.size()));

				}

				tick++;
				try {
					Thread.currentThread().sleep(500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			if (posAttitude > negAttitude) {
				posRun += 1;
			} else if (posAttitude < negAttitude) {
				negRun += 1;
			}
			avgScr += totalScores / allIndividuals.size();

		}
		System.out.print("Pos run number: ");
		System.out.println(posRun);
		System.out.print("Neg run number: ");
		System.out.println(negRun);
		System.out.print("Avg Scr: ");
		System.out.println(avgScr / 10);

	}

	// scale-free network creation
	private static SimpleWeightedGraph createNetwork(int no_of_nodes) {
		SimpleWeightedGraph scaleFreeGraph = new SimpleWeightedGraph<>(DefaultEdge.class);
		ScaleFreeGraphGenerator<String, DefaultEdge> scaleFreeGenerator = new ScaleFreeGraphGenerator<>(no_of_nodes);
		VertexFactory<String> vFactory = new VertexFactory<String>() {
			private int id = 0;

			public String createVertex() {
				return allNodes.get(id++);
			}
		};
		scaleFreeGenerator.generateGraph(scaleFreeGraph, vFactory, null);
		Object[] vertexSet = scaleFreeGraph.vertexSet().toArray();

		return scaleFreeGraph;
	}

	// transmission rules
	private static void transmission(ArrayList<Fact> misinformation, ArrayList<Fact> retractFacts) {
		ArrayList<Individual> copiedIndividuals = new ArrayList<Individual>(allIndividuals);
		List<Individual> transmissionNodes = new ArrayList<Individual>();

		List<String> neighbours;
		List<String> transNeighbours;
		Individual neighbour;
		Individual currentNode;
		ArrayList<Token> transmissionTokens;
		double no_of_trans = 0.0;

		// choose nodes who will send information to neighbours
		Collections.shuffle(copiedIndividuals);
		transmissionNodes = copiedIndividuals.subList(0,
				(int) Math.ceil(ModelConfig.TRANS_RATE * allIndividuals.size()));

		int transNum = Math.min(transmissionNodes.size(), allIndividuals.size());
		for (int i = 0; i < transNum; i++) {
			currentNode = transmissionNodes.get(i);

			// get transmissions
			transmissionTokens = currentNode.decideTransmission();

			// randomly select neighbours
			neighbours = Graphs.neighborListOf(network, currentNode.getID());
			Collections.shuffle(neighbours);
			no_of_trans = neighbours.size() * ModelConfig.NEIGHBOUR_RATE;
			transNeighbours = neighbours.subList(0, (int) no_of_trans);

			// transmission to neighbours, forceful nodes will not receive the
			// transmission
			if (transNeighbours.size() > 0) {
				if (isForceNode(currentNode.getID())) {
					for (int j = 0; j < transNeighbours.size(); j++) {
						neighbour = findIndividual(transNeighbours.get(j));
						if (!isForceNode(neighbour.getID())) {
							neighbour.addTokensByWeight(transmissionTokens);
							neighbour.setRetract(retractFacts);
						}
					}

				} else {

					for (int j = 0; j < transNeighbours.size(); j++) {
						neighbour = findIndividual(transNeighbours.get(j));
						if (!isForceNode(neighbour.getID())) {
							neighbour.addTokensByWeight(transmissionTokens);
						}

					}
				}
			}
			// currentNode.updateFactsLife();
		}

	}

	private static Individual findIndividual(String id) {
		Individual result = new Individual();
		for (int i = 0; i < allIndividuals.size(); i++) {
			if (allIndividuals.get(i).getID() == id) {
				result = allIndividuals.get(i);
				break;
			}
		}
		return result;

	}

	// ensure if the node is affected by misinformation
	private static boolean isAffectNode(String ID) {
		boolean isAffect = false;
		for (int i = 0; i < affectNodes.size(); i++) {
			if (ID == affectNodes.get(i)) {
				isAffect = true;
				break;
			}
		}

		return isAffect;
	}

	// ensure if the node is affected by retraction
	private static boolean isRetractNode(String ID) {
		boolean isRetract = false;
		for (int i = 0; i < affectNodes.size(); i++) {
			if (ID == affectNodes.get(i)) {
				isRetract = true;
				break;
			}
		}

		return isRetract;
	}

	private static boolean isForceNode(String ID) {
		boolean isForce = false;
		for (int i = 0; i < forceNodes.size(); i++) {
			if (ID == forceNodes.get(i)) {
				isForce = true;
				break;
			}
		}

		return isForce;
	}

	private static ArrayList<String> findForceNodes(ArrayList<String> allNodes, SimpleWeightedGraph network) {
		ArrayList<String> forceNodes = new ArrayList<String>();
		List<Map.Entry<String, Integer>> list = new ArrayList<>();
		HashMap<String, Integer> hashmap = new HashMap<String, Integer>();
		for (int i = 0; i < allNodes.size(); i++) {
			List<String> neighbours = Graphs.neighborListOf(network, allNodes.get(i));
			hashmap.put(allNodes.get(i), neighbours.size());
		}
		for (Map.Entry<String, Integer> entry : hashmap.entrySet()) {
			list.add(entry); // put elements of map in the list
		}

		list.sort(new Comparator<Map.Entry<String, Integer>>() {
			@Override
			public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
				return o2.getValue() - o1.getValue();
			}
		});

		for (int i = 0; i < (int) allNodes.size() * ModelConfig.FORCE_RATE; i++) {
			forceNodes.add(list.get(i).getKey());
		}

		return forceNodes;

	}

	private static void createChart() {
		JFrame window = new JFrame();
		JFrame scoreWindow = new JFrame();
		window.setTitle("Attitude Of Vaccination Opinion");
		window.setSize(ModelConfig.WINDOW_WIDTH, ModelConfig.WINDOW_HEIGHT);
		window.setLayout(new BorderLayout());
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		scoreWindow.setTitle("Attitude Of Vaccination Opinion");
		scoreWindow.setSize(ModelConfig.WINDOW_WIDTH, ModelConfig.WINDOW_HEIGHT);
		scoreWindow.setLayout(new BorderLayout());
		scoreWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// Create the line graph
		XYSeries series = new XYSeries("Desion-Making");
		XYSeriesCollection dataset = new XYSeriesCollection();
		dataset.addSeries(pos);
		dataset.addSeries(neg);

		XYSeries scoreSeries = new XYSeries("Scores Towards Vaccination");
		XYSeriesCollection scoreDataset = new XYSeriesCollection();
		scoreDataset.addSeries(scores);

		JFreeChart chart = ChartFactory.createXYLineChart("Desion-Making", "Tick", "Positive Attitude Portion: %",
				dataset);
		JFreeChart scoreChart = ChartFactory.createXYLineChart("Desion-Making", "Tick", "Scores", scoreDataset);

		// Add the graph to the window
		window.add(new ChartPanel(chart), BorderLayout.CENTER);
		scoreWindow.add(new ChartPanel(scoreChart), BorderLayout.CENTER);

		// Show the window
		scoreWindow.setVisible(true);
		window.setVisible(true);

	}

	private static void updateInfo() {
		// boolean attitude;
		double score = 0;
		for (int i = 0; i < allIndividuals.size(); i++) {
			// System.out.println(allIndividuals.get(i).getID() + ": " +
			// allIndividuals.get(i).getPositiveFacts(true));
			// allIndividuals.get(i).updateWeight();
			// allIndividuals.get(i).updateTokenAge();
			// allIndividuals.get(i).updateWeight();
			System.out.println(
					allIndividuals.get(i).getID() + " Positive no: " + allIndividuals.get(i).getPositiveTokens().size()
							+ " all tokens: " + allIndividuals.get(i).getTokens().size());
			// System.out.println(allIndividuals.get(i).getTokens());
			posFacts_inMem += allIndividuals.get(i).getPositiveTokens().size();
			negFacts_inMem += allIndividuals.get(i).getNegativeTokens().size();
			score = allIndividuals.get(i).decideAttitude();
			totalScores += score;
			System.out.println("Score: " + score);
			if (score > 0) {
				System.out.println(allIndividuals.get(i).getID() + ": Positive");
				posAttitude += 1;
			} else if (score < 0) {
				System.out.println(allIndividuals.get(i).getID() + ": Negative");
				negAttitude += 1;
			}
		}
		System.out.println(String.format("Positive Tokens: %d, Negative Tokens: %d", posFacts_inMem, negFacts_inMem));
		System.out.println(String.format("Positive Attitude: %d, Negative Attitude: %d", posAttitude, negAttitude));
		System.out.println(String.format("Average Scores: %f", (totalScores / allIndividuals.size())));

	}

}
