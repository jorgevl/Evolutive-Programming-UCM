package problemas;

import javax.swing.JFrame;

import geneticos.CromosomaBin;
import geneticos.Individuo;
import geneticos.TipoCromosoma;

import operadores.cruce.FuncionCruce;
import operadores.fitness.FitnessPr1Func1;
import operadores.mutacion.FuncionMutacion;
import operadores.seleccion.FuncionSeleccion;
import util.Par;
import view.GraficaPanel;

public class Pr1Func1 extends Problema{
		
	public Pr1Func1(FuncionCruce funcCruz, FuncionMutacion funcMuta, FuncionSeleccion funcSelec, double elite0to1, int numGenerations, int tamPob, JFrame gui){
		super(funcCruz, funcMuta, funcSelec, elite0to1, numGenerations, tamPob, 1, gui);
		
		this.rangoVar.add(new Par<Double>(new Double(-250),new Double(250))); //reminder to update rangoSize on super constructor  
		this.tolerancia = 0.001;
		this.funcFit = new FitnessPr1Func1();	
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
	
	public Double getOptimo(){
		return -201.843;
	}
}
