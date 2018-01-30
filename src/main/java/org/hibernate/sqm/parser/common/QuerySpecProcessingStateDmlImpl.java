/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.parser.common;

import java.util.Collections;
import java.util.List;

import org.hibernate.sqm.domain.AttributeDescriptor;
import org.hibernate.sqm.domain.EntityDescriptor;
import org.hibernate.sqm.parser.ParsingException;
import org.hibernate.sqm.query.JoinType;
import org.hibernate.sqm.query.PropertyPath;
import org.hibernate.sqm.query.expression.domain.AttributeReference;
import org.hibernate.sqm.query.expression.domain.SqmNavigableReference;
import org.hibernate.sqm.query.from.FromElementSpace;
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
	public SqmFromClause getFromClause() {
		return fromClause;
	}

	@Override
	public SqmNavigableReference findFromElementByIdentificationVariable(String identificationVariable) {
		return fromClause.fromElementSpace.getRoot().getIdentificationVariable().equals( identificationVariable )
				? fromClause.fromElementSpace.getRoot().getDomainReferenceBinding()
				: null;
	}

	@Override
	public SqmNavigableReference findFromElementExposingAttribute(String attributeName) {
		if ( rootExposesAttribute( attributeName ) ) {
			return fromClause.fromElementSpace.getRoot().getDomainReferenceBinding();
		}
		else {
			return null;
		}
	}

	private boolean rootExposesAttribute(String attributeName) {
		final AttributeDescriptor attrRef = getParsingContext().getConsumerContext()
				.getDomainMetamodel()
				.locateAttributeDescriptor( fromClause.fromElementSpace.getRoot().getDomainReferenceBinding().getBoundDomainReference(), attributeName );
		return attrRef != null;
	}

	@Override
	public FromElementLocator getFromElementLocator() {
		return this;
	}

	@Override
	public FromElementBuilder getFromElementBuilder() {
		return fromElementBuilder;
	}

	public static class DmlFromClause extends SqmFromClause {
		private final DmlFromElementSpace fromElementSpace = new DmlFromElementSpace( this );

		@Override
		public List<FromElementSpace> getFromElementSpaces() {
			return Collections.singletonList( fromElementSpace );
		}

		@Override
		public void addFromElementSpace(FromElementSpace space) {
			throw new ParsingException( "DML from-clause cannot have additional FromElementSpaces" );
		}

		@Override
		public FromElementSpace makeFromElementSpace() {
			throw new ParsingException( "DML from-clause cannot have additional FromElementSpaces" );
		}
	}

	public static class DmlFromElementSpace extends FromElementSpace {
		private DmlFromElementSpace(DmlFromClause fromClause) {
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
				FromElementSpace fromElementSpace, String uid, EntityDescriptor entityType, String alias) {
			throw new ParsingException( "DML from-clause cannot define joins" );
		}

		@Override
		public SqmEntityJoin buildEntityJoin(
				FromElementSpace fromElementSpace,
				String alias,
				EntityDescriptor entityType,
				JoinType joinType) {
			throw new ParsingException( "DML from-clause cannot define joins" );
		}

		@Override
		public SqmAttributeJoin buildAttributeJoin(
				AttributeReference attributeBinding,
				String alias,
				EntityDescriptor subclassIndicator,
				PropertyPath path,
				JoinType joinType,
				String lhsUniqueIdentifier,
				boolean fetched,
				boolean canReuseImplicitJoins) {
			throw new ParsingException( "DML from-clause cannot define joins" );
		}
	}
}
