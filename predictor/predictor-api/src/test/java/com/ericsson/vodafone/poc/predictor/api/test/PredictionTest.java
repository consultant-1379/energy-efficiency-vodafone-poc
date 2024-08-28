package com.ericsson.vodafone.poc.predictor.api.test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import com.ericsson.vodafone.poc.predictor.api.exception.PredictionUnavailableException;
import com.ericsson.vodafone.poc.predictor.api.exception.SerieOperationException;
import com.ericsson.vodafone.poc.predictor.api.impl.DomainAgnosticIncidentPrediction;
import com.ericsson.vodafone.poc.predictor.api.utils.Serie;
import com.ericsson.vodafone.poc.predictor.api.utils.Prediction;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class PredictionTest {

	final Integer rawValues[] = {30, 29, 28, 27, 26, 25, 24, 23, 22, 21, 20, 19, 18, 18, 17, 17, 16, 16, 16, 15, 15, 15, 16, 16,
			16, 17, 17, 17, 18, 18, 18, 19, 19, 20, 20, 21, 21, 22, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31,
			32, 33, 34, 35, 36, 37, 38, 40, 42, 44, 46, 48, 50, 52, 54, 56, 58, 60, 62, 64, 66, 68, 70, 72,
			73, 75, 76, 78, 79, 80, 82, 83, 84, 85, 86, 87, 88, 89, 89, 90, 90, 91, 91, 92, 92, 92, 93, 93,
			93, 94, 94, 94, 93, 92, 92, 91, 90, 88, 86, 84, 83, 82, 82, 82, 83, 84, 85, 87, 89, 91, 92, 93,
			94, 95, 96, 96, 96, 96, 96, 95, 95, 94, 93, 92, 91, 90, 89, 88, 87, 86, 85, 84, 83, 82, 81, 80,
			79, 78, 77, 76, 75, 74, 73, 72, 70, 69, 67, 66, 64, 63, 61, 59, 57, 55, 53, 51, 49, 47, 45, 43,
			41, 40, 38, 37, 36, 34, 33, 32, 31};

	private final double anomalyThreshold = 3.5; //This parameter can be fixed independently by the input: the internal logic must consider the reliability measure of the pattern
	private final int randomicity = 10;
	private int historicalImportance = 0;
	private int derivativeDependency = 0;
	private double patternThreshold = 0.07;

    /*@Before
    public void setUp() {

    }*/

	@Test
	public void testPredictionWithValidValues() {

        System.out.println("*** start testPredictionWithValidValues ***");

        Double[] completeSequence = new Double[rawValues.length*3];
        generateKpiSerie(rawValues, randomicity, completeSequence);

        writeToCsvFile(new ArrayList<>(Arrays.asList(completeSequence)), "target/completeSequence.txt");

        Serie scatteredAndRepeatedSerie = null;
        try {
            scatteredAndRepeatedSerie =
                    new Serie<Double>(3, completeSequence.length, (Arrays.asList(completeSequence)), new Date(), 60);
        } catch (SerieOperationException e) {
            System.out.println("ERROR in serie lenght and size");
            e.printStackTrace();
        }

        DomainAgnosticIncidentPrediction predictionEngine = new DomainAgnosticIncidentPrediction("test", scatteredAndRepeatedSerie);
        Serie<Prediction> predictedSerie = null;
        try {
            predictedSerie = predictionEngine.getPrediction();
            List<Prediction> predictionList = predictedSerie.getSerie();
            writeToCsvFile(predictionList, "target/predictedSerie.txt");

            assertEquals(predictionList.size(), rawValues.length);

            for (Prediction prediction: predictionList) {
                System.out.println("Prediction triplet: " + prediction.toString());
            }

        } catch (PredictionUnavailableException e) {
            e.printStackTrace();
        }
	}

	/**
    @Test(expected=HistoryOperationException.class)
    public void testPredictionWithValidValuesButMalformedHistory() throws SerieOperationException {

        System.out.println("*** start testPredictionWithValidValuesButMalformedHistory ***");

        Double[] completeSequence = new Double[rawValues.length*3];
        generateKpiSerie(rawValues, randomicity, completeSequence);

        Double[] completeSequencePlusOneElement = new Double[rawValues.length*3 + 1];
        for(int i=0; i < completeSequence.length; i++) {
            completeSequencePlusOneElement[i] = completeSequence[i];
        }
        completeSequencePlusOneElement[completeSequence.length -1] = completeSequencePlusOneElement[completeSequence.length -2];

        Serie scatteredAndRepeatedSerie =
                    new Serie(3, completeSequencePlusOneElement.length, new ArrayList<Double>(Arrays.asList(completeSequencePlusOneElement)));

    }
    **/
//
//    //All rowValues 0
//    @Test
//    public void testPredictionWithInvalidValues() {
//
//        System.out.println("*** start testPredictionWithInvalidValues ***");
//        PredictorImpl predictorImpl = new PredictorImpl();
//        Double[] completeSequence = new Double[rawValues.length*3];
//
//        generateNullKpiSerie(rawValues, completeSequence);
//        List<Double> prediction = predictorImpl.getPredictionFromHistoricalData(completeSequence, rawValues.length, historicalImportance, derivativeDependency,
//                anomalyThreshold, patternThreshold);
//        assertNull(prediction);
//    }
//    @Test
//    public void testPredictionWithInvalidValuesDefault() {
//
//        System.out.println("*** start testPredictionWithInvalidValuesDefault ***");
//        PredictorImpl predictorImpl = new PredictorImpl();
//        Double[] completeSequence = new Double[rawValues.length*3];
//
//        generateNullKpiSerie(rawValues, completeSequence);
//        List<Double> prediction = predictorImpl.getPrediction(completeSequence, rawValues.length);
//        assertNull(prediction);
//    }
//
//    //Invalid Periodicity
//    @Test
//    public void testPredictionWithInvalidPeriodicity() {
//
//        System.out.println("*** start testPredictionWithInvalidValues ***");
//        PredictorImpl predictorImpl = new PredictorImpl();
//        Double[] completeSequence = new Double[rawValues.length*3];
//
//        generateNullKpiSerie(rawValues, completeSequence);
//        List<Double> prediction = predictorImpl.getPredictionFromHistoricalData(completeSequence, 7, historicalImportance, derivativeDependency,
//                anomalyThreshold, patternThreshold);
//        assertNull(prediction);
//    }
//    @Test
//    public void testPredictionWithInvalidPeriodicityDefault() {
//
//        System.out.println("*** start testPredictionWithInvalidValuesDefault ***");
//        PredictorImpl predictorImpl = new PredictorImpl();
//        Double[] completeSequence = new Double[rawValues.length*3];
//
//        generateNullKpiSerie(rawValues, completeSequence);
//        List<Double> prediction = predictorImpl.getPrediction(completeSequence, 7);
//        assertNull(prediction);
//    }


	//Add noise to the basic series and repeat it for 3 times (3 repetitions) + add anomalies
	private void generateKpiSerie(Integer[] rawValues, int randomicity, Double[] completeSequence)
	{
		Random rand = new Random();
		int anomalia = rand.nextInt(rawValues.length * 3);
		System.out.println("Amonaly inserted in sample " + anomalia);
		for (int i = 0; i < rawValues.length * 3; i++)
		{
			int noise = rand.nextInt(randomicity);
			if (i == anomalia)
			{
				noise = randomicity * 7;
				anomalia = rand.nextInt(rawValues.length * 3);
			}
			Double noisedValue = Double.valueOf(rawValues[i%rawValues.length] + noise + (i/10));
			completeSequence[i] = noisedValue;
			System.out.println("completeSequence["+ i + "] = " + noisedValue);
		}
	}

	private void writeToCsvFile(List<?> sequence, String fileName) {
        BufferedWriter out = null;
        try
        {
            File fileTemp = new File(fileName);
            if (fileTemp.exists()){
                fileTemp.delete();
            }

            FileWriter fstream = new FileWriter(fileName, true); //true tells to append data.
            out = new BufferedWriter(fstream);
            for(int i=0; i < sequence.size()-1; i++) {
                out.write(sequence.get(i).toString() + ",");
            }

            out.write(sequence.get(sequence.size() - 1).toString());
            out.close();
        }
        catch (IOException e)
        {
            System.err.println("Error: " + e.getMessage());
        }

    }
    //Add noise to the basic series and repeat it for 3 times (3 repetitions) + add anomalies
    private static void generateNullKpiSerie(Integer[] rawValues, Double[] completeSequence)
    {
        for (int i = 0; i < rawValues.length * 3; i++)
        {
            completeSequence[i] = Double.valueOf(0);
        }
    }
}
