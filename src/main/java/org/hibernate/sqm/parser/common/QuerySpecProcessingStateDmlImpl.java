/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.parser.common;

import org.hibernate.sqm.domain.AttributeReference;
import org.hibernate.sqm.domain.EntityReference;
import org.hibernate.sqm.parser.ParsingException;
import org.hibernate.sqm.query.JoinType;
import org.hibernate.sqm.query.expression.domain.AttributeBinding;
import org.hibernate.sqm.query.expression.domain.DomainReferenceBinding;
import org.hibernate.sqm.query.from.FromElementSpace;
import org.hibernate.sqm.query.from.SqmAttributeJoin;
import org.hibernate.sqm.query.from.SqmCrossJoin;
import org.hibernate.sqm.query.from.SqmEntityJoin;
import org.hibernate.sqm.query.from.SqmFromClause;
import org.hibernate.sqm.query.from.SqmFromClauseContainer;
import org.hibernate.sqm.query.from.internal.DmlFromClause;
import org.hibernate.sqm.query.internal.AbstractSqmDmlStatement;
import org.hibernate.sqm.query.internal.InFlightSqmSubQueryContainer;

/**
 * QuerySpecProcessingState implementation for DML statements
 *
 * @author Steve Ebersole
 */
public class QuerySpecProcessingStateDmlImpl implements QuerySpecProcessingState, SqmFromClauseContainer {
	private final ParsingContext parsingContext;
	private final DmlFromClause fromClause;

	private final FromElementBuilder fromElementBuilder;

	public QuerySpecProcessingStateDmlImpl(ParsingContext parsingContext, AbstractSqmDmlStatement dmlStatement) {
		this.parsingContext = parsingContext;

		this.fromClause = new DmlFromClause( dmlStatement, this );
		this.fromElementBuilder = new DmlFromElementBuilder( parsingContext, new AliasRegistry() );
	}

	@Override
	public QuerySpecProcessingState getParent() {
		return null;
	}

	@Override
	public SqmFromClauseContainer getFromClauseContainer() {
		return this;
	}

	@Override
	public InFlightSqmSubQueryContainer getSubQueryContainer() {
		return fromClause.getDmlStatement();
	}

	@Override
	public SqmFromClause getFromClause() {
		return fromClause;
	}

	@Override
	public DomainReferenceBinding findFromElementByIdentificationVariable(String identificationVariable) {
		return fromClause.getFromElementSpace().getRoot().getIdentificationVariable().equals( identificationVariable )
				? fromClause.getFromElementSpace().getRoot().getDomainReferenceBinding()
				: null;
	}

	@Override
	public DomainReferenceBinding findFromElementExposingAttribute(String attributeName) {
		if ( rootExposesAttribute( attributeName ) ) {
			return fromClause.getFromElementSpace().getRoot().getDomainReferenceBinding();
		}
		else {
			return null;
		}
	}

	private boolean rootExposesAttribute(String attributeName) {
		final AttributeReference attrRef = getParsingContext().getConsumerContext()
				.getDomainMetamodel()
				.locateAttributeReference( fromClause.getFromElementSpace().getRoot().getDomainReferenceBinding().getBoundDomainReference(), attributeName );
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

	@Override
	public ParsingContext getParsingContext() {
		return parsingContext;
	}


	/**
	 * A FromElementBuilder specific for DML statements.  Basically throws exceptions
	 * on any calls to create any FromElements other than the root.
	 */
	public static class DmlFromElementBuilder extends FromElementBuilder {
		public DmlFromElementBuilder(ParsingContext parsingContext, AliasRegistry aliasRegistry) {
			super( parsingContext, aliasRegistry );
		}

		@Override
		public SqmCrossJoin makeCrossJoinedFromElement(
				FromElementSpace fromElementSpace, String uid, EntityReference entityType, String alias) {
			throw new ParsingException( "DML from-clause cannot define joins" );
		}

		@Override
		public SqmEntityJoin buildEntityJoin(
				FromElementSpace fromElementSpace,
				String alias,
				EntityReference entityType,
				JoinType joinType) {
			throw new ParsingException( "DML from-clause cannot define joins" );
		}

		@Override
		public SqmAttributeJoin buildAttributeJoin(
				AttributeBinding attributeBinding,
				String alias,
				EntityReference subclassIndicator,
				String path,
				JoinType joinType,
				String fetchParentAlias,
				boolean canReuseImplicitJoins) {
			throw new ParsingException( "DML from-clause cannot define joins" );
		}
	}
}
