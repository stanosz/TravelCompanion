package com.example.voyage.api.api.ia;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import com.example.voyage.api.api.data.TheoricDataBase;
import com.example.voyage.api.api.data.TheoricPlace;
import com.example.voyage.api.api.data.TheoricUser;
import com.example.voyage.api.common.type.TypeConfiguration;
import com.example.voyage.api.tools.ia.Perceptron;
import com.example.voyage.api.tools.ia.decition.SigmoidDecision;
import com.example.voyage.api.tools.ia.learning.PerceptronLearning;
import com.example.voyage.api.tools.math.MathTools;
import com.example.voyage.api.tools.math.Matrix;
import com.example.voyage.api.tools.math.compare.CompareUnitDouble;
import com.example.voyage.api.tools.math.compare.MathUnitComparator;
import com.example.voyage.api.tools.polls.Elector;
import com.example.voyage.api.tools.polls.Elegible;
import com.example.voyage.api.tools.polls.MajorityJudgmentManager;

public class IAManager {

	public static final double PERCEPTRON_STEP = 0.2;
	public static final double PERCEPTRON_CURVE = 0.1;                                                                              
	public static Perceptron ia = new Perceptron(new SigmoidDecision(PERCEPTRON_CURVE), new PerceptronLearning());
	private static double lastResult;
	private static int choice;
	public static ArrayList<CompareUnitDouble<TheoricPlace>> results;

    public static ArrayList<CompareUnitDouble<TheoricPlace>> choosePlaces() {
		return choosePlaces(TheoricDataBase.mainUser, TheoricDataBase.places);
	}

	public static ArrayList<CompareUnitDouble<TheoricPlace>> choosePlaces(TheoricUser theoricUser,
			ArrayList<TheoricPlace> places) {
		ia.init(theoricUser.getPreferences(), PERCEPTRON_STEP);
		ArrayList<CompareUnitDouble<TheoricPlace>> tmpList = new ArrayList<CompareUnitDouble<TheoricPlace>>();

		for (TheoricPlace place : places) {
			Matrix m = normalise(place.getTypes(), place.getNumberOfTypes());
			tmpList.add(new CompareUnitDouble<TheoricPlace>(
					ia.propagate(Matrix.mult(m, place.getNote())).getValue(0, 0), place));
		}

		//Comparator compare = MathUnitComparator.getByNameDouble("<");
		//noinspection Since15
		//tmpList.sort(compare);
		results = tmpList;
		Collections.sort(tmpList);
		//tmpList.sort(MathUnitComparator.getByNameDouble("<"));
		return tmpList;
	}

	public static double dist(Matrix m1, Matrix m2) {
		double sum = 0;
		for (int i = 0; i < m1.sizeX; i++)
			sum += Math.pow(m1.getValue(i, 0) - m2.getValue(i, 0), 2.0);
		return Math.sqrt(sum);
	}

	public static ArrayList<CompareUnitDouble<TheoricPlace>> choosePlaceWithFriends(TheoricUser user,
			ArrayList<TheoricUser> users, ArrayList<TheoricPlace> places, double lambda) {
		CompareUnitDouble<TheoricPlace> friendsChoice = friendsChoice(users, places).get(0);
		ia.init(user.getPreferences(), PERCEPTRON_STEP);
		ArrayList<CompareUnitDouble<TheoricPlace>> tmpList = new ArrayList<CompareUnitDouble<TheoricPlace>>();
		double d;
		Matrix m;
		for (TheoricPlace place : places) {
			m = place.getTypes();
			d = dist(m, friendsChoice.getElement().getTypes());
			m = Matrix.add(Matrix.mult(m, place.getNote()), Matrix.mult(m, -d * lambda));
			m = normalise(m, place.getNumberOfTypes());
			tmpList.add(new CompareUnitDouble<TheoricPlace>(ia.propagate(m).getValue(0, 0), place));
		}

		return tmpList;
	}

	public static void selectPlace(int place){
		choice = place;
	}

	public static TheoricPlace selectPlace(int posPlace, ArrayList<CompareUnitDouble<TheoricPlace>> list) {
		lastResult = list.get(posPlace).getValue();
		return list.get(posPlace).getElement();
	}

	public static TheoricUser learn(TheoricUser theoricUser, double note) {
		note = note / 5;
		TheoricPlace place = results.get(choice).getElement();
		ia.configureLearning(place.getTypes(), results.get(choice).getValue(), note);
		ia.learn();
		for (int i = 0; i < ia.numberWeights; i++)
			ia.getWeights().setValue(0, i, MathTools.roundAt(ia.getWeights().getValue(0, i), 3));
		theoricUser.updatePref(ia.getWeights());
		return theoricUser;
	}

	public static void learn(TheoricUser theoricUser, TheoricPlace place, double note) {
		note = note / 5;
		ia.configureLearning(place.getTypes(), lastResult, note);
		ia.learn();
		for (int i = 0; i < ia.numberWeights; i++)
			ia.getWeights().setValue(0, i, MathTools.roundAt(ia.getWeights().getValue(0, i), 3));
		theoricUser.updatePref(ia.getWeights());
	}

	public static TheoricUser shortLearn(TheoricUser theoricUser, TheoricPlace place) {
		ia.configureLearning(place.getTypes(), 0, 0.1);
		ia.learn();
		for (int i = 0; i < ia.numberWeights; i++)
			ia.getWeights().setValue(0, i, MathTools.roundAt(ia.getWeights().getValue(0, i), 3));
		theoricUser.updatePref(ia.getWeights());
		return  theoricUser;
	}

	private static Matrix normalise(Matrix type, int numberOfType) {
		return Matrix.mult(type, (TypeConfiguration.number / numberOfType));
	}

	public static double electionVote(TheoricPlace place, TheoricUser theoricUser) {
		Matrix m = normalise(place.getTypes(), place.getNumberOfTypes());
		ia.init(theoricUser.getPreferences(), PERCEPTRON_STEP);
		
		return ia.propagate(Matrix.mult(m, place.getNote())).getValue(0, 0);
	}

	public static ArrayList<CompareUnitDouble<TheoricPlace>> friendsChoice(ArrayList<TheoricUser> theoricUsers,
			ArrayList<TheoricPlace> theoricPlaces) {
		ArrayList<Elector> electors = new ArrayList<Elector>();
		electors.addAll(theoricUsers);
		ArrayList<Elegible> elegibles = new ArrayList<Elegible>();
		elegibles.addAll(theoricPlaces);

		ArrayList<CompareUnitDouble<Elegible>> result = MajorityJudgmentManager.election(electors, elegibles);
		ArrayList<CompareUnitDouble<TheoricPlace>> resultPlace = new ArrayList<CompareUnitDouble<TheoricPlace>>();

		for (CompareUnitDouble<Elegible> elegible : result)
			resultPlace.add(
					new CompareUnitDouble<TheoricPlace>(elegible.getValue(), (TheoricPlace) elegible.getElement()));
		return resultPlace;
	}

}
