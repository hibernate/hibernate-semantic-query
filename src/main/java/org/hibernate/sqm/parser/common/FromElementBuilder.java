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
import org.hibernate.sqm.query.PropertyPath;
import org.hibernate.sqm.query.expression.domain.AttributeBinding;
import org.hibernate.sqm.query.from.FromElementSpace;
import org.hibernate.sqm.query.from.SqmAttributeJoin;
import org.hibernate.sqm.query.from.SqmCrossJoin;
import org.hibernate.sqm.query.from.SqmEntityJoin;
import org.hibernate.sqm.query.from.SqmFrom;
import org.hibernate.sqm.query.from.SqmRoot;

import org.jboss.logging.Logger;

import static org.hibernate.sqm.Helper.isNotEmpty;

/**
 * @author Steve Ebersole
 */
public class FromElementBuilder {
	// todo : I am pretty sure the uses of `{LHS-from-element}.getContainingSpace()` is incorrect when building SqmFrom elements below
	//		instead we should be passing along the FromElementSpace to use.  the big scenario I can
	//		think of is correlated sub-queries where the "LHS" is actually part of the outer query - aside
	// 		from hoisting that is not the FromElementSpace we should be using.

	// todo : make AliasRegistry part of QuerySpecProcessingState - pass that reference in here too
	//		but its odd to externally get the AliasRegistry from the FromElementBuilder when
	//		we are dealing with result-variables (selection aliases)

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
			PropertyPath sourcePath,
			JoinType joinType,
			String fetchParentUniqueIdentifier,
			boolean canReuseImplicitJoins) {
		assert attributeBinding != null;
		assert joinType != null;

		if ( isNotEmpty(fetchParentUniqueIdentifier) && canReuseImplicitJoins ) {
			throw new ParsingException( "Illegal combination of [non-null fetch parent] and [canReuseImplicitJoins=true] passed to #buildAttributeJoin" );
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
					sourcePath,
					joinType,
					fetchParentUniqueIdentifier
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
