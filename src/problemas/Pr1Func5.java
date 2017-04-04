package problemas;

import geneticos.CromosomaBin;
import geneticos.Individuo;
import geneticos.TipoCromosoma;

import operadores.cruce.FuncionCruce;
import operadores.fitness.FitnessPr1Func5;
import operadores.mutacion.FuncionMutacion;
import operadores.seleccion.FuncionSeleccion;
import util.Par;
import view.GraficaPanel;

public class Pr1Func5 extends ProblemaFuncion{
		
	public Pr1Func5(FuncionCruce funcCruz, FuncionMutacion funcMuta, FuncionSeleccion funcSelec, double elite0to1, int numGenerations, int tamPob, GraficaPanel chartPanel){
		super(funcCruz, funcMuta, funcSelec, elite0to1, numGenerations, tamPob, 2, chartPanel);
		
		this.rangoVar.add(new Par<Double>(new Double(-10.0),new Double(10.0))); //reminder to update rangoSize on super constructor 
		this.rangoVar.add(new Par<Double>(new Double(-10.0),new Double(10.0)));
		this.tolerancia = 0.001;
		this.funcFit = new FitnessPr1Func5();
		this.minimizacion = true;
	}
	
	public void generaPobIni() {
		for (int i = 0; i < tamPob; i++) {
			CromosomaBin newCromo = new CromosomaBin(rangoVar, TipoCromosoma.BIN);
			newCromo.randomizeCromosome(tolerancia);
			Individuo newInd = new Individuo(newCromo);
			poblacion.add(newInd);		
		}
	}
}
