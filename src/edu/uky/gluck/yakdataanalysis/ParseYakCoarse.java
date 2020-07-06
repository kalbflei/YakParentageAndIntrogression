/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.uky.gluck.yakdataanalysis;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.StringTokenizer;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipException;

/**
 *
 * @author tedkalbfleisch
 */
public class ParseYakCoarse {
    
    /*
    0   199911001	Bos gaurus	Gaur * 
    1   199911002	Bos gaurus	Gaur *
    2   199912001	Bison	Bison *
    3   200008100	Ovis aries	sheep
    4   200124009	Alces alces	Moose
    5   200710001	Bos javanicus	Banteng *
    6   200710002	Bos javanicus	Banteng *
    7   201027018	Bos grunniens	Yak
    8   201162012	Taurotragus oryx	Eland
    9   201524011	Alces alces	Moose
    10  BGI_Goat	Capra hircus	Goat
    11  ChineseYak	Bos grunniens	Yak
    12  Clearwater06	Alces alces	Moose
    13  ICAR_MurrahHeifer	Bubalus bubalis	Water Buffalo
    14  R199	Alces alces	Moose
    */
    
    public static void main(String[] args){
        
        try{
                
        	String vcfFilename = args[0];
        	String mode = args[1];
        	//The mode is set in order to identify variants where the ancestral allele (i.e., the common
        	//non-reference allele found in the gaur, banteng, bison and yak) was the 1 allele or the 2
        	//alleles in separate runs.  When the mode=1, then the only gaur/banteng/bison genotype allowed was 1/1.
        	//When the mode is 2, then the homozygous ancestral genotype would have been 2/2.  Keeping two separate 
        	//lists made it more straighforward in later steps to count ancestral vs yak derived alleles in accompanying
        	//publication.
        	
        	
            BufferedReader bufferedReader =  getBufferedReader(vcfFilename);
            //The original data file that contained all variants and the corresponding genotypes of the animals listed above was called YakVsAllViaUnifiedGenotyper.vcf.gz
            //That file is too large to attach as a supplement, but the two result files 
            //1.1_markers.vcf and 2.2_markers.vcf are included in this github project.
            //BufferedReader bufferedReader =  new BufferedReader (new InputStreamReader (new GZIPInputStream (new FileInputStream (new File("YakVsAllViaUnifiedGenotyper.vcf")))));
            
            String line = null;
            StringTokenizer st = null;
            StringTokenizer gt = null;
            boolean printFlag = false;
            boolean yakPrintFlag = false;
            String genotype = null;
            String[] genotypes = new String[15];
            String refAllele = null;
            String varAlleles = null;
            String val = null;
            int[] nonYak = {0,1,2,5,6};
            String ancestralAllele = null;
            
            if(mode.equals("1")) {
            	ancestralAllele = "1/1";
            }else if(mode.equals("2")) {
            	ancestralAllele = "2/2";
            }else {
            	System.err.println("Mode must equal 1 or 2.");
            	System.err.println("now exiting to system....");
            	System.exit(1);
            }
            
            while((line=bufferedReader.readLine())!=null){
            
                if(line.startsWith("#")){
                    System.out.println(line);
                    System.out.flush();
                    continue;
                }
                    
                st = new StringTokenizer(line);
                for(int i=0;i<9;i++){
                    val = st.nextToken();
                    if(i==3) {
                    	refAllele = val;
                    }
                    if(i==4) {
                    	varAlleles = val;
                    }
                    
                }
                
                printFlag = false;
                yakPrintFlag = false;
                
                //if(refAllele.length()!=1 || !verifySingleNucleotideSubstitution(varAlleles)){
                if(refAllele.length()!=1){
                	continue;
                }
                
                for(int i=0;i<15;i++){
                    gt = new StringTokenizer(st.nextToken(),":");
                    genotypes[i] = gt.nextToken();
                }
                   
                        
                if(genotypes[7].equals("1/2") && genotypes[11].equals("1/2")){
                    yakPrintFlag = true;                   
                }else {
                	continue;//ignore the Yak
                }

                
                if(isFixedInNonYakNonCattle(ancestralAllele,nonYak,genotypes)){

                	printFlag = true;
                    
                }

                if(printFlag && yakPrintFlag){
                    System.out.println(line);
                    System.out.flush();
                }

            }
            
            bufferedReader.close();
            
        }catch(IOException ioException){
            ioException.printStackTrace();
        }
        
    }
    
    private static boolean verifySingleNucleotideSubstitution(String varAlleles) {
    	int length = 0;
    	
    	StringTokenizer st = new StringTokenizer(varAlleles,",");
    	
    	if(st.countTokens()!=2) {
    		return false;
    	}
    	String allele = null;
    	while(st.hasMoreTokens()) {
    		allele = st.nextToken();
    		if(allele.length()!=1) {
    			return false;
    		}
    	}
    	return true;
    }
    
    private static boolean isFixedInNonYakNonCattle(String genotype,int[] animalIndices,String[] measuredGenotypes) {
    
    	for(int i=0;i<animalIndices.length;i++) {
    		if(!measuredGenotypes[animalIndices[i]].equals(genotype)) {
    			
    			return false;
    		
    		}
    			
    	}
    	return true;
    }
    
    private static BufferedReader getBufferedReader(String filename)  {
    	
    	BufferedReader bufferedReader = null;
    	try {
    		try {
    			bufferedReader = new BufferedReader (new InputStreamReader (new GZIPInputStream (new FileInputStream (new File(filename)))));
    		}catch(ZipException zipException) {
    			bufferedReader = new BufferedReader(new FileReader(new File(filename)));
    		}
    	}catch(IOException ioException) {
    		ioException.printStackTrace();
    		System.exit(1);
    	}
    	return bufferedReader;
    	
    }
    
}
