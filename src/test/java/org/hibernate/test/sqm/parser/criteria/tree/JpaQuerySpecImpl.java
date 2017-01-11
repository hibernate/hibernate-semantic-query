/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.test.sqm.parser.criteria.tree;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import javax.persistence.criteria.AbstractQuery;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Selection;
import javax.persistence.criteria.Subquery;
import javax.persistence.metamodel.EntityType;

import org.hibernate.orm.persister.entity.spi.EntityPersister;
import org.hibernate.sqm.NotYetImplementedException;
import org.hibernate.sqm.parser.criteria.tree.JpaExpression;
import org.hibernate.sqm.parser.criteria.tree.JpaOrder;
import org.hibernate.sqm.parser.criteria.tree.JpaPredicate;
import org.hibernate.sqm.parser.criteria.tree.JpaQuerySpec;
import org.hibernate.sqm.parser.criteria.tree.JpaSubquery;
import org.hibernate.sqm.parser.criteria.tree.from.JpaFromClause;
import org.hibernate.sqm.parser.criteria.tree.select.JpaSelectClause;
import org.hibernate.sqm.parser.criteria.tree.select.JpaSelection;

import org.hibernate.test.sqm.parser.criteria.tree.path.RootImpl;

/**
 * Models basic query structure.  Used as a delegate in implementing both
 * {@link javax.persistence.criteria.CriteriaQuery} and
 * {@link Subquery}.
 * <p/>
 * Note the <tt>ORDER BY</tt> specs are neglected here.  That's because it is not valid
 * for a subquery to define an <tt>ORDER BY</tt> clause.  So we just handle them on the
 * root query directly...
 *
 * @author Steve Ebersole
 */
public class JpaQuerySpecImpl<T> implements JpaQuerySpec<T>, Serializable {
	private final AbstractQuery<T> owner;
	private final CriteriaBuilderImpl criteriaBuilder;
	private final boolean isSubQuery;

	public JpaQuerySpecImpl(AbstractQuery<T> owner, CriteriaBuilderImpl criteriaBuilder) {
		this.owner = owner;
		this.criteriaBuilder = criteriaBuilder;
		this.isSubQuery = Subquery.class.isInstance( owner );
	}

	private JpaSelectClauseImpl<T> jpaSelectClause = new JpaSelectClauseImpl<>();
	private FromClauseImpl fromClause = new FromClauseImpl();
	private JpaPredicate restriction;
	private List<JpaExpression<?>> groupings = Collections.emptyList();
	private JpaPredicate having;
	private List<JpaOrder> jpaOrderByList;
	private List<JpaSubquery<?>> subqueries;



	// SELECTION ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

	public boolean isDistinct() {
		return jpaSelectClause.isDistinct();
	}

	public void setDistinct(boolean distinct) {
		jpaSelectClause.setDistinct( distinct );
	}

	public Selection<? extends T> getSelection() {
		return jpaSelectClause.getSelection();
	}

	public void setSelection(JpaSelection<? extends T> selection) {
		jpaSelectClause.setJpaSelection( selection );
	}


	// ROOTS ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

	@SuppressWarnings("unchecked")
	public <X> Root<X> from(Class<X> entityClass) {
		EntityPersister<X> persister = (EntityPersister<X>) criteriaBuilder.consumerContext()
				.getDomainMetamodel()
				.resolveEntityReference( entityClass );
		if ( persister == null ) {
			throw new IllegalArgumentException( entityClass + " is not an entity" );
		}
		return from( persister );
	}

	private <X> Root<X> from(EntityPersister<X> entityType) {
		RootImpl<X> root = new RootImpl<X>( criteriaBuilder, entityType );
		fromClause.addRoot( root );
		return root;
	}

	@SuppressWarnings("unchecked")
	public <X> Root<X> from(String entityName) {
		EntityPersister<X> entityType = (EntityPersister<X>) criteriaBuilder.consumerContext()
				.getDomainMetamodel()
				.resolveEntityReference( entityName );
		if ( entityType == null ) {
			throw new IllegalArgumentException( entityName + " is not an entity" );
		}
		return from( entityType );
	}

	public <X> Root<X> from(EntityType<X> entityType) {
		throw new NotYetImplementedException(  );
//		RootImpl<X> root = new RootImpl<X>( criteriaBuilder, entityType );
//		roots.add( root );
//		return root;
	}


	// CORRELATION ROOTS ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
//
//	public void addCorrelationRoot(JpaFrom<?,?> jpaFrom) {
//		if ( !isSubQuery ) {
//			throw new IllegalStateException( "Query is not identified as sub-query" );
//		}
//		if ( correlationRoots == null ) {
//			correlationRoots = new HashSet<>();
//		}
//		correlationRoots.add( jpaFrom );
//	}
//
//	public Set<JpaAttributeJoin<?, ?>> collectCorrelatedJoins() {
//		if ( !isSubQuery ) {
//			throw new IllegalStateException( "Query is not identified as sub-query" );
//		}
//		final Set<JpaAttributeJoin<?, ?>> correlatedJoins;
//		if ( correlationRoots != null ) {
//			correlatedJoins = new HashSet<>();
//			for ( JpaFrom<?,?> correlationRoot : correlationRoots ) {
//				if (correlationRoot instanceof JpaAttributeJoin<?,?> && correlationRoot.isCorrelated()) {
//					correlatedJoins.add( (JpaAttributeJoin<?,?>) correlationRoot );
//				}
//				correlationRoot.getJoins().forEach(
//						join -> correlatedJoins.add( (JpaAttributeJoin<?, ?>) join )
//				);
//			}
//		}
//		else {
//			correlatedJoins = Collections.emptySet();
//		}
//		return correlatedJoins;
//	}

	@Override
	public JpaSelectClause getSelectClause() {
		return jpaSelectClause;
	}

	@Override
	public JpaFromClause getFromClause() {
		return fromClause;
	}


	public void clearOrderList() {
		if ( jpaOrderByList != null ) {
			jpaOrderByList.clear();
			jpaOrderByList = null;
		}
	}

	public void addOrderBy(JpaOrder order) {
		if ( jpaOrderByList == null ) {
			jpaOrderByList = new ArrayList<>();
		}
		jpaOrderByList.add( order );
	}

	@Override
	public List<JpaOrder> getOrderList() {
		return jpaOrderByList == null ? Collections.emptyList() : Collections.unmodifiableList( jpaOrderByList );
	}


	// RESTRICTIONS ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~


	public JpaPredicate getRestriction() {
		return restriction;
	}

	public void setRestriction(JpaPredicate restriction) {
		this.restriction = restriction;
	}


	// GROUPINGS ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

	public List<JpaExpression<?>> getGroupings() {
		return groupings;
	}

	public void setGroupings(List<JpaExpression<?>> groupings) {
		this.groupings = groupings;
	}

	public void setGroupings(JpaExpression<?>... groupings) {
		if ( groupings != null && groupings.length > 0 ) {
			this.groupings = Arrays.asList( groupings );
		}
		else {
			this.groupings = Collections.emptyList();
		}
	}

	public JpaPredicate getHaving() {
		return having;
	}

	public void setHaving(JpaPredicate having) {
		this.having = having;
	}


	// SUB-QUERIES ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

	public List<JpaSubquery<?>> getSubqueries() {
		return subqueries;
	}

	public List<JpaSubquery<?>> internalGetSubqueries() {
		if ( subqueries == null ) {
			subqueries = new ArrayList<>();
		}
		return subqueries;
	}

	public <U> Subquery<U> subquery(Class<U> subqueryType) {
//		CriteriaSubqueryImpl<U> subquery = new CriteriaSubqueryImpl<U>( criteriaBuilder, subqueryType, owner );
//		internalGetSubqueries().add( subquery );
//		return subquery;

		throw new NotYetImplementedException(  );
	}


	// PARAMETERS ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

	public Set<ParameterExpression<?>> collectParameters() {
		final Set<ParameterExpression<?>> parameters = new LinkedHashSet<ParameterExpression<?>>();
//		final ParameterRegistry registry = new ParameterRegistry() {
//			public void registerParameter(ParameterExpression<?> parameter) {
//				parameters.add( parameter );
//			}
//		};
//
//		ParameterContainer.Helper.possibleParameter(selection, registry);
//		ParameterContainer.Helper.possibleParameter(restriction, registry);
//		ParameterContainer.Helper.possibleParameter(having, registry);
//		if ( subqueries != null ) {
//			for ( Subquery subquery : subqueries ) {
//				ParameterContainer.Helper.possibleParameter(subquery, registry);
//			}
//		}
//
//		// both group-by and having expressions can (though unlikely) contain parameters...
//		ParameterContainer.Helper.possibleParameter(having, registry);
//		if ( groupings != null ) {
//			for ( Expression<?> grouping : groupings ) {
//				ParameterContainer.Helper.possibleParameter(grouping, registry);
//			}
//		}
//
		return parameters;
	}
}
