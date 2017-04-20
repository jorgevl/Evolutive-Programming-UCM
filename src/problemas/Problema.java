package problemas;

import geneticos.CromosomaPermInt;
import geneticos.Individuo;
import geneticos.Poblacion;

import java.util.ArrayList;
import java.util.Collections;

import javax.swing.JFrame;
import javax.swing.SwingWorker;

import operadores.cruce.FuncionCruce;
import operadores.fitness.FuncionFitness;
import operadores.mutacion.FuncionMutacion;
import operadores.mutacion.Inversion;
import operadores.seleccion.FuncionSeleccion;
import util.Contractividad;
import util.Par;
import util.Utiles;
import view.GUI;
import view.GraficaPanel;

public abstract class Problema extends SwingWorker<Individuo, String> {

	protected ArrayList<Individuo> poblacion;
	protected ArrayList<Individuo> poblacionNueva;
	protected double tolerancia;
	protected int tamPob;
	protected int numGenerations;
	protected ArrayList<Par<Double>> rangoVar;
	protected FuncionFitness funcFit;
	protected FuncionCruce funcCruz;
	protected FuncionMutacion funcMuta;
	protected Inversion invEspecial;
	protected FuncionSeleccion funcSelec;
	protected int tamElite;
	protected String contracString;
	protected Contractividad contractividad;
	protected ArrayList<Double> puntuaciones;
	protected ArrayList<Double> punts_acum;
	protected Individuo mejorIndividuo;
	protected Individuo mejorAbsoluto;
	protected Individuo peorIndividuo;
	protected ArrayList<Individuo> mejorPeorAvg;
	protected double pobAvgFitness;
	protected GUI gui;
	protected boolean minimizacion;
	protected boolean stop;
	protected boolean invEspActivada;

	public Problema(FuncionCruce funcCruz, FuncionMutacion funcMuta, FuncionSeleccion funcSelec, double elite0to1,
			int numGenerations, int tamPob, int rangoSize, JFrame gui) {
		this.funcSelec = funcSelec;
		this.funcMuta = funcMuta;
		this.funcCruz = funcCruz;

		this.tamPob = tamPob;
		this.numGenerations = numGenerations;
		this.tamElite = (int) Math.floor(elite0to1 * tamPob);

		this.rangoVar = new ArrayList<Par<Double>>(rangoSize);
		this.poblacion = new ArrayList<Individuo>(tamPob);
		this.puntuaciones = new ArrayList<Double>(tamPob);
		this.punts_acum = new ArrayList<Double>(tamPob);

		this.gui = (GUI) gui;
		
		if(true){//gui.inversionEspecialActivada){
			invEspActivada = true;
			invEspecial = new Inversion();
			invEspecial.setProb(0.5);//loQueSeaOportuno);  // TODO A mano? El mismo que el de la mutación normal (no)? ..
		}
		else
			invEspActivada = false;
		
		this.contracString = this.gui.getContractividad();
		
		this.stop = false;
	}

	public abstract void generaPobIni();

	public abstract Double getOptimo();

	public Individuo executeProblem() {
		generaPobIni();
		initGraphInds();
		contractividad = new Contractividad(contracString, minimizacion);
		int currentIter = 0;
		
		while((currentIter < numGenerations) && !stop) {
			evalPoblacion();
			adaptPoblacion();
			
			if(contractividad.execute(new Poblacion(poblacion, mejorIndividuo, mejorAbsoluto, peorIndividuo, pobAvgFitness))) //Devuelve true si hay que preprocesar (no ha vuelto a una pob antigua) 
				preProcesarPoblacion(); //Ademas modifica la poblacion si procede (para restaurar una anterior)

			poblacionNueva = new ArrayList<Individuo>(tamPob);
			rellenarPobCruce();
			funcMuta.mutar(poblacionNueva);
			insertElite();
			
			if(false)//invEspActivada)
				inversionEspecial();
			
			poblacion = poblacionNueva;

			if(contractividad.iteracionValida()){ //Se ha guardado previamente si es valida o no, en execute
				pintarGrafica(currentIter); //Pinta los de la ultima exitosa
				currentIter++;
			}
		}

		return mejorAbsoluto;
	}
	
	private void inversionEspecial() {
		double prob = invEspecial.getProb();
		for(int i = 0; i < poblacion.size(); i++){
			if(Utiles.randomDouble01() > prob){
				Individuo original = poblacion.get(i);
				Individuo maybe = original.clone();
				invEspecial.mutarInd(maybe);
				funcFit.evaluate(maybe);
				if(minimizacion){
					if(maybe.getFitness() < original.getFitness()){
						original = maybe;
						funcFit.adaptInd(original, peorIndividuo.getFitness());
					}
				}
				else{
					if(maybe.getFitness() > original.getFitness()){
						original = maybe;
						funcFit.adaptInd(original, peorIndividuo.getFitness());
					}
				}
			}
		}
	}

	private void preProcesarPoblacion(){
		Collections.sort(poblacion);
		puntuaciones = new ArrayList<Double>(tamPob);
		punts_acum = new ArrayList<Double>(tamPob);
		funcSelec.clasificarPob(poblacion, puntuaciones, punts_acum);
	}
	
	
	private void pintarGrafica(int currentIter){
		if (gui != null) gui.getChartPanel().update(currentIter, mejorIndividuo.getFitness(), peorIndividuo.getFitness(), pobAvgFitness, mejorAbsoluto.getFitness());
	}

	private void rellenarPobCruce() { // Rellena la nueva poblacion cruzando si
										// procede (y guardando espacio para la
										// elite)
		while (poblacionNueva.size() < tamPob - tamElite) {
			Individuo ind1 = funcSelec.select(poblacion, punts_acum).clone();
			Individuo ind2 = funcSelec.select(poblacion, punts_acum).clone();

			Par<Individuo> trasCruce = funcCruz.cruzar(new Par<Individuo>(ind1, ind2));

			Individuo ind1t = trasCruce.getN1();
			Individuo ind2t = trasCruce.getN2();

			poblacionNueva.add(ind1t);
			if (poblacionNueva.size() < tamPob - tamElite) // Puede ser un
															// numero impar de
															// nuevos individuos
				poblacionNueva.add(ind2t);
		}
	}

	private void evalPoblacion() {
		mejorPeorAvg = funcFit.evaluate(poblacion);
		actualizarGraphInds(mejorPeorAvg);
	}

	private void actualizarGraphInds(ArrayList<Individuo> mejorPeorAvg) {
		// Guardar fitness medio de la pob
		pobAvgFitness = mejorPeorAvg.get(2).getFitness();

		// Guardar mejor individuo de la generacion (posible mejor absoluto)
		mejorIndividuo = mejorPeorAvg.get(0).clone();

		if ((mejorAbsoluto.getFitness() > mejorIndividuo.getFitness()) && minimizacion)
			mejorAbsoluto = mejorIndividuo.clone();
		else if ((mejorAbsoluto.getFitness() < mejorIndividuo.getFitness()) && !minimizacion)
			mejorAbsoluto = mejorIndividuo.clone();

		// Guardar peor individuo de la generacion
		peorIndividuo = mejorPeorAvg.get(1).clone();
	}

	private void initGraphInds() {
		mejorAbsoluto = new Individuo();
		if (minimizacion)
			mejorAbsoluto.setFitness(Double.MAX_VALUE);
		else
			mejorAbsoluto.setFitness(Double.MIN_VALUE);
	}

	private void insertElite() {
		for (int i = 0; i < tamElite; i++)
			poblacionNueva.add(poblacion.get(tamPob - i - 1).clone());
	}

	private void adaptPoblacion() {
		funcFit.adapt(poblacion, peorIndividuo.getFitness());
	}

	private void setRangoVar(ArrayList<Par<Double>> rangoVar) {
		this.rangoVar = rangoVar;
	}

	public void setFuncFit(FuncionFitness funcFit) {
		this.funcFit = funcFit;
	}

	public void setFuncCruz(FuncionCruce funcCruz) {
		this.funcCruz = funcCruz;
	}

	public void setFuncMuta(FuncionMutacion funcMuta) {
		this.funcMuta = funcMuta;
	}

	public void setFuncSelec(FuncionSeleccion funcSelec) {
		this.funcSelec = funcSelec;
	}

	protected Individuo doInBackground() throws Exception {
		try {
			executeProblem();
		} catch (Exception e) {
			e.printStackTrace();
			System.in.read();
		}
		return null;
	}

	protected void done() {
		this.gui.onExecutionDone(this.mejorAbsoluto);
	}

	public void stopProblemExecution() {
		this.stop = true;
	}

	public boolean singularFact(int tamPobTwo, int cromPosibles, int rangoRestante) {
		if (cromPosibles > tamPobTwo)// Si ya hay mas posibles que el doble de
										// la población, true
			return true;
		else if (rangoRestante == 0) // Si llegamos al tam max de crom y no hay
										// mas del doble de la población, false
			return false;
		else // Si no, ampliamos el num de croms posibles con lo que da el rango
				// restante y reevaluamos
			return singularFact(tamPobTwo, cromPosibles *= rangoRestante, rangoRestante - 1);
	}
}
