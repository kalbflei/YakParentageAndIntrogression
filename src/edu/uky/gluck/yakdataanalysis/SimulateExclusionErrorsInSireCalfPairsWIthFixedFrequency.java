package edu.uky.gluck.yakdataanalysis;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.StringTokenizer;

public class SimulateExclusionErrorsInSireCalfPairsWIthFixedFrequency {

	public static void main(String[] args) {

		double errorRate = 0.01;
		
		/*
		 * A/A=0
		 * A/B=1
		 * B/B=2
		 */
		
		int markerCount = 87;
		//int markerCount = 131;
		//int markerCount = 174;
		double minorAlleleFrequency = 0.296;
		//double minorAlleleFrequency = 0.5;
		
		double A2 = minorAlleleFrequency*minorAlleleFrequency;
		double ABx2 = 2.0*minorAlleleFrequency*(1.0-minorAlleleFrequency);
		double B2 = (1.0-minorAlleleFrequency)*(1.0-minorAlleleFrequency);
		System.out.println("Should equal one:" + (A2+ABx2+B2));

		double[][] genotypeCounts = new double[markerCount][3];
		
		for(int i=0;i<genotypeCounts.length;i++) {
			
			genotypeCounts[i][0]=A2;
			genotypeCounts[i][1]=ABx2;
			genotypeCounts[i][2]=B2;
			
		}
		
		int[] counter = new int[genotypeCounts.length];
		
		for(int i=0;i<counter.length;i++) {
			counter[i]=0;
		}
		
		int parentGenotype = -1;
		int progenyGenotype = 0;
		double dartParent = 0.0;
		double dartProgeny = 0.0;
		int exclusions = 0;
		
		for(int i=0;i<1000000;i++) {
		//for(int i=0;i<1;i++) {
			exclusions = 0;
			for(int j=0;j<genotypeCounts.length;j++) {
				
				
				dartParent = Math.random();
				dartProgeny = Math.random();
				
				parentGenotype  = getParentGenotype(dartParent,genotypeCounts[j]);
				progenyGenotype = getProgenyGenotype(dartProgeny,genotypeCounts[j],parentGenotype);
									
				parentGenotype  = simulateError(parentGenotype,errorRate);
				progenyGenotype = simulateError(progenyGenotype,errorRate);
				
				if(isExclusion(progenyGenotype,parentGenotype)) {
					exclusions++;
				}
				
			}
		
			counter[exclusions]++;
			
		}			
		
		
		for(int i=0;i<counter.length;i++) {
			System.out.println(i + "\t" + (double)counter[i]/(1000000));
		}

		
		
	}
	
	private static boolean isError(double errorRate) {
		if(Math.random()<errorRate) {
			return true;
		}else {
			return false;
		}
	}

	private static int getParentGenotype(double dart,double[] genotypeCounts) {
		
		double runningVal = 0;
		int genotype = -1;
		
		//Step one, take the random value, and assess in which bin it falls;
		//we sum the probabilities up from the fraction of the A/A allele
		//incrementing by the fraction of the next allele in the list until we 
		//have a total that is greater than the random value.  That corresponding genotype
		//is the one chosen
		for(int i=0;i<genotypeCounts.length;i++) {
			
			runningVal+=(double)genotypeCounts[i];
			if(runningVal>dart) {
				genotype=i;
				break;
			}
		}
		
		return genotype;
		
	}
	
	public static int simulateError(int genotype,double errorRate) {
		
		if(isError(errorRate)) {
			
			if(genotype==1) {
				if(Math.random()<0.5) {
					genotype=0;
				}else {
					genotype=2;
				}
			
			}
		
		}
		
		return genotype;
		
	}
	
	private static int getProgenyGenotype(double dart,double[] genotypeCounts, int sireGenotype) {
		
		double runningVal = 0.0;
		int genotype = -1;
		
		//Step one, take the random value, and assess in which bin it falls;
		//we sum the probabilities up from the fraction of the A/A allele
		//incrementing by the fraction of the next allele in the list until we 
		//have a total that is greater than the random value.  That corresponding genotype
		//is the one chosen
		
		/*
		 * A/A=0
		 * A/B=1
		 * B/B=2
		 */
		
		double aCount = 2.0*(double)genotypeCounts[0] + (double)genotypeCounts[1];
		double bCount = 2.0*(double)genotypeCounts[2] + (double)genotypeCounts[1];
		
		aCount/=(aCount+bCount);
		bCount/=(aCount+bCount);

		//First, we choose the inherited maternal allele from the allele frequency distribution
		//for A/B, and choose the ran<=acount choose A, else if ran > (aCount) choose B,
		
		
		
		
		//Then pick the allele inherited from the sire at random.  If a homozygote (allele 1/allele 1)
		//is inherited, then allele 1 is passed down.
		//If a het is inherited, we flip a coin if ran <= 0.5 then allele 1, else allele 2
		
		double hetBreaker = Math.random();
		
		//Evaluate if inherited A from dam

		runningVal=aCount;
		
		if(runningVal>=dart) {  // A inherited from dam
			if(sireGenotype==0) { // A/A
				genotype=0;         // calf genotype A/A
			}
			if(sireGenotype==2) { // B/B
				genotype=1;         // calf genotype A/B
			}
			
			if(sireGenotype==1) { // A/B
				if(hetBreaker<=0.5) { 
					genotype=0;     // A inherited from sire 
				}else {            
					genotype=1;     // B inherited from sire 
				}
			}
			
			return genotype;
			
		}else { //B allele inherited from dam		    

			if(sireGenotype==0) { // A/A
				genotype=1;         // calf genotype A/B
			}
			if(sireGenotype==2) { // B/B
				genotype=2;         // calf genotype B/B
			}
			
			if(sireGenotype==1) { // A/B
				if(hetBreaker<=0.5) { // A inherited from sire
					genotype=1;     // A/B 
				}else {				  // B inherited from sire
					genotype=2;     // B/B
				}
			}
			return genotype;
		}
		
	}
	
	private static boolean isExclusion(int progenyGenotype, int parentGenotype) {
		
		if(progenyGenotype==0 && parentGenotype==2) {
			return true;
		}else if (progenyGenotype==2 && parentGenotype==0 ) {
			return true;
		}
		
		return false;
	}
	
}
