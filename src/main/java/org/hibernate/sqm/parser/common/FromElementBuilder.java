/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.parser.common;

import org.hibernate.sqm.domain.EntityReference;
import org.hibernate.sqm.parser.ParsingException;
import org.hibernate.sqm.query.JoinType;
import org.hibernate.sqm.query.from.FromElementSpace;
import org.hibernate.sqm.query.from.SqmAttributeJoin;
import org.hibernate.sqm.query.from.SqmCrossJoin;
import org.hibernate.sqm.query.from.SqmEntityJoin;
import org.hibernate.sqm.query.from.SqmFrom;
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
			EntityReference entityBinding,
			String alias) {
		if ( alias == null ) {
			alias = parsingContext.getImplicitAliasGenerator().buildUniqueImplicitAlias();
			log.debugf(
					"Generated implicit alias [%s] for root entity reference [%s]",
					alias,
					entityBinding.getEntityName()
			);
		}
		final SqmRoot root = new SqmRoot(
				fromElementSpace,
				parsingContext.makeUniqueIdentifier(),
				alias,
				entityBinding
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
			EntityReference entityBinding,
			String alias) {
		if ( alias == null ) {
			alias = parsingContext.getImplicitAliasGenerator().buildUniqueImplicitAlias();
			log.debugf(
					"Generated implicit alias [%s] for cross joined entity reference [%s]",
					alias,
					entityBinding.getEntityName()
			);
		}

		final SqmCrossJoin join = new SqmCrossJoin(
				fromElementSpace,
				uid,
				alias,
				entityBinding
		);
		fromElementSpace.addJoin( join );
		parsingContext.registerFromElementByUniqueId( join );
		registerAlias( join );
		return join;
	}

	public SqmEntityJoin buildEntityJoin(
			FromElementSpace fromElementSpace,
			String alias,
			EntityReference entityType,
			JoinType joinType) {
		if ( alias == null ) {
			alias = parsingContext.getImplicitAliasGenerator().buildUniqueImplicitAlias();
			log.debugf(
					"Generated implicit alias [%s] for entity join [%s]",
					alias,
					entityType.getEntityName()
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
			AttributeBinding attributeBinding,
			String alias,
			EntityReference subclassIndicator,
			String path,
			JoinType joinType,
			boolean fetched,
			boolean canReuseImplicitJoins) {
		assert attributeBinding != null;
		assert joinType != null;

		if ( fetched && canReuseImplicitJoins ) {
			throw new ParsingException( "Illegal combination of [fetched=true] and [canReuseImplicitJoins=true] passed to #buildAttributeJoin" );
		}

		if ( alias != null && canReuseImplicitJoins ) {
			throw new ParsingException( "Unexpected combination of [non-null alias] and [canReuseImplicitJoins=true] passed to #buildAttributeJoin" );
		}

		// todo : validate alias & fetched?  JPA at least disallows specifying an alias for fetched associations

		if ( alias == null ) {
			alias = parsingContext.getImplicitAliasGenerator().buildUniqueImplicitAlias();
			log.debugf(
					"Generated implicit alias [%s] for attribute join [%s.%s]",
					alias,
					attributeBinding.getLhs().getFromElement().getIdentificationVariable(),
					attributeBinding.getAttribute().getAttributeName()
			);
		}

		SqmAttributeJoin join = null;
		if ( canReuseImplicitJoins ) {
			join = parsingContext.getCachedAttributeJoin( attributeBinding.getLhs().getFromElement(), attributeBinding.getAttribute() );
		}

		if ( join == null ) {
			join = new SqmAttributeJoin(
					attributeBinding.getLhs().getFromElement().getContainingSpace(),
					attributeBinding,
					parsingContext.makeUniqueIdentifier(),
					alias,
					subclassIndicator,
					path,
					joinType,
					fetched
			);

			if ( canReuseImplicitJoins ) {
				parsingContext.cacheAttributeJoin( attributeBinding.getLhs().getFromElement(), join );
			}

			attributeBinding.getLhs().getFromElement().getContainingSpace().addJoin( join );
			parsingContext.registerFromElementByUniqueId( join );
			registerAlias( join );
		}

		return join;
	}

	private void registerAlias(SqmFrom sqmFrom) {
		final String alias = sqmFrom.getIdentificationVariable();

		if ( alias == null ) {
			throw new ParsingException( "FromElement alias was null" );
		}

		if ( ImplicitAliasGenerator.isImplicitAlias( alias ) ) {
			log.debug( "Alias registration for implicit FromElement alias : " + alias );
		}

		aliasRegistry.registerAlias( sqmFrom.getDomainReferenceBinding() );
	}
}
