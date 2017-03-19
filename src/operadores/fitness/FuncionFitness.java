package operadores.fitness;

import java.util.ArrayList;
import geneticos.Individuo;

public abstract class FuncionFitness {

	public abstract void evaluate(Individuo ind);	

	public ArrayList<Individuo> evaluate(ArrayList<Individuo> poblacion, boolean minimizacion) {
		double avgFitness = 0;
		
		Individuo cero = poblacion.get(0);
		evaluate(cero);
		Individuo mejor =cero;
		Individuo peor = cero;
		avgFitness += cero.getFitness();
		
		if(minimizacion){
			for (int i = 1; i < poblacion.size(); i++) {
				Individuo eval = poblacion.get(i);
				evaluate(eval);
				double fitEval = eval.getFitness();
				avgFitness += fitEval;
				if(fitEval > peor.getFitness())
					peor = eval;
				else if(fitEval < mejor.getFitness())
					mejor = eval;
			}
		}
		else{
			for (int i = 1; i < poblacion.size(); i++) {
				Individuo eval = poblacion.get(i);
				evaluate(eval);
				double fitEval = eval.getFitness();
				avgFitness += fitEval;
				if(fitEval < peor.getFitness())
					peor = eval;
				else if(fitEval > mejor.getFitness())
					mejor = eval;
			}
		}

		ArrayList<Individuo> ret = new ArrayList<Individuo>(3);
		ret.add(mejor);
		ret.add(peor);
		Individuo fake = new Individuo();
		fake.setFitness(avgFitness/(double)poblacion.size());
		ret.add(fake);
		
		return ret;
	}
		
	public void adapt(ArrayList<Individuo> poblacion, double fitPeor, boolean minimizacion){
		for(int i = 0; i < poblacion.size(); i++){
			adaptInd(poblacion.get(i), fitPeor, minimizacion);
		}
	}
	
	public void adaptInd(Individuo ind, double fitPeor, boolean minimizacion){
		if(minimizacion)
			ind.setFitnessAdaptado(fitPeor - ind.getFitness());
		else
			ind.setFitnessAdaptado(ind.getFitness() - fitPeor);
	}

}