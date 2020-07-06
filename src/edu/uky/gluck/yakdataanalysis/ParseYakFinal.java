/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.uky.gluck.yakdataanalysis;

//import edu.uky.utilities.Utils;
import htsjdk.samtools.reference.ReferenceSequenceFile;
import htsjdk.samtools.reference.ReferenceSequenceFileFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.StringTokenizer;

/**
 *
 * @author tedkalbfleisch
 */
public class ParseYakFinal {
    
    public static void main(String[] args){
        
        int contextLength = 100;
        
        try{

            String[] bovineVariants = getVariants("bovineVariantsNeighboringYakPositions.vcf",9);
            String[] yakVariants    = getVariants("YakVsYakEligibleViaUnifiedGenotyper.vcf",0);
          
            
            File fastaFile = new File("Bt_UMD3.1.fa");
            //Location location = null;
            int k = -1;
            
            ReferenceSequenceFile rsf = ReferenceSequenceFileFactory.getReferenceSequenceFile(fastaFile);
            
            BufferedReader bufferedReader = new BufferedReader(new FileReader(new File("YakSpecificVariantsAugmented.vcf")));
            String line = null;
            StringTokenizer st = null;
            StringTokenizer st2 = null;
            
            String chromosome = null;
            long position = -1L;
            String bovineAllele = null;
            String yakAlleles = null;
            String leftContext = null;
            String rightContext = null;
            String leftFlag = null;
            String rightFlag = null;
            String leftCloseFlag = null;
            String rightCloseFlag = null;

            
            while((line=bufferedReader.readLine())!=null){
                
            	if(line.startsWith("#")) {
            		continue;
            	}
            	
                st = new StringTokenizer(line);
            
                chromosome = st.nextToken();
                position = Long.parseLong(st.nextToken());
                st.nextToken();
                bovineAllele = st.nextToken();
                yakAlleles = st.nextToken();

            	leftContext = rsf.getSubsequenceAt(chromosome, position-contextLength, position-1).getBaseString().toLowerCase();

            	rightContext = rsf.getSubsequenceAt(chromosome, position+1, position+contextLength).getBaseString().toLowerCase();

                leftContext = markNeighboringVariants(leftContext,chromosome,(position-contextLength),bovineVariants,contextLength);
                leftContext = markNeighboringVariants(leftContext,chromosome,(position-contextLength),yakVariants,contextLength);
                rightContext = markNeighboringVariants(rightContext,chromosome,(position+1),bovineVariants,contextLength);
                rightContext = markNeighboringVariants(rightContext,chromosome,(position+1),yakVariants,contextLength);
                
                if(leftContext.indexOf("N")>=0){
                    leftFlag = "!";
                    if(leftContext.lastIndexOf("N")>=(leftContext.length()-25)) {
                    	leftCloseFlag = "!";
                    }else {
                    	leftCloseFlag = "*";
                    }
                }else{
                    leftFlag = "*";
                    leftCloseFlag = "*";
                }

                if(rightContext.indexOf("N")>=0){
                    rightFlag = "!";
                    if(rightContext.indexOf("N")<=25) {
                    	rightCloseFlag = "!";
                    }else {
                    	rightCloseFlag = "*";
                    }
                }else{
                    rightFlag = "*";
                    rightCloseFlag = "*";
                }                
                
                System.out.println(chromosome + ":" + position + "\t" + leftFlag + rightFlag + "\t" + leftCloseFlag + rightCloseFlag + "\t" + leftContext + "[" + bovineAllele + "/" + yakAlleles.replace(",", "/") + "]" + rightContext);
                System.out.flush();
            	
            }

            
        }catch(IOException ioException){
            ioException.printStackTrace();
        }
        
    }
    
    private static String[] getVariants(String bovineFileName, int alleleCnt) throws IOException{
        
        BufferedReader bufferedReader = new BufferedReader(new FileReader(new File(bovineFileName)));
        
        String line = null;
        StringTokenizer st = null;
        ArrayList arrayList = new ArrayList();
        String[] returnString = null;
        String description = null;
        String chromosome = null;
        String position = null;
        int variantAlleleCnt = 0;
        String acString = null;
        
        while((line=bufferedReader.readLine())!=null){
            
            if(line.startsWith("#")){
                continue;
            }

            
            st = new StringTokenizer(line,"\t ");

            chromosome = st.nextToken();
            position = st.nextToken();
            
     
            for(int i=0;i<6;i++){
                description = st.nextToken();
            }
            
            st = new StringTokenizer(description,";");
            
            acString = st.nextToken();
            
            st = new StringTokenizer(acString,"=,");
            
            variantAlleleCnt = 0;            
            
            st.nextToken();
            
            while(st.hasMoreTokens()){
                variantAlleleCnt+= Integer.parseInt(st.nextToken());
            }
            System.out.println(variantAlleleCnt);
            System.out.flush();
            if(variantAlleleCnt>=alleleCnt){
                arrayList.add(chromosome + ":" + position);
            }
        }
        
        returnString = new String[arrayList.size()];
        
        for(int i=0;i<returnString.length;i++){
            returnString[i] = (String)arrayList.get(i);
        }
        
        bufferedReader.close();
        
        return returnString;
        
    }
    
    private static String markNeighboringVariants(String context, String chromosome, long position, String[] variants, long contextLength){
        
        char[] contextArray = context.toCharArray();
        StringTokenizer st = null;
        String varChromosome = null;
        long varPosition = -1L;
        
        
        for(int i=0;i<variants.length;i++){
            
            st = new StringTokenizer(variants[i],":");
            varChromosome = st.nextToken();
            varPosition = Long.parseLong(st.nextToken());
            int bin = -1;
            
            if(chromosome.equals(varChromosome)){
                
                if(position<=varPosition && (position+contextLength)>varPosition){
                    
                    bin = (new Long(varPosition-position)).intValue();
                    contextArray[bin] = 'N';
                    
                }
                
            }
        }
        
        return new String(contextArray);
    }
    
}
