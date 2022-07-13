package edu.boun.edgecloudsim.applications.drone_app;

import edu.boun.edgecloudsim.utils.SimLogger;
import weka.classifiers.AbstractClassifier;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.functions.LinearRegression;
import weka.classifiers.functions.MultilayerPerceptron;
import weka.classifiers.functions.SMO;
import weka.classifiers.functions.SMOreg;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

//import org.json.simple.JSONObject;
//import org.json.simple.parser.JSONParser;

public class WekaWrapper {
	public static final double MAX_WLAN_DELAY = 9; // sec
	public static final double MAX_WAN_DELAY = 7; // sec
	public static final double MAX_GSM_DELAY = 8; // sec

	private static ArrayList<double[]> arrays = new ArrayList<double[]>();
	private static String statFilePath;

	private static final String[] EDGE_REGRESSION_ATTRIBUTES = { "TaskLength", "AvgEdgeUtilization", "ServiceTime" };
	private static double[] EDGE_REGRESSION_MEAN_VALS = new double[2];
	private static double[] EDGE_REGRESSION_STD_VALS = new double[2];

	private static final String[] EDGE_CLASSIFIER_ATTRIBUTES = { "NumOffloadedTask", "TaskLength", "WLANUploadDelay",
			"WLANDownloadDelay", "AvgEdgeUtilization" };
	private static double[] EDGE_CLASSIFIER_MEAN_VALS = new double[5];
	private static double[] EDGE_CLASSIFIER_STD_VALS = new double[5];

	private static final String[] DRONE_REGRESSION_ATTRIBUTES = { "TaskLength", "AvgDroneUtilization", "ServiceTime" };
	private static double[] DRONE_REGRESSION_MEAN_VALS = new double[2];
	private static double[] DRONE_REGRESSION_STD_VALS = new double[2];

	private static final String[] DRONE_CLASSIFIER_ATTRIBUTES = { "NumOffloadedTask", "TaskLength", "WLANUploadDelay",
			"WLANDownloadDelay", "AvgDroneUtilization" };
	private static double[] DRONE_CLASSIFIER_MEAN_VALS = new double[5];
	private static double[] DRONE_CLASSIFIER_STD_VALS = new double[5];

	private static final String[] CLOUD_RSU_REGRESSION_ATTRIBUTES = { "TaskLength", "WANUploadDelay",
			"WANDownloadDelay", "ServiceTime" };
	private static double[] CLOUD_RSU_REGRESSION_MEAN_VALS = new double[3];
	private static double[] CLOUD_RSU_REGRESSION_STD_VALS = new double[3];

	private static final String[] CLOUD_RSU_CLASSIFIER_ATTRIBUTES = { "NumOffloadedTask", "WANUploadDelay",
			"WANDownloadDelay" };
	private static double[] CLOUD_RSU_CLASSIFIER_MEAN_VALS = new double[3];
	private static double[] CLOUD_RSU_CLASSIFIER_STD_VALS = new double[3];

	private static final String[] CLOUD_GSM_REGRESSION_ATTRIBUTES = { "TaskLength", "GSMUploadDelay",
			"GSMDownloadDelay", "ServiceTime" };
	private static double[] CLOUD_GSM_REGRESSION_MEAN_VALS = new double[3];
	private static double[] CLOUD_GSM_REGRESSION_STD_VALS = new double[3];

	private static final String[] CLOUD_GSM_CLASSIFIER_ATTRIBUTES = { "NumOffloadedTask", "GSMUploadDelay",
			"GSMDownloadDelay" };
	private static double[] CLOUD_GSM_CLASSIFIER_MEAN_VALS = new double[3];
	private static double[] CLOUD_GSM_CLASSIFIER_STD_VALS = new double[3];

	private static final String[] CLASSIFIER_CLASSES = { "fail", "success" };

	private AbstractClassifier classifier_edge, classifier_cloud_rsu, classifier_cloud_gsm, classifier_drone;
	private AbstractClassifier regression_edge, regression_cloud_rsu, regression_cloud_gsm, regression_drone;

	private static WekaWrapper singleton;

	/*
	 * A private Constructor prevents any other class from instantiating.
	 */
	private WekaWrapper() {
		// TODO: add code for calculating mean values and std
	}

	/* Static 'instance' method */
	public static WekaWrapper getInstance() {
		if(singleton == null) {
			singleton = new WekaWrapper();
			readStat();
			return singleton;
		}else{
			return singleton;
		}
		
	}

	private static void readStat() {
//		String dataPath = "";
//		String statFilePath = "";
//		JSONParser parser = new JSONParser();
//		try {
//			Object object = parser.parse(new FileReader(args[0]));
//			JSONObject jsonObject = (JSONObject) object;
//			dataPath = (String) jsonObject.get("sim_result_folder");
//		} catch (Exception e) {
//			e.printStackTrace();
//			System.exit(1);
//		}
		String dataPath = "./sim_results";
		statFilePath = dataPath + "/stat.txt";

		
		arrays.add(EDGE_CLASSIFIER_MEAN_VALS);
		arrays.add(EDGE_CLASSIFIER_STD_VALS);
		arrays.add(EDGE_REGRESSION_MEAN_VALS);
		arrays.add(EDGE_REGRESSION_STD_VALS);

		arrays.add(CLOUD_RSU_CLASSIFIER_MEAN_VALS);
		arrays.add(CLOUD_RSU_CLASSIFIER_STD_VALS);
		arrays.add(CLOUD_RSU_REGRESSION_MEAN_VALS);
		arrays.add(CLOUD_RSU_REGRESSION_STD_VALS);

		arrays.add(CLOUD_GSM_CLASSIFIER_MEAN_VALS);
		arrays.add(CLOUD_GSM_CLASSIFIER_STD_VALS);
		arrays.add(CLOUD_GSM_REGRESSION_MEAN_VALS);
		arrays.add(CLOUD_GSM_REGRESSION_STD_VALS);

		arrays.add(DRONE_CLASSIFIER_MEAN_VALS);
		arrays.add(DRONE_CLASSIFIER_STD_VALS);
		arrays.add(DRONE_REGRESSION_MEAN_VALS);
		arrays.add(DRONE_REGRESSION_STD_VALS);

		try {

			File myObj = new File(statFilePath);
			Scanner myReader = new Scanner(myObj);

			for (int i = 0; i < arrays.size(); i++) {
				double[] tmp = arrays.get(i);
				for (int j = 0; j < tmp.length; j++) {
					tmp[j] = Double.parseDouble(myReader.nextLine());
				}
			}

			myReader.close();
		} catch (FileNotFoundException e) {
			System.out.println("An error occurred.");
			e.printStackTrace();
		}

	}

	/*
	 * Possible Values for ClassifierType: - NaiveBayes - SMO - LibSVM -
	 * MultilayerPerceptron
	 * 
	 * Possible Values for RegressionType: - LinearRegression - SMOreg
	 */
	public void initialize(String ClassifierType, String RegressionType, String ModelFolder) {
		try {
			if (ClassifierType.equals("NaiveBayes")) {
				classifier_edge = (NaiveBayes) weka.core.SerializationHelper.read(ModelFolder + "nb_edge.model");
				classifier_drone = (NaiveBayes) weka.core.SerializationHelper.read(ModelFolder + "nb_drone.model");

				classifier_cloud_rsu = (NaiveBayes) weka.core.SerializationHelper
						.read(ModelFolder + "nb_cloud_rsu.model");
				classifier_cloud_gsm = (NaiveBayes) weka.core.SerializationHelper
						.read(ModelFolder + "nb_cloud_gsm.model");
			} else if (ClassifierType.equals("SMO")) {
				classifier_edge = (SMO) weka.core.SerializationHelper.read(ModelFolder + "smo_edge.model");
				classifier_drone = (SMO) weka.core.SerializationHelper.read(ModelFolder + "smo_drone.model");
				classifier_cloud_rsu = (SMO) weka.core.SerializationHelper.read(ModelFolder + "smo_cloud_rsu.model");
				classifier_cloud_gsm = (SMO) weka.core.SerializationHelper.read(ModelFolder + "smo_cloud_gsm.model");
			} else if (ClassifierType.equals("MultilayerPerceptron")) {
				classifier_edge = (MultilayerPerceptron) weka.core.SerializationHelper
						.read(ModelFolder + "mlp_edge.model");
				classifier_drone = (MultilayerPerceptron) weka.core.SerializationHelper
						.read(ModelFolder + "mlp_drone.model");
				classifier_cloud_rsu = (MultilayerPerceptron) weka.core.SerializationHelper
						.read(ModelFolder + "mlp_cloud_rsu.model");
				classifier_cloud_gsm = (MultilayerPerceptron) weka.core.SerializationHelper
						.read(ModelFolder + "mlp_cloud_gsm.model");
			}

			if (RegressionType.equals("LinearRegression")) {
				regression_edge = (LinearRegression) weka.core.SerializationHelper.read(ModelFolder + "lr_edge.model");
				regression_drone = (LinearRegression) weka.core.SerializationHelper
						.read(ModelFolder + "lr_drone.model");

				regression_cloud_rsu = (LinearRegression) weka.core.SerializationHelper
						.read(ModelFolder + "lr_cloud_rsu.model");
				regression_cloud_gsm = (LinearRegression) weka.core.SerializationHelper
						.read(ModelFolder + "lr_cloud_gsm.model");
			} else if (RegressionType.equals("SMOreg")) {
				regression_edge = (SMOreg) weka.core.SerializationHelper.read(ModelFolder + "smoreg_edge.model");
				regression_drone = (SMOreg) weka.core.SerializationHelper.read(ModelFolder + "smoreg_drone.model");
				regression_cloud_rsu = (SMOreg) weka.core.SerializationHelper
						.read(ModelFolder + "smoreg_cloud_rsu.model");
				regression_cloud_gsm = (SMOreg) weka.core.SerializationHelper
						.read(ModelFolder + "smoreg_cloud_gsm.model");
			}
		} catch (Exception e) {
			SimLogger.printLine("cannot serialize weka objects!");
			System.exit(1);
		}
	}

	public double handleRegression(int targetDatacenter, double[] values) {
		double result = 0;

		try {
			if (targetDatacenter == MyEdgeOrchestrator.EDGE_DATACENTER) {
				Instance data = getRegressionData("edge", values, EDGE_REGRESSION_ATTRIBUTES, EDGE_REGRESSION_MEAN_VALS,
						EDGE_REGRESSION_STD_VALS);
				result = regression_edge.classifyInstance(data);
			} else if (targetDatacenter == MyEdgeOrchestrator.DRONE_DATACENTER) {
				Instance data = getRegressionData("drone", values, DRONE_REGRESSION_ATTRIBUTES,
						DRONE_REGRESSION_MEAN_VALS, DRONE_REGRESSION_STD_VALS);
				result = regression_drone.classifyInstance(data);
			} else if (targetDatacenter == MyEdgeOrchestrator.CLOUD_DATACENTER_VIA_RSU) {
				Instance data = getRegressionData("cloud_rsu", values, CLOUD_RSU_REGRESSION_ATTRIBUTES,
						CLOUD_RSU_REGRESSION_MEAN_VALS, CLOUD_RSU_REGRESSION_STD_VALS);
				result = regression_cloud_rsu.classifyInstance(data);
			} else if (targetDatacenter == MyEdgeOrchestrator.CLOUD_DATACENTER_VIA_GSM) {
				Instance data = getRegressionData("cloud_gsm", values, CLOUD_GSM_REGRESSION_ATTRIBUTES,
						CLOUD_GSM_REGRESSION_MEAN_VALS, CLOUD_GSM_REGRESSION_STD_VALS);
				result = regression_cloud_gsm.classifyInstance(data);
			}
		} catch (Exception e) {
			SimLogger.printLine("cannot handle regression!");
			System.exit(1);
		}

		return result;
	}

	public boolean handleClassification(int targetDatacenter, double[] values) {
		boolean result = false;

		try {
			if (targetDatacenter == MyEdgeOrchestrator.EDGE_DATACENTER) {
				Instance data = getClassificationData("edge", values, EDGE_CLASSIFIER_ATTRIBUTES,
						EDGE_CLASSIFIER_MEAN_VALS, EDGE_CLASSIFIER_STD_VALS);
				result = (classifier_edge.classifyInstance(data) == 1) ? true : false;
			} else if (targetDatacenter == MyEdgeOrchestrator.CLOUD_DATACENTER_VIA_RSU) {
				Instance data = getClassificationData("cloud_rsu", values, CLOUD_RSU_CLASSIFIER_ATTRIBUTES,
						CLOUD_RSU_CLASSIFIER_MEAN_VALS, CLOUD_RSU_CLASSIFIER_STD_VALS);
				result = (classifier_cloud_rsu.classifyInstance(data) == 1) ? true : false;
			} else if (targetDatacenter == MyEdgeOrchestrator.CLOUD_DATACENTER_VIA_GSM) {
				Instance data = getClassificationData("cloud_gsm", values, CLOUD_GSM_CLASSIFIER_ATTRIBUTES,
						CLOUD_GSM_CLASSIFIER_MEAN_VALS, CLOUD_GSM_CLASSIFIER_STD_VALS);
				result = (classifier_cloud_gsm.classifyInstance(data) == 1) ? true : false;
			} else if (targetDatacenter == MyEdgeOrchestrator.DRONE_DATACENTER) {
				Instance data = getClassificationData("drone", values, DRONE_CLASSIFIER_ATTRIBUTES,
						DRONE_CLASSIFIER_MEAN_VALS, DRONE_CLASSIFIER_STD_VALS);
				result = (classifier_drone.classifyInstance(data) == 1) ? true : false;
			}
		} catch (Exception e) {
			SimLogger.printLine("cannot handle classification!");
			System.exit(1);
		}

		return result;
	}

	private Instance getRegressionData(String relation, double[] values, String[] attributes, double[] meanVals,
			double[] stdVals) {
		ArrayList<Attribute> atts = new ArrayList<Attribute>();
		for (int i = 0; i < attributes.length; i++)
			atts.add(new Attribute(attributes[i]));

		Instances dataRaw = new Instances(relation, atts, 0);
		double[] instanceValue1 = new double[dataRaw.numAttributes()];
		for (int i = 0; i < values.length; i++)
			instanceValue1[i] = (values[i] - meanVals[i]) / stdVals[i];

		dataRaw.add(new DenseInstance(1.0, instanceValue1));
		dataRaw.setClassIndex(dataRaw.numAttributes() - 1);

		return dataRaw.get(0);
	}

	public Instance getClassificationData(String relation, double[] values, String[] attributes, double[] meanVals,
			double[] stdVals) {
		ArrayList<Attribute> atts = new ArrayList<Attribute>();
		ArrayList<String> classVal = new ArrayList<String>();
		for (int i = 0; i < CLASSIFIER_CLASSES.length; i++)
			classVal.add(CLASSIFIER_CLASSES[i]);

		for (int i = 0; i < attributes.length; i++)
			atts.add(new Attribute(attributes[i]));

		atts.add(new Attribute("class", classVal));

		Instances dataRaw = new Instances(relation, atts, 0);

		double[] instanceValue1 = new double[dataRaw.numAttributes()];
		for (int i = 0; i < values.length; i++)
			instanceValue1[i] = (values[i] - meanVals[i]) / stdVals[i];

		dataRaw.add(new DenseInstance(1.0, instanceValue1));
		dataRaw.setClassIndex(dataRaw.numAttributes() - 1);

		return dataRaw.get(0);
	}
}
