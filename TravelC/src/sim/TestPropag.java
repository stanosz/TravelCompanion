package sim;

import java.sql.SQLException;
import java.util.ArrayList;

import api.data.TheoricDataBase;
import api.data.TheoricPlace;
import api.data.TheoricUser;
import api.ia.IAManager;
import model.Utilisateur;
import persistence.PersistenceData;
import persistence.exception.NoPlaceFoundException;
import persistence.exception.NoUserFoundException;
import persistence.exception.YouHaveNoFriendsExeption;
import tools.math.CoordinatesDouble;
import tools.math.MathTools;
import tools.math.Matrix;
import tools.math.compare.CompareUnitDouble;

public class TestPropag {
	public static void main(String[] args) {

		CoordinatesDouble coordinatesDouble = new CoordinatesDouble(new double[] { 49.0428886, 2.084052299 });
		PersistenceData persistenceData = new PersistenceData("root", "", "release1");
		try {
			Utilisateur utilisateur = persistenceData.User(1);
			ArrayList<Utilisateur> utilisateurs = (ArrayList<Utilisateur>) persistenceData.persisteAmis(1);
			System.out.println(utilisateur);
			System.out.println(utilisateurs);
			TheoricDataBase.requestMainUser(persistenceData, 1);
			TheoricDataBase.mainUser.moveTo(coordinatesDouble);

			TheoricDataBase.requestNearPlace(persistenceData);
			for (TheoricUser user : TheoricDataBase.friends)
				System.out.println(user);

			ArrayList<CompareUnitDouble<TheoricPlace>> places = IAManager.choosePlaces(TheoricDataBase.mainUser,
					TheoricDataBase.places);
			System.out.println("\n");
			System.out.println(Matrix.trans(TheoricDataBase.mainUser.getPreferences()));
			for (CompareUnitDouble<TheoricPlace> placeData : places)
				System.out.println("place name : "+placeData.getElement().getName() + "\n" + "exepected note : "+ MathTools.roundAt(placeData.getValue(), 3));

		} catch (SQLException ea) {
			// TODO Auto-generated catch block
			ea.printStackTrace();
		} catch (NoUserFoundException eb) {
			// TODO Auto-generated catch block
			eb.printStackTrace();
		} catch (YouHaveNoFriendsExeption ec) {
			// TODO Auto-generated catch block
			ec.printStackTrace();
		} catch (NoPlaceFoundException ed) {
			// TODO Auto-generated catch block
			ed.printStackTrace();
		}
	}
}
