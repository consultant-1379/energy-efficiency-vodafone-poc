package com.ericsson.vodafone.poc.predictor.pattern.detection;

import java.util.*;

import com.ericsson.vodafone.poc.predictor.kpi.management.KpiSerie;

/**
 * @author egiubur
 * 
 * This code is not normalized nor optimized:
 * It's just a draft implementation to create a base to check the algorithm and to improve it:
 *   - pattern computation by considering as more reliable the most recent historical values (see historicalImportance parameter)
 *   - pattern computation by filtering out any anomaly in historical (training) values (see anomalyThreshold)
 *
 * @param <Double>: is the expected type of historical KPI values (Integer, Double, etc.)
 */

/**
 * @author egiubur
 *
 */
public class PatternDetector {
	private Double reliability = 0.0; // range [0,1]. A �good� pattern has affinity indexes in range (0.1,1]. See "anomalyThreshold" constructor parameter
	private List<Double> pattern = null;
	private List<List<Double>> fixedKPIseries = null;
	private List<Double> normDeviation = null;
	
	// Per each training series
	private List<Double> minValues = null;
	private List<Double> maxValues = null; 
	private List<Double> averageValues = null; 
	private List<Double> weightValues = null; 
	
	public List<Double> getAverageValues() {
		return averageValues;
	}
	
	public List<Double> getGainValues() {
		List<Double> gainValues = new ArrayList<Double>();
		for (int i = 0; i < this.minValues.size(); i++)
			gainValues.add(this.maxValues.get(i) - this.minValues.get(i));
		return gainValues;
	}
	
	public List<Double> getNormDeviation() {
		return normDeviation;
	}
	
	public List<List<Double>> getFixedKPIseries() {
		return fixedKPIseries;
	}
	
	public Double getReliability() {
		return reliability;
	}

	public List<Double> getPattern() {
		return pattern;
	}

	/**
	 * @param historicalKpiData: is the list of KPI values (and timestamp) for the historical time slot of observation (minimum 3 * periodicity values)
	 * @param periodicity: is the number of samples in a period (in a day or in a week, usually)
	 * @param historicalImportance (default 0): is the weight [0...+1] to give less/more importance to the historical data age (weight = 1 + alpha * time):
	 * 				if a KPI is very old, it can be considered less reliable in the pattern computation (historicalImportance higher)
	 * 				if an old KPI should be considered reliable as a new one, historicalImportance should be set to 0
	 * @param derivativeDependency (default 0): is the "C" coefficient [0...+inf) to introduce the standard deviation dependency on the KPI derivative (see formula 7). If you increase this param, the derivative is more important
	 * @param anomalyThreshold (default 4.5): is the coefficient (0..+inf) to classify as anomaly a divergent historical KPI value. If you increase this param, less anomalies are detected
	 */
	public PatternDetector(KpiSerie<Double> historicalKpiData, int periodicity, double historicalImportance, double derivativeDependency, double anomalyThreshold) {
		super();

		if (this.validateInput(historicalKpiData, periodicity, historicalImportance, derivativeDependency, anomalyThreshold))
		{
			System.err.println("Invalid Input");
			return;
		}

		if (this.init(historicalKpiData, periodicity, historicalImportance))
		{
			System.err.println("No pattern at all");
			return;
		}

		if (this.computePattern(historicalKpiData, periodicity, historicalImportance))
		{
			System.err.println("Error in pattern computation");
			return;
		}

		if (this.computeReliability(historicalKpiData, periodicity, historicalImportance, derivativeDependency))
		{
			System.err.println("Error in reliability computation");
			return;
		}

		//Let me remove the anomalies in training series
//		if (this.refinePatternForAnomalies(historicalKpiData, periodicity, historicalImportance, anomalyThreshold))
//		{
//			System.err.println("Error in pattern computation");
//			return;
//		}
	}



	private boolean validateInput (KpiSerie<Double> historicalKpiData, int periodicity, double historicalImportance, double derivativeDependency, double anomalyThreshold )
	{
		//At the moment, the periodicity must be set (not any implementation for the periodicity computation)
		//The expected minimum value is 3 (but it's suggested to have many samples per period)
		if (periodicity <= 3)
		{
			System.err.println("periodicity <= 3");
			return true;
		}
		//The historical data must be N times the periodicity (not any missing data allowed); the data interpolation must be performed outside this class
		if (historicalKpiData.size() % periodicity != 0)
		{
			System.err.println("periodicity doesn't match the historical data size");
			return true;
		}		//In order to compute a reliable pattern, it's required three series (3 weeks or 3 days, at least)
		if (historicalKpiData.size() / periodicity < 3)
		{
			System.err.println("Historical data <= 3 * periodicity");
			return true;
		}		//The derivativeDependency coefficient must be > 0
		if (derivativeDependency > 0)
		{
			System.err.println("Derivative dependency > 0");
			return true;
		}
		return false;
	}

	private boolean init (KpiSerie<Double> historicalKpiData, int periodicity, double historicalImportance)
	{
		this.reliability = 0.0; // range [0,1]. A �good� pattern has affinity indexes in range (0.07,1]. See "anomalyThreshold" constructor parameter
		this.pattern = new ArrayList<Double>();
		this.fixedKPIseries = new ArrayList<List<Double>>();
		this.normDeviation = new ArrayList<Double>();

		this.minValues = new ArrayList<Double>();
		this.maxValues = new ArrayList<Double>();
		this.averageValues = new ArrayList<Double>();
		this.weightValues = new ArrayList<Double>();
		int numberOfRepetitions = historicalKpiData.size() / periodicity;
		for (int i = 0; i < numberOfRepetitions; i++)
		{
			this.minValues.add((double) historicalKpiData.get(i*periodicity).getValue());
			this.maxValues.add((double) historicalKpiData.get(i*periodicity).getValue());
			this.averageValues.add(0.0);
			double x = (double)i-((double)(numberOfRepetitions-1)/2.0);
			double alpha = historicalImportance / ((double)(numberOfRepetitions-1)/2.0);
			this.weightValues.add(1.0 + x * alpha);
		}
		for (int i = 0; i < historicalKpiData.size(); i++) {
			int currentIndex = i/periodicity;
			if ( ((Double) historicalKpiData.get(i).getValue()) < ((Double) this.minValues.get(currentIndex)) )
				this.minValues.set(currentIndex, (Double) historicalKpiData.get(i).getValue());
			if ( ((Double) historicalKpiData.get(i).getValue()) > ((Double) this.maxValues.get(currentIndex)) )
				this.maxValues.set(currentIndex, (Double) historicalKpiData.get(i).getValue());
			this.averageValues.set(currentIndex, this.averageValues.get(currentIndex) + (Double) historicalKpiData.get(i).getValue());
		}
		for (int i = 0; i < numberOfRepetitions; i ++){
			this.averageValues.set(i, this.averageValues.get(i)/(double)periodicity);
			if(minValues.get(i) == maxValues.get(i))
				return true;
		}
		//I don't check the validity of time ticks (the index of input historical data)
		for (int i = 0; i < periodicity; i++)
			this.pattern.add(0.0);
		return false;
	}
	
	
	/**
	 * This method estimates a pattern depending on the historical data. In case of anomalies in the historical data, they
	 *  are not filtered out: this job is performed by the iterative API "refinePatternForAnomalies".
	 * @param historicalKpiData
	 * @param periodicity
	 * @param historicalImportance
	 * @return
	 */
	private boolean computePattern (KpiSerie<Double> historicalKpiData, int periodicity, double historicalImportance)
	{
		int numberOfRepetitions = historicalKpiData.size() / periodicity;
		List<Double> normKPI = null;
		for (int i = 0; i < historicalKpiData.size(); i++)
		{
			int currentrepetition = i/periodicity;
			int currentPatternIndex = i%periodicity;
			double currentMagnitude = this.maxValues.get(currentrepetition) - this.minValues.get(currentrepetition);
			double currentNormalizedKpiValue = (double) historicalKpiData.get(i).getValue() - this.averageValues.get(currentrepetition);
			
			if (currentPatternIndex == 0)
				normKPI = new ArrayList<Double>();
			
			this.pattern.set
			(
				currentPatternIndex, 
				this.pattern.get(currentPatternIndex) + 
					(currentNormalizedKpiValue * this.weightValues.get(currentrepetition) / (currentMagnitude * numberOfRepetitions) )
			); 
			normKPI.add(currentNormalizedKpiValue/currentMagnitude);
			if (((i+1)%periodicity) == 0) //end of this series
				this.fixedKPIseries.add(normKPI);
		}
		return false;
	}
	
	
	/**
	 * This API should be private and used by the "computePattern", but for testing purposes I decided to explicitly call this API from the tester.
	 * This API checks the pattern (previously computed) reliability:
	 *   - if the pattern is not computed, it returns true
	 *   - if the percentage of equal values (usually 0) is more than 50%, it's considered as a "no pattern"
	 *   - otherwise, the reliability factor is dependent on the weighted average of standard deviations for each ponit in time
	 *   
	 * @param historicalKpiData: the historical data on which the pattern is computed
	 * @param periodicity: the expected periodicity of the pattern (1/N of the historicalKPIData entries)
	 * @param historicalImportance: the old historical values can be less important than the most recent. This is the alpha in the weight function w = 1 + alpha * T
	 * @param derivativeDependency: the standard deviation can be considered more or less in case of high derivative of the pattern
	 * @return true in case of errors
	 */
	private boolean computeReliability (KpiSerie<Double> historicalKpiData, int periodicity, double historicalImportance, double derivativeDependency)
	{
		double normalizedStandardDeviation = 0.0;
		double kpiLatestValue = (double) historicalKpiData.get(1).getValue();
		for (int i = 0; i < periodicity; i++)
			this.normDeviation.add(0.0);
		
		Map<Double, Integer> distribution = new HashMap<Double, Integer>();
		for (int i = 0; i < historicalKpiData.size(); i++)
		{
			Double value = historicalKpiData.get(i).getValue();
			if(distribution.containsKey(value))
			{ 
				distribution.put(value, 1+distribution.get(value));
				if(distribution.get(value) > (periodicity/2))
					//Too many constant values... not a reliable pattern
					// TODO ALDO
					return true;
			}
			else
				distribution.put(value, 1);
		}	
		
		for (int i = 0; i < historicalKpiData.size(); i++)
		{
			double derivativeKpiValue = Math.abs((double) historicalKpiData.get(i).getValue() - kpiLatestValue);
			double currNormalizedKpiValue = 
					((double) historicalKpiData.get(i).getValue() - this.averageValues.get(i/periodicity)) / 
					(this.maxValues.get(i/periodicity) - this.minValues.get(i/periodicity));
			double currentNormDeviation = 
				(
					Math.pow(currNormalizedKpiValue - this.pattern.get(i%periodicity), 2) / 
					(1 + derivativeDependency * derivativeKpiValue)
				);
			normalizedStandardDeviation += currentNormDeviation;
			this.normDeviation.set(i%periodicity, this.normDeviation.get(i%periodicity)+currentNormDeviation);
				
			kpiLatestValue = (double) historicalKpiData.get(i).getValue();
		}
				
		this.reliability = 1.0 / (1.0+normalizedStandardDeviation);
		return false;
	}
	
	/**
	 * @param periodicity : the expected periodicity of the pattern (1/N of the historicalKPIData entries)
	 * @param historicalImportance (default 0): the old historical values can be less important than the most recent. This is the alpha in the weight function w = 1 + alpha * T
	 * @param derivativeDependency (default 0): the standard deviation can be considered more or less in case of high derivative of the pattern. It defines how much the anomalies can be accepted in presence of high derivative (high variation of the series)
	 * @param anomalyThreshold (default 4.5): is the coefficient (0..+inf) to classify as anomaly a divergent historical KPI value. If you increase this param, less anomalies are detected
	 * @return true if it detects an anomaly (and refines the pattern)
	 */
	public boolean refinePatternForAnomalies(int periodicity, double historicalImportance, double derivativeDependency, double anomalyThreshold)
	{
		if (this.pattern == null)
			return false;
		
		boolean bFoundAnomalousValues = false;
		
		double averageDeviation = 0;
		for (int i = 0; i < this.normDeviation.size(); i++)
		{
			averageDeviation += (this.normDeviation.get(i) / this.normDeviation.size());
		}
		
		//Attenuate any anomaly (recursive attenuation supported)
		KpiSerie<Double> historicalKpiData = new KpiSerie<Double>();
		double kpiLatestValue = this.fixedKPIseries.get(0).get(1); //idea: derivative start //TODO BUG
		List<Double> maxDeviations = new ArrayList<Double>();
		List<Integer> mostDeviated = new ArrayList<Integer>();
		for (int i = 0; i < this.fixedKPIseries.size(); i++)
		{
			for(int j = 0; j < this.fixedKPIseries.get(i).size(); j++)
			{
				double derivativeKpiValue = Math.abs((double) this.fixedKPIseries.get(i).get(j) - kpiLatestValue);
				double currDeviation =
					Math.pow(this.fixedKPIseries.get(i).get(j) - this.pattern.get(j), 2) / 
					(1 + derivativeDependency * derivativeKpiValue);
				if(i == 0)
				{
					maxDeviations.add(currDeviation);
					mostDeviated.add(i);
				}
				else if (maxDeviations.get(j) < currDeviation)
				{
					maxDeviations.set(j, currDeviation);
					mostDeviated.set(j, i);
				}
			}
		}
		
		for (int i = 0; i < this.fixedKPIseries.size(); i++)
		{
			for(int j = 0; j < this.fixedKPIseries.get(i).size(); j++)
			{
				if(mostDeviated.get(j) == i && (maxDeviations.get(j) >  (averageDeviation * anomalyThreshold)) )
				{
					// it's an anomalous value, it should not be considered in normalization
					System.out.println("Anomalous value in training set " + i + " at the time " + j);
					bFoundAnomalousValues = true;
					double newNormalizedValue = 0.0;
					double currentGain = this.maxValues.get(i) - this.minValues.get(i);
					for (int k = 0; k < this.fixedKPIseries.size(); k++)
					{
						if (k != i) //exclude the current anomaly
						{
							System.out.println("other value: " + this.fixedKPIseries.get(k).get(j));
							newNormalizedValue += (this.fixedKPIseries.get(k).get(j) / (this.fixedKPIseries.size()) );
						}
						else
						{
							System.out.println("skip: " + this.fixedKPIseries.get(k).get(j));

						}
					}
					
					double newValue = newNormalizedValue * currentGain + this.averageValues.get(i);
					historicalKpiData.add( new AbstractMap.SimpleEntry<Integer, Double>(i * this.fixedKPIseries.size() + j, newValue ) );
				}
				else
				{
					historicalKpiData.add( 
						new AbstractMap.SimpleEntry<Integer, Double>(
								i * this.fixedKPIseries.size() + j, 
								this.fixedKPIseries.get(i).get(j) * (this.maxValues.get(i) - this.minValues.get(i)) + this.averageValues.get(i)
						)
					);
				}
			}
		}
		
		this.init(historicalKpiData, periodicity, historicalImportance);
		
		if (this.computePattern(historicalKpiData, periodicity, historicalImportance))
		{
			System.err.println("Error in pattern computation");
			return bFoundAnomalousValues;
		}
		
		if (this.computeReliability(historicalKpiData, periodicity, historicalImportance, derivativeDependency))
		{
			System.err.println("Error in reliability computation");
			return bFoundAnomalousValues;
		}
		
		return bFoundAnomalousValues;
	}
	
	

}
