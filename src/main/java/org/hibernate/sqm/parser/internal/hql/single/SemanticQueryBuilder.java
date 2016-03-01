/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.parser.internal.hql.single;

import org.hibernate.sqm.domain.EntityType;
import org.hibernate.sqm.domain.PolymorphicEntityType;
import org.hibernate.sqm.parser.ParsingException;
import org.hibernate.sqm.parser.SemanticException;
import org.hibernate.sqm.parser.StrictJpaComplianceViolation;
import org.hibernate.sqm.parser.internal.FromElementBuilder;
import org.hibernate.sqm.parser.internal.ImplicitAliasGenerator;
import org.hibernate.sqm.parser.internal.ParsingContext;
import org.hibernate.sqm.parser.internal.hql.AbstractHqlParseTreeVisitor;
import org.hibernate.sqm.parser.internal.hql.antlr.HqlParser;
import org.hibernate.sqm.parser.internal.hql.path.FromElementLocator;
import org.hibernate.sqm.parser.internal.hql.path.PathResolverBasicImpl;
import org.hibernate.sqm.parser.internal.hql.path.ResolutionContext;
import org.hibernate.sqm.path.Binding;
import org.hibernate.sqm.query.DeleteStatement;
import org.hibernate.sqm.query.InsertSelectStatement;
import org.hibernate.sqm.query.JoinType;
import org.hibernate.sqm.query.QuerySpec;
import org.hibernate.sqm.query.SelectStatement;
import org.hibernate.sqm.query.Statement;
import org.hibernate.sqm.query.UpdateStatement;
import org.hibernate.sqm.query.expression.AttributeReferenceExpression;
import org.hibernate.sqm.query.expression.Expression;
import org.hibernate.sqm.query.from.CrossJoinedFromElement;
import org.hibernate.sqm.query.from.FromClause;
import org.hibernate.sqm.query.from.FromElement;
import org.hibernate.sqm.query.from.FromElementSpace;
import org.hibernate.sqm.query.from.JoinedFromElement;
import org.hibernate.sqm.query.from.QualifiedAttributeJoinFromElement;
import org.hibernate.sqm.query.from.QualifiedJoinedFromElement;
import org.hibernate.sqm.query.from.RootEntityFromElement;
import org.hibernate.sqm.query.order.OrderByClause;
import org.hibernate.sqm.query.order.SortOrder;
import org.hibernate.sqm.query.order.SortSpecification;
import org.hibernate.sqm.query.predicate.Predicate;
import org.hibernate.sqm.query.predicate.WhereClause;
import org.hibernate.sqm.query.select.SelectClause;

import org.jboss.logging.Logger;

import org.antlr.v4.runtime.tree.TerminalNode;

/**
 * @author Steve Ebersole
 */
public class SemanticQueryBuilder extends AbstractHqlParseTreeVisitor {
	private static final Logger log = Logger.getLogger( SemanticQueryBuilder.class );

	private Statement statement;

	private QuerySpecProcessingState currentQuerySpecProcessingState;

	public SemanticQueryBuilder(ParsingContext parsingContext) {
		super( parsingContext );
	}

	@Override
	public FromClause getCurrentFromClause() {
		return currentQuerySpecProcessingState.getFromClause();
	}

	@Override
	public FromElementBuilder getFromElementBuilder() {
		return currentQuerySpecProcessingState.getFromElementBuilder();
	}

	@Override
	public Statement visitStatement(HqlParser.StatementContext ctx) {
		if ( ctx.insertStatement() != null ) {
			return visitInsertStatement( ctx.insertStatement() );
		}
		else if ( ctx.updateStatement() != null ) {
			return visitUpdateStatement( ctx.updateStatement() );
		}
		else if ( ctx.deleteStatement() != null ) {
			return visitDeleteStatement( ctx.deleteStatement() );
		}
		else if ( ctx.selectStatement() != null ) {
			return visitSelectStatement( ctx.selectStatement() );
		}

		throw new ParsingException( "Unexpected statement type [not INSERT, UPDATE, DELETE or SELECT] : " + ctx.getText() );
	}

	@Override
	public Binding visitIndexedPath(HqlParser.IndexedPathContext ctx) {
		return super.visitIndexedPath( ctx );
	}

	@Override
	public SelectStatement visitSelectStatement(HqlParser.SelectStatementContext ctx) {
		if ( getParsingContext().getConsumerContext().useStrictJpaCompliance() ) {
			if ( ctx.querySpec().selectClause() == null ) {
				throw new StrictJpaComplianceViolation(
						"Encountered implicit select-clause, but strict JPQL compliance was requested",
						StrictJpaComplianceViolation.Type.IMPLICIT_SELECT
				);
			}
		}

		final SelectStatement selectStatement = new SelectStatement();
		selectStatement.applyQuerySpec( visitQuerySpec( ctx.querySpec() ) );
		if ( ctx.orderByClause() != null ) {
			pathResolverStack.push(
					new PathResolverBasicImpl(
							new OrderByResolutionContext(
									SemanticQueryBuilder.this.getParsingContext(),
									selectStatement
							)
					)
			);
			try {
				selectStatement.applyOrderByClause( visitOrderByClause( ctx.orderByClause() ) );
			}
			finally {
				pathResolverStack.pop();
			}
		}

		return selectStatement;
	}

	private static class OrderByResolutionContext implements ResolutionContext, FromElementLocator {
		private final ParsingContext parsingContext;
		private final SelectStatement selectStatement;

		public OrderByResolutionContext(ParsingContext parsingContext, SelectStatement selectStatement) {
			this.parsingContext = parsingContext;
			this.selectStatement = selectStatement;
		}

		@Override
		public FromElement findFromElementByIdentificationVariable(String identificationVariable) {
			for ( FromElementSpace fromElementSpace : selectStatement.getQuerySpec().getFromClause().getFromElementSpaces() ) {
				if ( fromElementSpace.getRoot().getIdentificationVariable().equals( identificationVariable ) ) {
					return fromElementSpace.getRoot();
				}

				for ( JoinedFromElement joinedFromElement : fromElementSpace.getJoins() ) {
					if ( joinedFromElement.getIdentificationVariable().equals( identificationVariable ) ) {
						return joinedFromElement;
					}
				}
			}

			// otherwise there is none
			return null;
		}

		@Override
		public FromElement findFromElementExposingAttribute(String attributeName) {
			for ( FromElementSpace fromElementSpace : selectStatement.getQuerySpec().getFromClause().getFromElementSpaces() ) {
				if ( fromElementSpace.getRoot().resolveAttribute( attributeName ) != null ) {
					return fromElementSpace.getRoot();
				}

				for ( JoinedFromElement joinedFromElement : fromElementSpace.getJoins() ) {
					if ( joinedFromElement.resolveAttribute( attributeName ) != null ) {
						return joinedFromElement;
					}
				}
			}

			// otherwise there is none
			return null;
		}

		@Override
		public FromElementLocator getFromElementLocator() {
			return this;
		}

		@Override
		public FromElementBuilder getFromElementBuilder() {
			throw new SemanticException( "order-by clause cannot define implicit joins" );
		}

		@Override
		public ParsingContext getParsingContext() {
			return parsingContext;
		}
	}

	@Override
	public OrderByClause visitOrderByClause(HqlParser.OrderByClauseContext ctx) {
		final OrderByClause orderByClause = new OrderByClause();
		for ( HqlParser.SortSpecificationContext sortSpecificationContext : ctx.sortSpecification() ) {
			orderByClause.addSortSpecification( visitSortSpecification( sortSpecificationContext ) );
		}
		return orderByClause;
	}

	@Override
	public SortSpecification visitSortSpecification(HqlParser.SortSpecificationContext ctx) {
		final Expression sortExpression = (Expression) ctx.expression().accept( this );
		final String collation;
		if ( ctx.collationSpecification() != null && ctx.collationSpecification().collateName() != null ) {
			collation = ctx.collationSpecification().collateName().dotIdentifierSequence().getText();
		}
		else {
			collation = null;
		}
		final SortOrder sortOrder;
		if ( ctx.orderingSpecification() != null ) {
			final String ordering = ctx.orderingSpecification().getText();
			try {
				sortOrder = interpretSortOrder( ordering );
			}
			catch (IllegalArgumentException e) {
				throw new SemanticException( "Unrecognized sort ordering: " + ordering, e );
			}
		}
		else {
			sortOrder = null;
		}
		return new SortSpecification( sortExpression, collation, sortOrder );
	}

	private SortOrder interpretSortOrder(String value) {
		if ( value == null ) {
			return null;
		}

		if ( value.equalsIgnoreCase( "ascending" ) || value.equalsIgnoreCase( "asc" ) ) {
			return SortOrder.ASCENDING;
		}

		if ( value.equalsIgnoreCase( "descending" ) || value.equalsIgnoreCase( "desc" ) ) {
			return SortOrder.DESCENDING;
		}

		throw new SemanticException( "Unknown sort order : " + value );
	}

	// todo : the structure for handling update/delete in FromClauseProcessor (and accessing them here) needs some re-thought.
	// todo : given the current set up, need to set up the FromClauseIndex to understand the "DML root"

	@Override
	public DeleteStatement visitDeleteStatement(HqlParser.DeleteStatementContext ctx) {
		currentQuerySpecProcessingState = new QuerySpecProcessingStateDmlImpl( getParsingContext() );
		try {
			final RootEntityFromElement root = resolveDmlRootEntityReference( ctx.mainEntityPersisterReference() );
			final DeleteStatement deleteStatement = new DeleteStatement( root );

			pathResolverStack.push( new PathResolverBasicImpl( currentQuerySpecProcessingState ) );
			try {
				deleteStatement.getWhereClause().setPredicate( (Predicate) ctx.whereClause()
						.predicate()
						.accept( this ) );
			}
			finally {
				pathResolverStack.pop();
			}

			return deleteStatement;
		}
		finally {
			currentQuerySpecProcessingState = null;
		}
	}

	protected RootEntityFromElement resolveDmlRootEntityReference(HqlParser.MainEntityPersisterReferenceContext rootEntityContext) {
		final EntityType entityType = resolveEntityReference( rootEntityContext.dotIdentifierSequence() );
		String alias = interpretAlias( rootEntityContext.IDENTIFIER() );
		if ( alias == null ) {
			alias = getParsingContext().getImplicitAliasGenerator().buildUniqueImplicitAlias();
			log.debugf(
					"Generated implicit alias [%s] for DML root entity reference [%s]",
					alias,
					entityType.getName()
			);
		}
		final RootEntityFromElement root = new RootEntityFromElement( null, getParsingContext().makeUniqueIdentifier(), alias, entityType );
		getParsingContext().registerFromElementByUniqueId( root );
		currentQuerySpecProcessingState.getFromElementBuilder().getAliasRegistry().registerAlias( root );
		currentQuerySpecProcessingState.getFromClause().getFromElementSpaces().get( 0 ).setRoot( root );
		return root;
	}

	@Override
	public UpdateStatement visitUpdateStatement(HqlParser.UpdateStatementContext ctx) {
		currentQuerySpecProcessingState = new QuerySpecProcessingStateDmlImpl( getParsingContext() );
		try {
			final RootEntityFromElement root = resolveDmlRootEntityReference( ctx.mainEntityPersisterReference() );
			final UpdateStatement updateStatement = new UpdateStatement( root );

			pathResolverStack.push( new PathResolverBasicImpl( currentQuerySpecProcessingState ) );
			try {
				updateStatement.getWhereClause().setPredicate(
						(Predicate) ctx.whereClause().predicate().accept( this )
				);

				for ( HqlParser.AssignmentContext assignmentContext : ctx.setClause().assignment() ) {
					// todo : validate "state field" expression
					updateStatement.getSetClause().addAssignment(
							(AttributeReferenceExpression) pathResolverStack.getCurrent().resolvePath( assignmentContext.dotIdentifierSequence() ),
							(Expression) assignmentContext.expression().accept( this )
					);
				}
			}
			finally {
				pathResolverStack.pop();
			}

			return updateStatement;
		}
		finally {
			currentQuerySpecProcessingState = null;
		}
	}

	@Override
	public InsertSelectStatement visitInsertStatement(HqlParser.InsertStatementContext ctx) {
		currentQuerySpecProcessingState = new QuerySpecProcessingStateDmlImpl( getParsingContext() );
		try {
			final EntityType entityType = resolveEntityReference( ctx.insertSpec().intoSpec().dotIdentifierSequence() );
			String alias = getParsingContext().getImplicitAliasGenerator().buildUniqueImplicitAlias();
			log.debugf(
					"Generated implicit alias [%s] for INSERT target [%s]",
					alias,
					entityType.getName()
			);

			RootEntityFromElement root = new RootEntityFromElement( null, getParsingContext().makeUniqueIdentifier(), alias, entityType );
			getParsingContext().registerFromElementByUniqueId( root );
			currentQuerySpecProcessingState.getFromElementBuilder().getAliasRegistry().registerAlias( root );
			currentQuerySpecProcessingState.getFromClause().getFromElementSpaces().get( 0 ).setRoot( root );

			// for now we only support the INSERT-SELECT form
			final InsertSelectStatement insertStatement = new InsertSelectStatement( root );

			pathResolverStack.push( new PathResolverBasicImpl( currentQuerySpecProcessingState ) );
			try {
				insertStatement.setSelectQuery( visitQuerySpec( ctx.querySpec() ) );

				for ( HqlParser.DotIdentifierSequenceContext stateFieldCtx : ctx.insertSpec().targetFieldsSpec().dotIdentifierSequence() ) {
					final AttributeReferenceExpression stateField = (AttributeReferenceExpression) pathResolverStack.getCurrent().resolvePath( stateFieldCtx );
					// todo : validate each resolved stateField...
					insertStatement.addInsertTargetStateField( stateField );
				}
			}
			finally {
				pathResolverStack.pop();
			}

			return insertStatement;
		}
		finally {
			currentQuerySpecProcessingState = null;
		}
	}

	@Override
	protected ResolutionContext buildPathResolutionContext() {
		return currentQuerySpecProcessingState;
	}

	@Override
	public QuerySpec visitQuerySpec(HqlParser.QuerySpecContext ctx) {
		currentQuerySpecProcessingState = new QuerySpecProcessingStateStandardImpl( getParsingContext(), currentQuerySpecProcessingState );
		pathResolverStack.push( new PathResolverBasicImpl( currentQuerySpecProcessingState ) );
		try {
			// visit from-clause first!!!
			visitFromClause( ctx.fromClause() );

			final SelectClause selectClause;
			if ( ctx.selectClause() != null ) {
				selectClause = visitSelectClause( ctx.selectClause() );
			}
			else {
				log.info( "Encountered implicit select clause which is a deprecated feature : " + ctx.getText() );
				selectClause = buildInferredSelectClause( getCurrentFromClause() );
			}

			final WhereClause whereClause;
			if ( ctx.whereClause() != null ) {
				whereClause = visitWhereClause( ctx.whereClause() );
			}
			else {
				whereClause = null;
			}
			return new QuerySpec( getCurrentFromClause(), selectClause, whereClause );
		}
		finally {
			pathResolverStack.pop();
			currentQuerySpecProcessingState = currentQuerySpecProcessingState.getParent();
		}
	}

	private FromElementSpace currentFromElementSpace;

	@Override
	public Object visitFromElementSpace(HqlParser.FromElementSpaceContext ctx) {
		currentFromElementSpace = currentQuerySpecProcessingState.getFromClause().makeFromElementSpace();

		// adding root and joins to the FromElementSpace is currently handled in FromElementBuilder
		// it is very questionable whether this should be done there, but for now keep it
		// todo : revisit ^^

		visitFromElementSpaceRoot( ctx.fromElementSpaceRoot() );

		for ( HqlParser.CrossJoinContext crossJoinContext : ctx.crossJoin() ) {
			visitCrossJoin( crossJoinContext );
		}

		for ( HqlParser.QualifiedJoinContext qualifiedJoinContext : ctx.qualifiedJoin() ) {
			visitQualifiedJoin( qualifiedJoinContext );
		}

		for ( HqlParser.JpaCollectionJoinContext jpaCollectionJoinContext : ctx.jpaCollectionJoin() ) {
			visitJpaCollectionJoin( jpaCollectionJoinContext );
		}


		FromElementSpace rtn = currentFromElementSpace;
		currentFromElementSpace = null;
		return rtn;
	}

	@Override
	public RootEntityFromElement visitFromElementSpaceRoot(HqlParser.FromElementSpaceRootContext ctx) {
		final EntityType entityType = resolveEntityReference(
				ctx.mainEntityPersisterReference().dotIdentifierSequence()
		);

		if ( PolymorphicEntityType.class.isInstance( entityType ) ) {
			if ( getParsingContext().getConsumerContext().useStrictJpaCompliance() ) {
				throw new StrictJpaComplianceViolation(
						"Encountered unmapped polymorphic reference [" + entityType.getName()
								+ "], but strict JPQL compliance was requested",
						StrictJpaComplianceViolation.Type.UNMAPPED_POLYMORPHISM
				);
			}

			// todo : disallow in subqueries as well
		}

		return currentQuerySpecProcessingState.getFromElementBuilder().makeRootEntityFromElement(
				currentFromElementSpace,
				entityType,
				interpretAlias( ctx.mainEntityPersisterReference().IDENTIFIER() )
		);
	}

	private EntityType resolveEntityReference(HqlParser.DotIdentifierSequenceContext dotIdentifierSequenceContext) {
		final String entityName = dotIdentifierSequenceContext.getText();
		final EntityType entityTypeDescriptor = getParsingContext().getConsumerContext()
				.getDomainMetamodel()
				.resolveEntityType( entityName );
		if ( entityTypeDescriptor == null ) {
			throw new SemanticException( "Unresolved entity name : " + entityName );
		}
		return entityTypeDescriptor;
	}

	private String interpretAlias(TerminalNode aliasNode) {
		if ( aliasNode == null ) {
			return getParsingContext().getImplicitAliasGenerator().buildUniqueImplicitAlias();
		}

		// todo : not sure I like asserts for this kind of thing.  They are generally disable in runtime environments.
		// either the thing is important to check or it isn't.
		assert aliasNode.getSymbol().getType() == HqlParser.IDENTIFIER;

		return aliasNode.getText();
	}

	@Override
	public CrossJoinedFromElement visitCrossJoin(HqlParser.CrossJoinContext ctx) {
		final EntityType entityType = resolveEntityReference(
				ctx.mainEntityPersisterReference().dotIdentifierSequence()
		);

		if ( PolymorphicEntityType.class.isInstance( entityType ) ) {
			throw new SemanticException(
					"Unmapped polymorphic references are only valid as query root, not in cross join : " +
							entityType.getName()
			);
		}

		return currentQuerySpecProcessingState.getFromElementBuilder().makeCrossJoinedFromElement(
				currentFromElementSpace,
				getParsingContext().makeUniqueIdentifier(),
				entityType,
				interpretAlias( ctx.mainEntityPersisterReference().IDENTIFIER() )
		);
	}

	@Override
	public QualifiedJoinedFromElement visitJpaCollectionJoin(HqlParser.JpaCollectionJoinContext ctx) {
		final ParseTreeVisitorQualifiedJoinImpl visitor = new ParseTreeVisitorQualifiedJoinImpl(
				getParsingContext(),
				currentQuerySpecProcessingState,
				currentFromElementSpace,
				JoinType.INNER,
				interpretAlias( ctx.IDENTIFIER() ),
				false
		);

		QualifiedJoinedFromElement joinedPath = (QualifiedJoinedFromElement) ctx.path().accept(
				visitor
		);

		if ( joinedPath == null ) {
			throw new ParsingException( "Could not resolve JPA collection join path : " + ctx.getText() );
		}

		return joinedPath;
	}

	@Override
	public QualifiedJoinedFromElement visitQualifiedJoin(HqlParser.QualifiedJoinContext ctx) {
		final JoinType joinType;
		if ( ctx.outerKeyword() != null ) {
			// for outer joins, only left outer joins are currently supported
			joinType = JoinType.LEFT;
		}
		else {
			joinType = JoinType.INNER;
		}

		final ParseTreeVisitorQualifiedJoinImpl visitor = new ParseTreeVisitorQualifiedJoinImpl(
				getParsingContext(),
				currentQuerySpecProcessingState,
				currentFromElementSpace,
				joinType,
				interpretAlias( ctx.qualifiedJoinRhs().IDENTIFIER() ),
				ctx.fetchKeyword() != null
		);

		QualifiedJoinedFromElement joinedPath = (QualifiedJoinedFromElement) ctx.qualifiedJoinRhs().path().accept(
				visitor
		);

		if ( joinedPath == null ) {
			throw new ParsingException( "Could not resolve join path : " + ctx.qualifiedJoinRhs().getText() );
		}

		if ( getParsingContext().getConsumerContext().useStrictJpaCompliance() ) {
			if ( !ImplicitAliasGenerator.isImplicitAlias( joinedPath.getIdentificationVariable() ) ) {
				if ( QualifiedAttributeJoinFromElement.class.isInstance( joinedPath ) ) {
					if ( QualifiedAttributeJoinFromElement.class.cast( joinedPath ).isFetched() ) {
						throw new StrictJpaComplianceViolation(
								"Encountered aliased fetch join, but strict JPQL compliance was requested",
								StrictJpaComplianceViolation.Type.ALIASED_FETCH_JOIN
						);
					}
				}
			}
		}

		if ( ctx.qualifiedJoinPredicate() != null ) {
			visitor.setCurrentJoinRhs( joinedPath );
			joinedPath.setOnClausePredicate( (Predicate) ctx.qualifiedJoinPredicate().accept( visitor ) );
		}

		return joinedPath;
	}
}
