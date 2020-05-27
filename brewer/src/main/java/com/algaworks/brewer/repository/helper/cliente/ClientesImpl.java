package com.algaworks.brewer.repository.helper.cliente;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.algaworks.brewer.model.Cidade;
import com.algaworks.brewer.model.Cliente;
import com.algaworks.brewer.model.Endereco;
import com.algaworks.brewer.repository.filter.ClienteFilter;
import com.algaworks.brewer.repository.util.OrdinationBuilder;
import com.algaworks.brewer.repository.util.PaginationBuilder;

public class ClientesImpl implements ClientesQueries {

	@PersistenceContext
	private EntityManager manager;
	
	@Autowired
	private OrdinationBuilder ordinationBuilder;
	
	@Autowired
	private PaginationBuilder paginationBuilder;
	
	@Override
	@Transactional(readOnly = true)
	public Page<Cliente> filtrar(ClienteFilter filtro, Pageable pageable) {
		CriteriaBuilder builder = manager.getCriteriaBuilder();
		CriteriaQuery<Cliente> criteria = builder.createQuery(Cliente.class);
		Root<Cliente> root = criteria.from(Cliente.class);
		
		Join<Cliente, Endereco> joinEndereco = (Join) root.fetch("endereco", JoinType.INNER);
		Join<Endereco, Cidade> joinCidade = (Join) root.fetch("cidade", JoinType.LEFT);
		
		List<Predicate> predicates = adicionarFiltro(filtro, builder, root);
		criteria.where(predicates.toArray(new Predicate[predicates.size()]));
		
		ordinationBuilder.preparar(pageable, builder, criteria, root, root.get("codigo"));
		
		TypedQuery<Cliente> query = manager.createQuery(criteria);
		
		paginationBuilder.preparar(query, pageable);

		return new PageImpl<>(query.getResultList(), pageable, total(filtro));
	}
	
	@Transactional(readOnly = true)
	@Override
	public Cliente buscar(Long codigo) {
		CriteriaBuilder builder = manager.getCriteriaBuilder();
		CriteriaQuery<Cliente> criteria = builder.createQuery(Cliente.class);
		Root<Cliente> root = criteria.from(Cliente.class);
		
		Join<Cliente, Endereco> joinEndereco = (Join) root.fetch("endereco", JoinType.INNER);
		Join<Endereco, Cidade> joinCidade = (Join) root.fetch("cidade", JoinType.LEFT);

		Predicate predicate = builder.equal(root.get("codigo"), codigo);
		criteria.where(predicate);
		
		TypedQuery<Cliente> query = manager.createQuery(criteria);
		
		return query.getSingleResult();

	}
	
	private Long total(ClienteFilter filtro) {
		CriteriaBuilder builder = manager.getCriteriaBuilder();
		CriteriaQuery<Long> criteria = builder.createQuery(Long.class);
		Root<Cliente> root = criteria.from(Cliente.class);
		
		List<Predicate> predicates = adicionarFiltro(filtro, builder, root);
		criteria.where(predicates.toArray(new Predicate[predicates.size()]));
		
		criteria.select(builder.count(root));
		
		return manager.createQuery(criteria).getSingleResult();
	}
	
	private List<Predicate> adicionarFiltro(ClienteFilter filtro, CriteriaBuilder builder, Root<Cliente> root) {
		List<Predicate> predicates = new ArrayList<>();
			
		if (filtro != null) {
			if (!StringUtils.isEmpty(filtro.getNome())) {
				predicates.add(builder.like(root.get("nome"), filtro.getNome().toLowerCase() + "%"));
			}

			if (!StringUtils.isEmpty(filtro.getCpfOuCnpj())) {
				predicates.add(builder.equal(root.get("nome"), filtro.getCpfOuCnpjSemFormatacao()));
			}
		}
		
		return predicates;
	}

}