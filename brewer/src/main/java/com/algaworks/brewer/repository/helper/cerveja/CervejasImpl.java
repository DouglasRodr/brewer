package com.algaworks.brewer.repository.helper.cerveja;

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

import com.algaworks.brewer.dto.CervejaDTO;
import com.algaworks.brewer.dto.ValorItensEstoque;
import com.algaworks.brewer.model.Cerveja;
import com.algaworks.brewer.model.Estilo;
import com.algaworks.brewer.repository.filter.CervejaFilter;
import com.algaworks.brewer.repository.util.OrdinationBuilder;
import com.algaworks.brewer.repository.util.PaginationBuilder;
import com.algaworks.brewer.storage.FotoStorage;

public class CervejasImpl implements CervejasQueries {

	@PersistenceContext
	private EntityManager manager;
	
	@Autowired
	private OrdinationBuilder ordinationBuilder;
	
	@Autowired
	private PaginationBuilder paginationBuilder;
	
	@Autowired
	private FotoStorage fotoStorage;
	
	@Override
	@Transactional(readOnly = true)
	public Page<Cerveja> filtrar(CervejaFilter filtro, Pageable pageable) {
		CriteriaBuilder builder = manager.getCriteriaBuilder();
		CriteriaQuery<Cerveja> criteria = builder.createQuery(Cerveja.class);
		Root<Cerveja> root = criteria.from(Cerveja.class);
		
		Join<Cerveja, Estilo> joinEstilo = (Join) root.fetch("estilo", JoinType.INNER);
		
		List<Predicate> predicates = adicionarFiltro(filtro, builder, root);
		criteria.where(predicates.toArray(new Predicate[predicates.size()]));
		
		ordinationBuilder.preparar(pageable, builder, criteria, root, root.get("codigo"));
		
		TypedQuery<Cerveja> query = manager.createQuery(criteria);
		
		paginationBuilder.preparar(query, pageable);

		return new PageImpl<>(query.getResultList(), pageable, total(filtro));
	}
	
	@Transactional(readOnly = true)
	@Override
	public Cerveja buscar(Long codigo) {
		CriteriaBuilder builder = manager.getCriteriaBuilder();
		CriteriaQuery<Cerveja> criteria = builder.createQuery(Cerveja.class);
		Root<Cerveja> root = criteria.from(Cerveja.class);
		
		Join<Cerveja, Estilo> joinEstilo = (Join) root.fetch("estilo", JoinType.INNER);
		
		Predicate predicate = builder.equal(root.get("codigo"), codigo);
		criteria.where(predicate);
		
		TypedQuery<Cerveja> query = manager.createQuery(criteria);
		
		return query.getSingleResult();
	}
	
	@Override
	public List<CervejaDTO> porSkuOuNome(String skuOuNome) {
		String jpql = "select new com.algaworks.brewer.dto.CervejaDTO(codigo, sku, nome, origem, valor, foto) "
				+ "from Cerveja where lower(sku) like lower(:skuOuNome) or lower(nome) like lower(:skuOuNome)";
		List<CervejaDTO> cervejasFiltradas = manager.createQuery(jpql, CervejaDTO.class)
					.setParameter("skuOuNome", skuOuNome + "%")
					.getResultList();
		cervejasFiltradas.forEach(c -> c.setUrlThumbnailFoto(fotoStorage.getUrl(FotoStorage.THUMBNAIL_PREFIX + c.getFoto())));
		return cervejasFiltradas;
	}
	
	@Override
	public ValorItensEstoque valorItensEstoque() {
		String query = "select new com.algaworks.brewer.dto.ValorItensEstoque(sum(valor * quantidadeEstoque), sum(quantidadeEstoque)) from Cerveja";
		return manager.createQuery(query, ValorItensEstoque.class).getSingleResult();
	}
	
	private Long total(CervejaFilter filtro) {
		CriteriaBuilder builder = manager.getCriteriaBuilder();
		CriteriaQuery<Long> criteria = builder.createQuery(Long.class);
		Root<Cerveja> root = criteria.from(Cerveja.class);
		
		List<Predicate> predicates = adicionarFiltro(filtro, builder, root);
		criteria.where(predicates.toArray(new Predicate[predicates.size()]));
		
		criteria.select(builder.count(root));
		
		return manager.createQuery(criteria).getSingleResult();
	}
	
	private List<Predicate> adicionarFiltro(CervejaFilter filtro, CriteriaBuilder builder, Root<Cerveja> root) {
		List<Predicate> predicates = new ArrayList<>();
		
		if (filtro != null) {
			if (!StringUtils.isEmpty(filtro.getSku())) {
				predicates.add(builder.equal(root.get("sku"), filtro.getSku()));
			}
			
			if (!StringUtils.isEmpty(filtro.getNome())) {
				predicates.add(builder.like(root.get("nome"), filtro.getNome().toLowerCase() + "%"));
			}
			
			if (filtro.getEstilo() != null && filtro.getEstilo().getCodigo() != null) {
				predicates.add(builder.equal(root.get("estilo").get("codigo"), filtro.getEstilo().getCodigo()));
			}
			
			if (filtro.getSabor() != null) {
				predicates.add(builder.equal(root.get("sabor"), filtro.getSabor()));
			}
			
			if (filtro.getOrigem() != null) {
				predicates.add(builder.equal(root.get("origem"), filtro.getOrigem()));
			}
			
			if (filtro.getValorDe() != null) {
				predicates.add(builder.greaterThanOrEqualTo(root.get("valor"), filtro.getValorDe()));
			}

			if (filtro.getValorAte() != null) {
				predicates.add(builder.lessThanOrEqualTo(root.get("valor"), filtro.getValorAte()));
			}
			
		}
		
		return predicates;
	}

}