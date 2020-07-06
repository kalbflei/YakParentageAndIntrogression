package edu.uky.gluck.yakdataanalysis;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.StringTokenizer;

public class SimulateExclusionsWithFixedFrequency {

	public static void main(String[] args) {
		
		double errorRate = 0.0;
		
		//int markerCount = 87;
		//int markerCount = 131;
		int markerCount = 174;
		//double minorAlleleFrequency = 0.296;
		double minorAlleleFrequency = 0.5;
		
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
				
				parentGenotype  = getParentGenotype(dartParent,genotypeCounts[j],errorRate);
				progenyGenotype = getParentGenotype(dartProgeny,genotypeCounts[j],errorRate);
									
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

	private static int getParentGenotype(double dart,double[] genotypeCounts, double errorRate) {
		
		double runningVal = 0;
		int genotype = -1;
		
		//Step one, take the random value, and assess in which bin it falls;
		//we sum the probabilities up from the fraction of the A/A allele
		//incrementing by the fraction of the next allele in the list until we 
		//have a total that is greater than the random value.  That corresponding genotype
		//is the one chosen
		for(int i=0;i<genotypeCounts.length;i++) {
			
			runningVal+=genotypeCounts[i];
			if(runningVal>dart) {
				genotype=i;
				break;
			}
		}
		
		//here, we add a random error rate to compensate for miscalling heterozygotes 
		//as the homozygote of one of the two alleles present for the animal.
		
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
	
	private static boolean isExclusion(int progenyGenotype, int parentGenotype) {
		
		if(progenyGenotype==0 &&
				(parentGenotype==2 ||
				 parentGenotype==4 ||
				 parentGenotype==5)) {
			return true;
		}else if (progenyGenotype==1 &&
				   parentGenotype==5) {
			return true;
		}else if (progenyGenotype==2 &&
				  (parentGenotype==0 ||
				   parentGenotype==3 ||
				   parentGenotype==5)) {
			return true;
		}else if (progenyGenotype==3 &&
				   parentGenotype==2) {
			return true;
		}else if (progenyGenotype==4 &&
				   parentGenotype==0) {
			return true;
		}else if (progenyGenotype==5 &&
				  (parentGenotype==0 ||
				   parentGenotype==1 ||
				   parentGenotype==2)) {
			return true;
		}
		
		return false;
	}
	
}
