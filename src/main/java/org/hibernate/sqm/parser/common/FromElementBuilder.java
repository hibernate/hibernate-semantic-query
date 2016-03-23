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
import org.hibernate.sqm.query.from.CrossJoinedFromElement;
import org.hibernate.sqm.query.from.FromElement;
import org.hibernate.sqm.query.from.FromElementSpace;
import org.hibernate.sqm.query.from.QualifiedAttributeJoinFromElement;
import org.hibernate.sqm.query.from.QualifiedEntityJoinFromElement;
import org.hibernate.sqm.query.from.RootEntityFromElement;

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
	public RootEntityFromElement makeRootEntityFromElement(
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
		final RootEntityFromElement root = new RootEntityFromElement(
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
	public CrossJoinedFromElement makeCrossJoinedFromElement(
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

		final CrossJoinedFromElement join = new CrossJoinedFromElement(
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

	public QualifiedEntityJoinFromElement buildEntityJoin(
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

		final QualifiedEntityJoinFromElement join = new QualifiedEntityJoinFromElement(
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

	public QualifiedAttributeJoinFromElement buildAttributeJoin(
			FromElementSpace fromElementSpace,
			String alias,
			Attribute attributeDescriptor,
			EntityType subclassIndicator,
			String path,
			JoinType joinType,
			FromElement lhs,
			boolean fetched) {
		if ( attributeDescriptor == null ) {
			throw new ParsingException(
					"AttributeDescriptor was null [name unknown]; cannot build attribute join in relation to from-element [" +
							lhs.getBoundModelType() + "(" + lhs.getIdentificationVariable() + ")]"
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

		final QualifiedAttributeJoinFromElement join = new QualifiedAttributeJoinFromElement(
				fromElementSpace,
				parsingContext.makeUniqueIdentifier(),
				alias,
				attributeDescriptor,
				subclassIndicator,
				path,
				joinType,
				lhs,
				fetched
		);
		fromElementSpace.addJoin( join );
		parsingContext.registerFromElementByUniqueId( join );
		registerAlias( join );
		registerPath( join );
		return join;
	}

	private void registerAlias(FromElement fromElement) {
		final String alias = fromElement.getIdentificationVariable();

		if ( alias == null ) {
			throw new ParsingException( "FromElement alias was null" );
		}

		if ( ImplicitAliasGenerator.isImplicitAlias( alias ) ) {
			log.debug( "Alias registration for implicit FromElement alias : " + alias );
		}

		aliasRegistry.registerAlias( fromElement );
	}

	private void registerPath(QualifiedAttributeJoinFromElement join) {
		// todo : come back to this
		// 		Be sure to disable this while processing from clauses (FromClauseProcessor).  Paths in from clause
		//		should almost never be reused.  Paths defined in other parts of the sqm are fine...
	}
}
