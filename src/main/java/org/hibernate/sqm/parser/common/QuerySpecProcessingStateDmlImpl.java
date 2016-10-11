/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.parser.common;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.sqm.domain.Attribute;
import org.hibernate.sqm.domain.EntityType;
import org.hibernate.sqm.parser.ParsingException;
import org.hibernate.sqm.query.JoinType;
import org.hibernate.sqm.query.from.SqmCrossJoin;
import org.hibernate.sqm.query.from.SqmFrom;
import org.hibernate.sqm.query.from.SqmFromClause;
import org.hibernate.sqm.query.from.FromElementSpace;
import org.hibernate.sqm.query.from.SqmJoin;
import org.hibernate.sqm.query.from.SqmAttributeJoin;
import org.hibernate.sqm.query.from.SqmEntityJoin;
import org.hibernate.sqm.query.from.SqmRoot;

/**
 * QuerySpecProcessingState implementation for DML statements
 *
 * @author Steve Ebersole
 */
public class QuerySpecProcessingStateDmlImpl implements QuerySpecProcessingState {
	private final ParsingContext parsingContext;
	private final DmlFromClause fromClause;

	private final FromElementBuilder fromElementBuilder;

	private Map<String,SqmFrom> fromElementsByPath = new HashMap<String, SqmFrom>();

	public QuerySpecProcessingStateDmlImpl(ParsingContext parsingContext) {
		this.parsingContext = parsingContext;
		this.fromClause = new DmlFromClause();
		this.fromElementBuilder = new DmlFromElementBuilder( parsingContext, new AliasRegistry() );
	}

	@Override
	public QuerySpecProcessingState getParent() {
		return null;
	}

	@Override
	public SqmFromClause getFromClause() {
		return fromClause;
	}

	@Override
	public SqmFrom findFromElementByIdentificationVariable(String identificationVariable) {
		return fromClause.fromElementSpace.getRoot().getIdentificationVariable().equals( identificationVariable )
				? fromClause.fromElementSpace.getRoot()
				: null;
	}

	@Override
	public SqmFrom findFromElementExposingAttribute(String attributeName) {
		return fromClause.fromElementSpace.getRoot().resolveAttribute(attributeName ) != null
				? fromClause.fromElementSpace.getRoot()
				: null;
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

	public static class DmlFromClause extends SqmFromClause {
		private final DmlFromElementSpace fromElementSpace = new DmlFromElementSpace( this );

		@Override
		public List<FromElementSpace> getFromElementSpaces() {
			return Collections.<FromElementSpace>singletonList( fromElementSpace );
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
				FromElementSpace fromElementSpace, String uid, EntityType entityType, String alias) {
			throw new ParsingException( "DML from-clause cannot define joins" );
		}

		@Override
		public SqmEntityJoin buildEntityJoin(
				FromElementSpace fromElementSpace,
				String alias,
				EntityType entityType,
				JoinType joinType) {
			throw new ParsingException( "DML from-clause cannot define joins" );
		}

		@Override
		public SqmAttributeJoin buildAttributeJoin(
				FromElementSpace fromElementSpace,
				String alias,
				Attribute attributeDescriptor,
				EntityType subclassIndicator,
				String path,
				JoinType joinType,
				SqmFrom lhs,
				boolean fetched,
				boolean canReuseImplicitJoins) {
			throw new ParsingException( "DML from-clause cannot define joins" );
		}
	}
}
