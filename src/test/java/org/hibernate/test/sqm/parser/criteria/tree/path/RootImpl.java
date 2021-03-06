/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.test.sqm.parser.criteria.tree.path;

import java.io.Serializable;
import javax.persistence.criteria.Root;

import org.hibernate.orm.persister.entity.spi.EntityPersister;
import org.hibernate.query.sqm.produce.spi.criteria.CriteriaVisitor;
import org.hibernate.query.sqm.produce.spi.criteria.from.JpaFrom;
import org.hibernate.query.sqm.produce.spi.criteria.from.JpaRoot;
import org.hibernate.query.sqm.produce.spi.criteria.path.JpaPathSource;
import org.hibernate.query.sqm.tree.expression.SqmExpression;
import org.hibernate.query.sqm.tree.select.SqmAliasedExpressionContainer;

import org.hibernate.test.sqm.parser.criteria.tree.CriteriaBuilderImpl;

/**
 * Hibernate implementation of the JPA {@link Root} contract
 *
 * @author Steve Ebersole
 */
public class RootImpl<X> extends AbstractFromImpl<X,X> implements JpaRoot<X>, Serializable {
	private final EntityPersister entityType;
	private final boolean allowJoins;

	public RootImpl(CriteriaBuilderImpl criteriaBuilder, EntityPersister entityType) {
		this( criteriaBuilder, entityType, true );
	}

	public RootImpl(CriteriaBuilderImpl criteriaBuilder, EntityPersister entityType, boolean allowJoins) {
//		super( criteriaBuilder, entityType.getJavaType() );
		super( criteriaBuilder, entityType, null );
		this.entityType = entityType;
		this.allowJoins = allowJoins;
	}

	@Override
	public EntityPersister getEntityType() {
		return entityType;
	}

	@Override
	public javax.persistence.metamodel.EntityType<X> getModel() {
		throw new UnsupportedOperationException(  );
	}

	@Override
	protected JpaFrom<X, X> createCorrelationDelegate() {
		return new RootImpl<X>( criteriaBuilder(), getEntityType() );
	}

//	@Override
//	public RootImpl<X> correlateTo(CriteriaSubqueryImpl subquery) {
//		return (RootImpl<X>) super.correlateTo( subquery );
//	}

	@Override
	protected boolean canBeJoinSource() {
		return allowJoins;
	}

	@Override
	@SuppressWarnings("ThrowableResultOfMethodCallIgnored")
	protected RuntimeException illegalJoin() {
		return allowJoins ? super.illegalJoin() : new IllegalArgumentException( "UPDATE/DELETE criteria queries cannot define joins" );
	}

	@Override
	@SuppressWarnings("ThrowableResultOfMethodCallIgnored")
	protected RuntimeException illegalFetch() {
		return allowJoins ? super.illegalFetch() : new IllegalArgumentException( "UPDATE/DELETE criteria queries cannot define fetches" );
	}

	@Override
	public String getPathIdentifier() {
		return getAlias();
	}

	@Override
	public EntityPersister getNavigable() {
		return entityType;
	}

	@Override
	public <T extends X> RootImpl<T> treatAs(Class<T> treatAsType) {
		return new TreatedRoot<T>( this, treatAsType );
	}

	@Override
	public SqmExpression visitExpression(CriteriaVisitor visitor) {
		return visitor.visitRoot( this );
	}

	@Override
	public void visitSelections(CriteriaVisitor visitor, SqmAliasedExpressionContainer container) {
		container.add( visitor.visitRoot( this ), getAlias() );
	}

	public static class TreatedRoot<T> extends RootImpl<T> {
		private final RootImpl<? super T> original;
		private final Class<T> treatAsType;

		public TreatedRoot(RootImpl<? super T> original, Class<T> treatAsType) {
			super(
					original.criteriaBuilder(),
					(EntityPersister) original.criteriaBuilder().consumerContext().getDomainMetamodel().resolveEntityReference( treatAsType )
			);
			this.original = original;
			this.treatAsType = treatAsType;
		}

		@Override
		public String getAlias() {
			return original.getAlias();
		}

		protected String getTreatFragment() {
			return "treat(" + original.getAlias() + " as " + treatAsType.getName() + ")";
		}

		@Override
		public String getPathIdentifier() {
			return getTreatFragment();
		}

		@Override
		protected JpaPathSource<T> getPathSourceForSubPaths() {
			return this;
		}
	}

}
