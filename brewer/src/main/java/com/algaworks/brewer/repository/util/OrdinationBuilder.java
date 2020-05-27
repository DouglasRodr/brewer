package com.algaworks.brewer.repository.util;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Root;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

@Component
public class OrdinationBuilder {

	public void preparar(Pageable pageable, CriteriaBuilder builder, CriteriaQuery<?> criteria, Root<?> root,
			Path<?> defaultField) {
		Sort sort = pageable.getSort();
		if (sort != null && sort.iterator().hasNext()) {
			Sort.Order order = sort.iterator().next();
			String property = order.getProperty();
			criteria.orderBy(order.isAscending() ? builder.asc(root.get(property)) : builder.desc(root.get(property)));				
		} else {
			criteria.orderBy(builder.asc(defaultField));
		}		
	}

}
