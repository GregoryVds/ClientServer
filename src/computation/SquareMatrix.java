/**
 * SquareMatrix raises a square matrix to a given exponent.
 * 
 * @author      Jérôme Lemaire
 */

package computation;

///////////////////////////////////////////////////////////////////////////////////////////////////

public class SquareMatrix {
	private double coefficients[][];
	
	///////////////////////////////////////////////////////////////////////////////////////////////
	/*
	 * @pre : 	matrix must be in this form : a,b,c,d;e,f,g,h;i,j,k,l;m,n,o,p
	 * 			where 	; separates rows of the matrix
	 * 					a to p the coeficient of matrix
	 * 					the matrix must be squared
	 */
	
	public static String powerMatrix(String matrix, int exponent) {
		SquareMatrix matrixA = StringConvertToMatrix(matrix);
		SquareMatrix resultat = matrixA.pow(exponent);
		return resultat.toString();
	}
	
	///////////////////////////////////////////////////////////////////////////////////////////////
	
	public String toString(){
		double[][] coef = this.getCoefficients();
		String str="";
		int rank= coef.length;
		for(int i=0;i<rank; i++){
			for(int j=0; j<rank;j++){
				if(j!=rank-1)
					str += coef[i][j] + ",";
				else
					str += coef[i][j];
				
			}
			if(i!=rank-1)
				str +=";";
				
		}
		return str;
	}
	
	///////////////////////////////////////////////////////////////////////////////////////////////
	
	private SquareMatrix(int rank){
		coefficients = new double [rank][rank];
	}
	
	///////////////////////////////////////////////////////////////////////////////////////////////
	
	private SquareMatrix pow(int power){
		if(power<=1)
			return this;
		
		SquareMatrix tmp = this.MultiplicationSquareMatrix(this);
		for(int i=0; i<power-2; i++){
			tmp = tmp.MultiplicationSquareMatrix(this);
		}
		return tmp;
	}
	
	///////////////////////////////////////////////////////////////////////////////////////////////
	
	private SquareMatrix MultiplicationSquareMatrix(SquareMatrix B){	
		double[][] CoefA = this.getCoefficients();
		int rank = CoefA.length;
		double[][] CoefB = B.getCoefficients();
		double [][] Coeftmp = new double [rank][rank]; 
		SquareMatrix C = new  SquareMatrix(rank);
		for(int i = 0; i<rank;i++){
			for(int j = 0;j<rank;j++){
				double sum =0;
				for(int k=0; k<rank;k++){
					sum += CoefA[i][k]*CoefB[k][j];
				}
				Coeftmp[i][j]=sum;
			}
		}
		C.setCoefficients(Coeftmp);
		return C;
	}
	
	///////////////////////////////////////////////////////////////////////////////////////////////

	private double[][] getCoefficients() {
		return coefficients;
	}

	///////////////////////////////////////////////////////////////////////////////////////////////
	
	private void setCoefficients(double coefficients[][]) {
		this.coefficients = coefficients;
	}

	///////////////////////////////////////////////////////////////////////////////////////////////
	
	private static SquareMatrix StringConvertToMatrix(String stringMatrix){
		String[] rows = stringMatrix.split(";");
		SquareMatrix matrix = new SquareMatrix(rows.length);
		double [][] coeficients = new double[rows.length][rows.length];
		
		for(int i=0; i<rows.length;i++){
			String[] coefinRow = rows[i].split(",");
			for(int j=0; j< coefinRow.length; j++){
				coeficients[i][j] = Double.parseDouble(coefinRow[j]);
			}
		}
		matrix.setCoefficients(coeficients);
		return matrix;
		
	}
	
	///////////////////////////////////////////////////////////////////////////////////////////////
}
