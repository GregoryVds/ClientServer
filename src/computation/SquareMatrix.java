package computation;

public class SquareMatrix {
	private double coefficients[][];
	
	public SquareMatrix(int rank){
		coefficients = new double [rank][rank];
	}
	
	public SquareMatrix pow(int power){
		if(power<=1)
			return this;
		
		SquareMatrix tmp = this.MultiplicationSquareMatrix(this);
		for(int i=0; i<power-2; i++){
			tmp = tmp.MultiplicationSquareMatrix(this);
		}
		return tmp;
	}
	
	public SquareMatrix MultiplicationSquareMatrix(SquareMatrix B){	
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
	

	public double[][] getCoefficients() {
		return coefficients;
	}

	public void setCoefficients(double coefficients[][]) {
		this.coefficients = coefficients;
	}
	
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
	
	public static SquareMatrix StringConvertToMatrix(String stringMatrix){
		System.out.println(stringMatrix);
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
	
	
	/*
	 * @pre : 	matrix must be in this form : a,b,c,d;e,f,g,h;i,j,k,l;m,n,o,p
	 * 			where 	; represent row of matrix
	 * 					a to p the coeficient of matrix
	 * 					the matrix must be square
	 */
	public static String powerMatrix(String matrix, int exponent) {
		SquareMatrix matrixA = StringConvertToMatrix(matrix);
		SquareMatrix resultat = matrixA.pow(exponent);
		return resultat.toString();
	}
	
	/*public static void main (String args[]){
		SquareMatrix A = new SquareMatrix(2);
		double [][] coefA = { {0,2,4,4},{1,3,4,6},{1,2,6,9},{1,3,4,6}};
		A.setCoefficients(coefA);
		SquareMatrix C = A.pow(40);
		String resultat = C.toString();
		System.out.println(resultat);
		
		String stringMatrix = "0,2,4,4;1,3,4,6;1,2,6,9;1,3,4,6";
		SquareMatrix B = StringConvertToMatrix(stringMatrix);
		String resultatB = B.toString();
		System.out.println(resultatB);
		
		SquareMatrix C = B.pow(2);
		String resultatC = C.toString();
		System.out.println(resultatC);
		String data = "0,2,4,4;1,3,4,6;1,2,6,9;1,3,4,6/6";
		System.out.println(powerMatrix(data));
	}*/
	
}
