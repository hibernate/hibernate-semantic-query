/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.parser.common;

import org.hibernate.sqm.parser.ParsingException;
import org.hibernate.sqm.domain.Attribute;
import org.hibernate.sqm.domain.EntityType;
import org.hibernate.sqm.query.JoinType;
import org.hibernate.sqm.query.from.SqmCrossJoin;
import org.hibernate.sqm.query.from.SqmFrom;
import org.hibernate.sqm.query.from.FromElementSpace;
import org.hibernate.sqm.query.from.SqmAttributeJoin;
import org.hibernate.sqm.query.from.SqmEntityJoin;
import org.hibernate.sqm.query.from.SqmRoot;

import org.jboss.logging.Logger;

/**
 * @author Steve Ebersole
 */
public class FromElementBuilder {
	private static final Logger log = Logger.getLogger( FromElementBuilder.class );

	private final ParsingContext parsingContext;
	private final AliasRegistry aliasRegistry;

	public FromElementBuilder(ParsingContext parsingContext, AliasRegistry aliasRegistry) {
		this.parsingContext = parsingContext;
		this.aliasRegistry = aliasRegistry;
	}

	public AliasRegistry getAliasRegistry(){
		return aliasRegistry;
	}

	/**
	 * Make the root entity reference for the FromElementSpace
	 */
	public SqmRoot makeRootEntityFromElement(
			FromElementSpace fromElementSpace,
			EntityType entityType,
			String alias) {
		if ( alias == null ) {
			alias = parsingContext.getImplicitAliasGenerator().buildUniqueImplicitAlias();
			log.debugf(
					"Generated implicit alias [%s] for root entity reference [%s]",
					alias,
					entityType.getName()
			);
		}
		final SqmRoot root = new SqmRoot(
				fromElementSpace,
				parsingContext.makeUniqueIdentifier(),
				alias,
				entityType
		);
		fromElementSpace.setRoot( root );
		parsingContext.registerFromElementByUniqueId( root );
		registerAlias( root );
		return root;
	}


	/**
	 * Make the root entity reference for the FromElementSpace
	 */
	public SqmCrossJoin makeCrossJoinedFromElement(
			FromElementSpace fromElementSpace,
			String uid,
			EntityType entityType,
			String alias) {
		if ( alias == null ) {
			alias = parsingContext.getImplicitAliasGenerator().buildUniqueImplicitAlias();
			log.debugf(
					"Generated implicit alias [%s] for cross joined entity reference [%s]",
					alias,
					entityType.getName()
			);
		}

		final SqmCrossJoin join = new SqmCrossJoin(
				fromElementSpace,
				uid,
				alias,
				entityType
		);
		fromElementSpace.addJoin( join );
		parsingContext.registerFromElementByUniqueId( join );
		registerAlias( join );
		return join;
	}

	public SqmEntityJoin buildEntityJoin(
			FromElementSpace fromElementSpace,
			String alias,
			EntityType entityType,
			JoinType joinType) {
		if ( alias == null ) {
			alias = parsingContext.getImplicitAliasGenerator().buildUniqueImplicitAlias();
			log.debugf(
					"Generated implicit alias [%s] for entity join [%s]",
					alias,
					entityType.getName()
			);
		}

		final SqmEntityJoin join = new SqmEntityJoin(
				fromElementSpace,
				parsingContext.makeUniqueIdentifier(),
				alias,
				entityType,
				joinType
		);
		fromElementSpace.addJoin( join );
		parsingContext.registerFromElementByUniqueId( join );
		registerAlias( join );
		return join;
	}

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
		if ( fetched && canReuseImplicitJoins ) {
			throw new ParsingException( "Illegal combination of [fetched=true] and [canReuseImplicitJoins=true] passed to #buildAttributeJoin" );
		}

		if ( alias != null && canReuseImplicitJoins ) {
			throw new ParsingException( "Unexpected combination of [non-null alias] and [canReuseImplicitJoins=true] passed to #buildAttributeJoin" );
		}

		// todo : validate alias & fetched?  JPA at least disallows specifying an alias for fetched associations

		if ( attributeDescriptor == null ) {
			throw new ParsingException(
					"AttributeDescriptor was null [name unknown]; cannot build attribute join in relation to from-element [" +
							lhs.getBindable() + "(" + lhs.getIdentificationVariable() + ")]"
			);
		}

		if ( alias == null ) {
			alias = parsingContext.getImplicitAliasGenerator().buildUniqueImplicitAlias();
			log.debugf(
					"Generated implicit alias [%s] for attribute join [%s.%s]",
					alias,
					lhs.getIdentificationVariable(),
					attributeDescriptor.getName()
			);
		}

		SqmAttributeJoin join = null;
		if ( canReuseImplicitJoins ) {
			join = parsingContext.getCachedAttributeJoin( lhs, attributeDescriptor );
		}

		if ( join == null ) {
			join = new SqmAttributeJoin(
					lhs,
					parsingContext.makeUniqueIdentifier(),
					alias,
					attributeDescriptor,
					subclassIndicator,
					path,
					joinType,
					fetched
			);

			if ( canReuseImplicitJoins ) {
				parsingContext.cacheAttributeJoin( lhs, join );
			}

			fromElementSpace.addJoin( join );
			parsingContext.registerFromElementByUniqueId( join );
			registerAlias( join );
			registerPath( join );
		}

		return join;
	}

	private void registerAlias(SqmFrom fromElement) {
		final String alias = fromElement.getIdentificationVariable();

		if ( alias == null ) {
			throw new ParsingException( "FromElement alias was null" );
		}

		if ( ImplicitAliasGenerator.isImplicitAlias( alias ) ) {
			log.debug( "Alias registration for implicit FromElement alias : " + alias );
		}

		aliasRegistry.registerAlias( fromElement );
	}

	private void registerPath(SqmAttributeJoin join) {
		// todo : come back to this
		// 		Be sure to disable this while processing from clauses (FromClauseProcessor).  Paths in from clause
		//		should almost never be reused.  Paths defined in other parts of the sqm are fine...
	}
}
