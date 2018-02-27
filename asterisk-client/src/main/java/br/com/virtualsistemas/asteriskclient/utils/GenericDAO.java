package br.com.virtualsistemas.asteriskclient.utils;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaQuery;

@Stateless
public class GenericDAO{

	@Inject
	private EntityManager em;

	public <T> T find(Class<T> entityClass, Serializable primaryKey) {
		return em.find(entityClass, primaryKey);
	}
	
	public void delete(Class<?> entityClass, Serializable primaryKey) {
		em.remove(em.getReference(entityClass, primaryKey));
	}
	
	public <T> T insert(T entity) {
		em.persist(entity);
		return entity;
	}
	
	public <T> T update(T entity) {
		em.merge(entity);
		return entity;
	}

	@SuppressWarnings("unchecked")
	public List<Object> getResultList(QueryType queryType, String query, Map<String, Object> parameters) {
		Query q = createQuery(queryType, query);
		populateQueryParameters(q, parameters);
		return q.getResultList();
	}

	public int executeQuery(QueryType queryType, String query, Map<String, Object> parameters) {
		Query q = createQuery(queryType, query);
		populateQueryParameters(q, parameters);
		return q.executeUpdate();
	}

	public Object getOneResult(QueryType queryType, String query, Map<String, Object> parameters) {
		Query q = createQuery(queryType, query);
		populateQueryParameters(q, parameters);
		try {
			return q.getSingleResult();		
		} catch (NoResultException | NonUniqueResultException e) {
			return null;
		}
	}
	
	@SuppressWarnings({ "unchecked" })
	public List<?> findAll(Class<?> entityClass, String orderBy) {

		if(orderBy != null){
			List<?> result = null;
			Query query = em.createQuery("from " + entityClass.getSimpleName() + " o "+ "order by o."+orderBy);
			result = query.getResultList();
			return result;
		}
		else{
			@SuppressWarnings("rawtypes")
			CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
			cq.select(cq.from(entityClass));
			return em.createQuery(cq).getResultList();
		}
	}

	public List<?> getResultList(Class<?> entityClass, QueryType queryType, String query, Map<String, Object> parameters, int maxResults){
		List<?> result = null;
		try {
			Query q = createQuery(queryType, query);
			if(maxResults > 0){
				q.setMaxResults(maxResults);
			}
			

			// Method that will populate parameters if they are passed not null and empty
			if (parameters != null && !parameters.isEmpty()) {
				populateQueryParameters(q, parameters);
			}

			result = q.getResultList();
		} catch (NoResultException e) {	}

		return result;
	}


	// Using the unchecked because JPA does not have a
	// ery.getSingleResult()<T> method
	public Class<?> getOneResult(Class<?> entityClass, QueryType queryType, String query, Map<String, Object> parameters) {
		Class<?> result = null;
		try {
			Query q = createQuery(queryType, query);

			// Method that will populate parameters if they are passed not null and empty
			if (parameters != null && !parameters.isEmpty()) {
				populateQueryParameters(q, parameters);
			}

			result = (Class<?>) q.getSingleResult();

		} catch (NoResultException nre) {
		} catch (Exception e) {
			System.out.println("Error while running query: " + e.getMessage());
			e.printStackTrace();
		}

		return result;
	}

	
	protected Query createQuery(QueryType queryType, String query) {
		switch (queryType) {
			case NATIVE: return em.createNativeQuery(query);
			case NAMED: return em.createNamedQuery(query);
			case DEFAULT: return em.createQuery(query);
			default: return null;
		}
	}
	
	protected void populateQueryParameters(Query query, Map<String, Object> parameters) {
		if (!((parameters == null) || (parameters.isEmpty()))) {
			parameters.forEach((name, value)->query.setParameter(name, value));
		}
	}
}