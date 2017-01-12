/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.parser.common;

import java.util.Collections;
import java.util.List;

import org.hibernate.sqm.domain.SqmExpressableTypeEmbedded;
import org.hibernate.sqm.domain.SqmExpressableTypeEntity;
import org.hibernate.sqm.domain.SqmNavigable;
import org.hibernate.sqm.parser.ParsingException;
import org.hibernate.sqm.query.SqmJoinType;
import org.hibernate.sqm.query.expression.domain.SqmAttributeBinding;
import org.hibernate.sqm.query.expression.domain.SqmNavigableBinding;
import org.hibernate.sqm.query.from.SqmFromElementSpace;
import org.hibernate.sqm.query.from.SqmAttributeJoin;
import org.hibernate.sqm.query.from.SqmCrossJoin;
import org.hibernate.sqm.query.from.SqmEntityJoin;
import org.hibernate.sqm.query.from.SqmFromClause;
import org.hibernate.sqm.query.from.SqmJoin;
import org.hibernate.sqm.query.from.SqmRoot;

/**
 * QuerySpecProcessingState implementation for DML statements
 *
 * @author Steve Ebersole
 */
public class QuerySpecProcessingStateDmlImpl extends AbstractQuerySpecProcessingState {
	private final DmlFromClause fromClause;

	private final FromElementBuilder fromElementBuilder;

	public QuerySpecProcessingStateDmlImpl(ParsingContext parsingContext) {
		// implicitly no outer query, so pass null
		super( parsingContext, null );
		this.fromClause = new DmlFromClause();
		this.fromElementBuilder = new DmlFromElementBuilder( parsingContext, new AliasRegistry() );
	}

	@Override
	public DmlFromClause getFromClause() {
		return fromClause;
	}

	@Override
	public SqmNavigableBinding findNavigableBindingByIdentificationVariable(String identificationVariable) {
		return fromClause.fromElementSpace.getRoot().getIdentificationVariable().equals( identificationVariable )
				? fromClause.fromElementSpace.getRoot().getBinding()
				: null;
	}

	@Override
	public SqmNavigableBinding findNavigableBindingExposingAttribute(String attributeName) {
		if ( rootExposesAttribute( attributeName ) ) {
			return fromClause.fromElementSpace.getRoot().getBinding();
		}
		else {
			return null;
		}
	}

	private boolean rootExposesAttribute(String attributeName) {
		final SqmNavigable sqmNavigable = getParsingContext().getConsumerContext()
				.getDomainMetamodel()
				.locateNavigable( fromClause.fromElementSpace.getRoot().getBinding().getBoundNavigable(), attributeName );
		return sqmNavigable != null;
	}

	@Override
	public FromElementLocator getFromElementLocator() {
		return this;
	}

	@Override
	public FromElementBuilder getFromElementBuilder() {
		return fromElementBuilder;
	}

	public DmlSqmFromElementSpace getDmlFromElementSpace() {
		return fromClause.fromElementSpace;
	}

	public static class DmlFromClause extends SqmFromClause {
		private final DmlSqmFromElementSpace fromElementSpace = new DmlSqmFromElementSpace( this );

		@Override
		public List<SqmFromElementSpace> getFromElementSpaces() {
			return Collections.singletonList( fromElementSpace );
		}

		@Override
		public void addFromElementSpace(SqmFromElementSpace space) {
			throw new ParsingException( "DML from-clause cannot have additional FromElementSpaces" );
		}

		@Override
		public SqmFromElementSpace makeFromElementSpace() {
			throw new ParsingException( "DML from-clause cannot have additional FromElementSpaces" );
		}
	}

	public static class DmlSqmFromElementSpace extends SqmFromElementSpace {
		private DmlSqmFromElementSpace(DmlFromClause fromClause) {
			super( fromClause );
		}

		@Override
		public void setRoot(SqmRoot root) {
			super.setRoot( root );
		}

		@Override
		public List<SqmJoin> getJoins() {
			return Collections.emptyList();
		}

		@Override
		public void addJoin(SqmJoin join) {
			throw new ParsingException( "DML from-clause cannot define joins" );
		}
	}

	public static class DmlFromElementBuilder extends FromElementBuilder {
		public DmlFromElementBuilder(ParsingContext parsingContext, AliasRegistry aliasRegistry) {
			super( parsingContext, aliasRegistry );
		}

		@Override
		public SqmCrossJoin makeCrossJoinedFromElement(
				SqmFromElementSpace fromElementSpace, String uid, SqmExpressableTypeEntity entityType, String alias) {
			throw new ParsingException( "DML from-clause cannot define joins" );
		}

		@Override
		public SqmEntityJoin buildEntityJoin(
				SqmFromElementSpace fromElementSpace,
				String alias,
				SqmExpressableTypeEntity entityType,
				SqmJoinType joinType) {
			throw new ParsingException( "DML from-clause cannot define joins" );
		}

		@Override
		public SqmAttributeJoin buildAttributeJoin(
				SqmAttributeBinding attributeBinding,
				String alias,
				SqmExpressableTypeEntity subclassIndicator,
				SqmJoinType joinType,
				boolean fetched,
				boolean canReuseImplicitJoins) {
			if ( SqmExpressableTypeEmbedded.class.isInstance( attributeBinding.getBoundNavigable() ) ) {
				return super.buildAttributeJoin(
						attributeBinding,
						alias,
						subclassIndicator,
						joinType,
						fetched,
						canReuseImplicitJoins
				);
			}
			throw new ParsingException( "DML from-clause cannot define joins" );
		}
	}
}
