package operadores.fitness;

import geneticos.Individuo;

public class FuncionFitnessP4 extends FuncionFitness {

	public void evaluate(Individuo ind) {
		double resfor = 0;
		for(int i = 1; i <= ind.getFenotipo().size(); i++){
			double x = ind.getFenotipo().get(i-1);
			double sen1 = Math.sin(x);
			double parentesis = ((i+1)*Math.pow(x,2))/Math.PI;
			double sen2 = Math.pow(Math.sin(parentesis), 20);
			resfor += sen1*sen2;
		}
		
		double result = (-1)*resfor;
		
		ind.setFitness(result);
	}

}