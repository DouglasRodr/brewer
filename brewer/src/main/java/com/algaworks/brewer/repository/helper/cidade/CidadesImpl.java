package com.algaworks.brewer.repository.helper.cidade;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.algaworks.brewer.model.Cidade;
import com.algaworks.brewer.repository.filter.CidadeFilter;
import com.algaworks.brewer.repository.util.OrdinationBuilder;
import com.algaworks.brewer.repository.util.PaginationBuilder;

public class CidadesImpl implements CidadesQueries {

	@PersistenceContext
	private EntityManager manager;
	
	@Autowired
	private OrdinationBuilder ordinationBuilder;
	
	@Autowired
	private PaginationBuilder paginationBuilder;
	
	@Override
	@Transactional(readOnly = true)
	public Page<Cidade> filtrar(CidadeFilter filtro, Pageable pageable) {
		CriteriaBuilder builder = manager.getCriteriaBuilder();
		CriteriaQuery<Cidade> criteria = builder.createQuery(Cidade.class);
		Root<Cidade> root = criteria.from(Cidade.class);
		
		List<Predicate> predicates = adicionarFiltro(filtro, builder, root);
		criteria.where(predicates.toArray(new Predicate[predicates.size()]));
		
		ordinationBuilder.preparar(pageable, builder, criteria, root, root.get("codigo"));
		
		TypedQuery<Cidade> query = manager.createQuery(criteria);
		
		paginationBuilder.preparar(query, pageable);

		return new PageImpl<>(query.getResultList(), pageable, total(filtro));
	}
	
	private Long total(CidadeFilter filtro) {
		CriteriaBuilder builder = manager.getCriteriaBuilder();
		CriteriaQuery<Long> criteria = builder.createQuery(Long.class);
		Root<Cidade> root = criteria.from(Cidade.class);
		
		List<Predicate> predicates = adicionarFiltro(filtro, builder, root);
		criteria.where(predicates.toArray(new Predicate[predicates.size()]));
		
		criteria.select(builder.count(root));
		
		return manager.createQuery(criteria).getSingleResult();
	}
	
	private List<Predicate> adicionarFiltro(CidadeFilter filtro, CriteriaBuilder builder, Root<Cidade> root) {
		List<Predicate> predicates = new ArrayList<>();
		
		if (filtro != null) {
			if (filtro.getEstado() != null) {
				predicates.add(builder.equal(root.get("estado"), filtro.getEstado()));
			}
			
			if (!StringUtils.isEmpty(filtro.getNome())) {
				predicates.add(builder.like(root.get("nome"), filtro.getNome().toLowerCase() + "%"));
			}
		}
		
		return predicates;		
	}

}