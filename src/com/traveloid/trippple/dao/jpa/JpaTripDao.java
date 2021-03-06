package com.traveloid.trippple.dao.jpa;

import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;

import com.traveloid.trippple.dao.TripDao;
import com.traveloid.trippple.entity.Trip;
import com.traveloid.trippple.util.PersistenceManager;

public class JpaTripDao implements TripDao {
	private EntityManager manager = null;

	public JpaTripDao() {
		this.manager = PersistenceManager.getFactory().createEntityManager();
	}

	public void destroy() {
		if(this.manager.isOpen()) {
			this.manager.close();
		}
	}

	@SuppressWarnings("unchecked")
	// masque les erreurs li�es au cast (ligne 25). De base, il faut v�rifier la bonne ex�cution d'un cast
	@Override
	public List<Trip> findAll() {
		Query query = manager.createQuery("SELECT trip FROM Trip as trip");

		return query.getResultList();
	}

	@Override
	public Trip findById(Long id) {
		Trip result;

		try {
			result = manager.find(Trip.class, id);
		} catch(NoResultException e) {
			result = null;
		}

		return result;
	}

	@Override
	public Trip addTrip(Trip trip) { // Trip en retour : on pourra ex�cuter une m�thode sur la m�me ligne que l'ajout, ou v�rifier le bon ajout de l'entit� en v�rifiant que le
										// retour n'est pas �gal � null
		Trip result = null;

		manager.getTransaction().begin();
		try {
			manager.persist(trip);
			manager.getTransaction().commit();
			result = trip; // Si on a pas r�ussi l'ajout, on met le trip pass� en param�tres dans result
		} finally {
			if(manager.getTransaction().isActive()) {
				manager.getTransaction().rollback();
			}
		}

		return result;
	}

	@Override
	public void updateTrip(Trip trip) {
		manager.getTransaction().begin();
		try {
			manager.merge(trip);
			manager.getTransaction().commit();
		} finally {
			if(manager.getTransaction().isActive()) {
				manager.getTransaction().rollback();
			}
		}
	}

	@Override
	public void removeTrip(Trip trip) {
		manager.getTransaction().begin();

		try {
			manager.remove(trip);
			manager.getTransaction().commit();
		} finally {
			if(manager.getTransaction().isActive()) {
				manager.getTransaction().rollback();
			}
		}
	}

	private class MatchCampus implements Predicate<Trip> {
		private Pattern pattern;

		public MatchCampus(Pattern pattern) {
			this.pattern = pattern;
		}

		@Override
		public boolean test(Trip trip) {
			return pattern.matcher(trip.getDestination().getName()).find() || pattern.matcher(trip.getOrigin().getName()).find();
		}
	}

	@Override
	public List<Trip> findByCampus(String request) {
		Pattern pattern = Pattern.compile(Pattern.quote(request), Pattern.CASE_INSENSITIVE);

		return findAll().stream()
				.filter(new MatchCampus(pattern))
				.collect(Collectors.toList());
	}
}
