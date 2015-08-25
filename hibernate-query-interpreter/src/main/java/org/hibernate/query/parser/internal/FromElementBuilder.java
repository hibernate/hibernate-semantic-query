/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.query.parser.internal;

import org.hibernate.query.parser.ParsingException;
import org.hibernate.sqm.domain.AttributeDescriptor;
import org.hibernate.sqm.domain.EntityTypeDescriptor;
import org.hibernate.sqm.query.JoinType;
import org.hibernate.sqm.query.from.CrossJoinedFromElement;
import org.hibernate.sqm.query.from.FromElement;
import org.hibernate.sqm.query.from.FromElementSpace;
import org.hibernate.sqm.query.from.QualifiedAttributeJoinFromElement;
import org.hibernate.sqm.query.from.RootEntityFromElement;

import org.jboss.logging.Logger;

/**
 * @author Steve Ebersole
 */
public class FromElementBuilder {
	private static final Logger log = Logger.getLogger( FromElementBuilder.class );

	private final ParsingContext parsingContext;
	private final FromClauseIndex fromClauseIndex;

	public FromElementBuilder(ParsingContext parsingContext, FromClauseIndex fromClauseIndex) {
		this.parsingContext = parsingContext;
		this.fromClauseIndex = fromClauseIndex;
	}

	/**
	 * Make the root entity reference for the FromElementSpace
	 *
	 * @param fromElementSpace
	 * @param entityTypeDescriptor
	 * @param alias
	 *
	 * @return
	 */
	public RootEntityFromElement makeRootEntityFromElement(
			FromElementSpace fromElementSpace,
			EntityTypeDescriptor entityTypeDescriptor,
			String alias) {
		if ( alias == null ) {
			alias = parsingContext.getImplicitAliasGenerator().buildUniqueImplicitAlias();
			log.debugf(
					"Generated implicit alias [%s] for root entity reference [%s]",
					alias,
					entityTypeDescriptor.getTypeName()
			);
		}
		final RootEntityFromElement root = new RootEntityFromElement( fromElementSpace, alias, entityTypeDescriptor );
		fromElementSpace.setRoot( root );
		registerAlias( root );
		return root;
	}


	/**
	 * Make the root entity reference for the FromElementSpace
	 *
	 * @param fromElementSpace
	 * @param entityTypeDescriptor
	 * @param alias
	 *
	 * @return
	 */
	public CrossJoinedFromElement makeCrossJoinedFromElement(
			FromElementSpace fromElementSpace,
			EntityTypeDescriptor entityTypeDescriptor,
			String alias) {
		if ( alias == null ) {
			alias = parsingContext.getImplicitAliasGenerator().buildUniqueImplicitAlias();
			log.debugf(
					"Generated implicit alias [%s] for cross joined entity reference [%s]",
					alias,
					entityTypeDescriptor.getTypeName()
			);
		}

		final CrossJoinedFromElement join = new CrossJoinedFromElement( fromElementSpace, alias, entityTypeDescriptor );
		fromElementSpace.addJoin( join );
		registerAlias( join );
		return join;
	}

	public QualifiedAttributeJoinFromElement buildAttributeJoin(
			FromElementSpace fromElementSpace,
			FromElement lhs,
			AttributeDescriptor attributeDescriptor,
			String alias,
			JoinType joinType,
			boolean fetched) {
		if ( attributeDescriptor == null ) {
			throw new ParsingException(
					"AttributeDescriptor was null [name unknown]; cannot build attribute join in relation to from-element [" +
							lhs.getTypeDescriptor().getTypeName() + "]"
			);
		}

		if ( alias == null ) {
			alias = parsingContext.getImplicitAliasGenerator().buildUniqueImplicitAlias();
			log.debugf(
					"Generated implicit alias [%s] for attribute join [%s.%s]",
					alias,
					lhs.getAlias(),
					attributeDescriptor.getName()
			);
		}

		final QualifiedAttributeJoinFromElement join = new QualifiedAttributeJoinFromElement(
				fromElementSpace,
				alias,
				attributeDescriptor,
				joinType,
				fetched
		);
		fromElementSpace.addJoin( join );
		registerAlias( join );
		registerPath( join );
		return join;
	}

	private void registerAlias(FromElement fromElement) {
		final String alias = fromElement.getAlias();

		if ( alias == null ) {
			throw new ParsingException( "FromElement alias was null" );
		}

		if ( ImplicitAliasGenerator.isImplicitAlias( alias ) ) {
			log.debug( "Alias registration for implicit FromElement alias : " + alias );
		}

		fromClauseIndex.registerAlias( fromElement );
	}

	private void registerPath(QualifiedAttributeJoinFromElement join) {
		// todo : come back to this
		// 		Be sure to disable this while processing from clauses (FromClauseProcessor).  Paths in from clause
		//		should almost never be reused.  Paths defined in other parts of the query are fine...
	}
}
