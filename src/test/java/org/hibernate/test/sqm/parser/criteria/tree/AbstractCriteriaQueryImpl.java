/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.test.sqm.parser.criteria.tree;

import java.util.List;
import java.util.Set;
import javax.persistence.criteria.AbstractQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Selection;
import javax.persistence.criteria.Subquery;
import javax.persistence.metamodel.EntityType;

/**
 * @author Steve Ebersole
 */
public class AbstractCriteriaQueryImpl<T> implements AbstractQuery<T> {
	@Override
	public <X> Root<X> from(Class<X> entityClass) {
		return null;
	}

	@Override
	public <X> Root<X> from(EntityType<X> entity) {
		return null;
	}

	@Override
	public AbstractQuery<T> where(Expression<Boolean> restriction) {
		return null;
	}

	@Override
	public AbstractQuery<T> where(Predicate... restrictions) {
		return null;
	}

	@Override
	public AbstractQuery<T> groupBy(Expression<?>... grouping) {
		return null;
	}

	@Override
	public AbstractQuery<T> groupBy(List<Expression<?>> grouping) {
		return null;
	}

	@Override
	public AbstractQuery<T> having(Expression<Boolean> restriction) {
		return null;
	}

	@Override
	public AbstractQuery<T> having(Predicate... restrictions) {
		return null;
	}

	@Override
	public AbstractQuery<T> distinct(boolean distinct) {
		return null;
	}

	@Override
	public Set<Root<?>> getRoots() {
		return null;
	}

	@Override
	public Selection<T> getSelection() {
		return null;
	}

	@Override
	public List<Expression<?>> getGroupList() {
		return null;
	}

	@Override
	public Predicate getGroupRestriction() {
		return null;
	}

	@Override
	public boolean isDistinct() {
		return false;
	}

	@Override
	public Class<T> getResultType() {
		return null;
	}

	@Override
	public <U> Subquery<U> subquery(Class<U> type) {
		return null;
	}

	@Override
	public Predicate getRestriction() {
		return null;
	}
}
