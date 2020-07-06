package edu.uky.gluck.yakdataanalysis;

public class SimulateAlleleLoss {

	public static void main(String[] args) {
		
		int generations = 6;
		int iterations = 1000000;
		String[][] genotypes = new String[2][350];
		String inheritedAllele = null;
		int cattleAlleles = 0;
		
		int[][] cattleAlleleCount = new int[400][generations];
		for(int i=0;i<cattleAlleleCount.length;i++) {
			for(int j=0;j<cattleAlleleCount[i].length;j++) {
				cattleAlleleCount[i][j]=0;
			}
		}
		
		for(int k=0;k<iterations;k++) {
		
			//initialize genotype array
			for(int i=0;i<genotypes[0].length;i++) {
				genotypes[0][i] = "Y";
				genotypes[1][i] = "C";
			}
			
			for(int j=0;j<generations;j++) {
				
				for(int i=0;i<genotypes[0].length;i++) {
					
					inheritedAllele = genotypes[(int)Math.floor(2.0*Math.random())][i];
					genotypes[0][i] = inheritedAllele;
					genotypes[1][i] = "Y";
				}
				
				cattleAlleles = 0;
				
				for(int i=0;i<genotypes[0].length;i++) {
					
					if(genotypes[0][i].equals("C") || genotypes[1][i].equals("C")) {
						cattleAlleles++;
					}
					
				}
				cattleAlleleCount[cattleAlleles][j]++;
				
			}

			
		}
	
		for(int i=0;i<cattleAlleleCount.length;i++) {
			System.out.print(i);
			for(int j=0;j<cattleAlleleCount[i].length;j++) {
				System.out.print("\t" + ((double)cattleAlleleCount[i][j]/(double)iterations));
			}
			System.out.println();
		}
		
	}
	
}
