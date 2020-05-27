package com.algaworks.brewer.repository.helper.usuario;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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

import com.algaworks.brewer.model.Grupo;
import com.algaworks.brewer.model.Usuario;
import com.algaworks.brewer.model.UsuarioGrupo;
import com.algaworks.brewer.model.UsuarioGrupoId;
import com.algaworks.brewer.repository.filter.UsuarioFilter;
import com.algaworks.brewer.repository.util.OrdinationBuilder;
import com.algaworks.brewer.repository.util.PaginationBuilder;

public class UsuariosImpl implements UsuariosQueries {

	@PersistenceContext
	private EntityManager manager;

	@Autowired
	private OrdinationBuilder ordinationBuilder;
	
	@Autowired
	private PaginationBuilder paginationBuilder;
	
	@Override
	public Optional<Usuario> porEmailEAtivo(String email) {
		return manager
				.createQuery("from Usuario where lower(email) = lower(:email) and ativo = true", Usuario.class)
				.setParameter("email", email).getResultList().stream().findFirst();
	}

	@Override
	public List<String> permissoes(Usuario usuario) {
		return manager.createQuery(
				"select distinct p.nome from Usuario u inner join u.grupos g inner join g.permissoes p where u = :usuario", String.class)
				.setParameter("usuario", usuario)
				.getResultList();
	}

	@Override
	@Transactional(readOnly = true)
	public Page<Usuario> filtrar(UsuarioFilter filtro, Pageable pageable) {
		CriteriaBuilder builder = manager.getCriteriaBuilder();
		CriteriaQuery<Usuario> criteria = builder.createQuery(Usuario.class);
		Root<Usuario> root = criteria.from(Usuario.class);		
		
		criteria.distinct(true);
		
		List<Predicate> predicates = adicionarFiltro(filtro, builder, root);
		criteria.where(predicates.toArray(new Predicate[predicates.size()]));
		
		ordinationBuilder.preparar(pageable, builder, criteria, root, root.get("codigo"));
		
		TypedQuery<Usuario> query = manager.createQuery(criteria);
		
		paginationBuilder.preparar(query, pageable);

		return new PageImpl<>(query.getResultList(), pageable, total(filtro));
	}

	@Transactional(readOnly = true)
	@Override
	public Usuario buscar(Long codigo) {
		CriteriaBuilder builder = manager.getCriteriaBuilder();
		CriteriaQuery<Usuario> criteria = builder.createQuery(Usuario.class);
		Root<Usuario> root = criteria.from(Usuario.class);
		
		Join<Usuario, Grupo> joinGrupo = (Join) root.fetch("grupos", JoinType.LEFT);
		
		Predicate predicate = builder.equal(root.get("codigo"), codigo);
		criteria.where(predicate);
		
		TypedQuery<Usuario> query = manager.createQuery(criteria);
		
		return query.getSingleResult();
	}	
	
	@Transactional(readOnly = true)
	@Override
	public List<UsuarioGrupo> buscarGrupos(List<Usuario> usuarios) {
		CriteriaBuilder builder = manager.getCriteriaBuilder();
		CriteriaQuery<UsuarioGrupo> criteria = builder.createQuery(UsuarioGrupo.class);
		Root<UsuarioGrupo> root = criteria.from(UsuarioGrupo.class);		
		
		Join<UsuarioGrupo, UsuarioGrupoId> joinUsuarioGrupoId = (Join) root.fetch("id");
		Join<UsuarioGrupoId, Usuario> joinUsuario = (Join) joinUsuarioGrupoId.fetch("usuario");
		Join<UsuarioGrupoId, Grupo> joinGrupo = (Join) joinUsuarioGrupoId.fetch("grupo");
		
		Predicate predicate = joinUsuario.get("codigo")
				.in(usuarios.stream().map(Usuario::getCodigo).collect(Collectors.toList()));
		
		TypedQuery<UsuarioGrupo> query = manager.createQuery(criteria);
		
		return query.getResultList();
	}
	
	private Long total(UsuarioFilter filtro) {
		CriteriaBuilder builder = manager.getCriteriaBuilder();
		CriteriaQuery<Long> criteria = builder.createQuery(Long.class);
		Root<Usuario> root = criteria.from(Usuario.class);
		
		List<Predicate> predicates = adicionarFiltro(filtro, builder, root);
		criteria.where(predicates.toArray(new Predicate[predicates.size()]));
		
		criteria.select(builder.countDistinct(root));
		
		return manager.createQuery(criteria).getSingleResult();
	}
	
	private List<Predicate> adicionarFiltro(UsuarioFilter filtro, CriteriaBuilder builder, Root<Usuario> root) {
		List<Predicate> predicates = new ArrayList<>();
		
		if (filtro != null) {
			if (!StringUtils.isEmpty(filtro.getNome())) {
				predicates.add(builder.like(root.get("nome"), "%" + filtro.getNome().toLowerCase().replace(" ", "%") + "%"));
			}
			
			if (!StringUtils.isEmpty(filtro.getEmail())) {
				predicates.add(builder.like(root.get("email"), filtro.getEmail().toLowerCase() + "%"));
			}
			
			if (filtro.getGrupos() != null && !filtro.getGrupos().isEmpty()) {
				Join<Usuario, Grupo> joinGrupo = root.join("grupos", JoinType.INNER);
				predicates.add(joinGrupo.get("codigo").in(filtro.getGrupos().stream().map(Grupo::getCodigo).collect(Collectors.toList())));
			}
			
		}
		
		return predicates;
	}	

}