package perf;



public class SquareMatrix {
	private double coefficients[][];
	
	public SquareMatrix(int rank){
		coefficients = new double [rank][rank];
	}
	
	public SquareMatrix pow(double power){
		if(power<=1)
			return this;
		
		SquareMatrix tmp = this.MultiplicationSquareMatrix(this);
		for(double i=0; i<power-2; i++){
			tmp=  tmp.MultiplicationSquareMatrix(this);
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
		int rank = coef.length;
		String str = "rank :"+rank +"\n";
		for(int i=0;i<rank; i++){
			str += "(";
			for(int j=0; j<rank;j++){
				str += " "+ coef[i][j];
			}
			str +=") \n";
		}
		return str;
	}
	
	public static void main (String args[]){
		SquareMatrix A = new SquareMatrix(2);
		double [][] coefA = { {0,2,4,4},{1,3,4,6},{1,2,6,9},{1,3,4,6} };
		A.setCoefficients(coefA);
		SquareMatrix C = A.pow(40);
		String resultat = C.toString();
		System.out.println(resultat);
	}
	
}
