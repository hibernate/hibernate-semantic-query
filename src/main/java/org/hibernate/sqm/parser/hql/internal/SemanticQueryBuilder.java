/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.parser.hql.internal;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.hibernate.sqm.StrictJpaComplianceViolation;
import org.hibernate.sqm.domain.DomainReference;
import org.hibernate.sqm.domain.EntityReference;
import org.hibernate.sqm.domain.PluralAttributeElementReference.ElementClassification;
import org.hibernate.sqm.domain.PluralAttributeIndexReference.IndexClassification;
import org.hibernate.sqm.domain.PluralAttributeReference;
import org.hibernate.sqm.domain.PluralAttributeReference.CollectionClassification;
import org.hibernate.sqm.domain.PolymorphicEntityReference;
import org.hibernate.sqm.domain.SingularAttributeReference;
import org.hibernate.sqm.domain.SingularAttributeReference.SingularAttributeClassification;
import org.hibernate.sqm.parser.LiteralNumberFormatException;
import org.hibernate.sqm.parser.ParsingException;
import org.hibernate.sqm.parser.SemanticException;
import org.hibernate.sqm.parser.common.ImplicitAliasGenerator;
import org.hibernate.sqm.parser.common.ParameterDeclarationContext;
import org.hibernate.sqm.parser.common.ParsingContext;
import org.hibernate.sqm.parser.common.QuerySpecProcessingState;
import org.hibernate.sqm.parser.common.QuerySpecProcessingStateDmlImpl;
import org.hibernate.sqm.parser.common.QuerySpecProcessingStateStandardImpl;
import org.hibernate.sqm.parser.common.Stack;
import org.hibernate.sqm.parser.hql.internal.antlr.HqlParser;
import org.hibernate.sqm.parser.hql.internal.antlr.HqlParserBaseVisitor;
import org.hibernate.sqm.parser.hql.internal.path.PathHelper;
import org.hibernate.sqm.parser.hql.internal.path.PathResolver;
import org.hibernate.sqm.parser.hql.internal.path.PathResolverBasicImpl;
import org.hibernate.sqm.parser.hql.internal.path.PathResolverJoinAttributeImpl;
import org.hibernate.sqm.parser.hql.internal.path.PathResolverJoinPredicateImpl;
import org.hibernate.sqm.parser.hql.internal.path.PathResolverSelectClauseImpl;
import org.hibernate.sqm.query.JoinType;
import org.hibernate.sqm.query.SqmDeleteStatement;
import org.hibernate.sqm.query.SqmInsertSelectStatement;
import org.hibernate.sqm.query.SqmQuerySpec;
import org.hibernate.sqm.query.SqmSelectStatement;
import org.hibernate.sqm.query.SqmStatement;
import org.hibernate.sqm.query.SqmUpdateStatement;
import org.hibernate.sqm.query.expression.BinaryArithmeticSqmExpression;
import org.hibernate.sqm.query.expression.CaseSearchedSqmExpression;
import org.hibernate.sqm.query.expression.CaseSimpleSqmExpression;
import org.hibernate.sqm.query.expression.CoalesceSqmExpression;
import org.hibernate.sqm.query.expression.CollectionIndexSqmExpression;
import org.hibernate.sqm.query.expression.CollectionSizeSqmExpression;
import org.hibernate.sqm.query.expression.ConcatSqmExpression;
import org.hibernate.sqm.query.expression.ConstantEnumSqmExpression;
import org.hibernate.sqm.query.expression.ConstantFieldSqmExpression;
import org.hibernate.sqm.query.expression.ConstantSqmExpression;
import org.hibernate.sqm.query.expression.EntityTypeLiteralSqmExpression;
import org.hibernate.sqm.query.expression.ImpliedTypeSqmExpression;
import org.hibernate.sqm.query.expression.LiteralBigDecimalSqmExpression;
import org.hibernate.sqm.query.expression.LiteralBigIntegerSqmExpression;
import org.hibernate.sqm.query.expression.LiteralCharacterSqmExpression;
import org.hibernate.sqm.query.expression.LiteralDoubleSqmExpression;
import org.hibernate.sqm.query.expression.LiteralFalseSqmExpression;
import org.hibernate.sqm.query.expression.LiteralFloatSqmExpression;
import org.hibernate.sqm.query.expression.LiteralIntegerSqmExpression;
import org.hibernate.sqm.query.expression.LiteralLongSqmExpression;
import org.hibernate.sqm.query.expression.LiteralNullSqmExpression;
import org.hibernate.sqm.query.expression.LiteralSqmExpression;
import org.hibernate.sqm.query.expression.LiteralStringSqmExpression;
import org.hibernate.sqm.query.expression.LiteralTrueSqmExpression;
import org.hibernate.sqm.query.expression.NamedParameterSqmExpression;
import org.hibernate.sqm.query.expression.NullifSqmExpression;
import org.hibernate.sqm.query.expression.ParameterSqmExpression;
import org.hibernate.sqm.query.expression.ParameterizedEntityTypeSqmExpression;
import org.hibernate.sqm.query.expression.PositionalParameterSqmExpression;
import org.hibernate.sqm.query.expression.SqmExpression;
import org.hibernate.sqm.query.expression.SubQuerySqmExpression;
import org.hibernate.sqm.query.expression.UnaryOperationSqmExpression;
import org.hibernate.sqm.query.expression.domain.AttributeBinding;
import org.hibernate.sqm.query.expression.domain.DomainReferenceBinding;
import org.hibernate.sqm.query.expression.domain.EntityBinding;
import org.hibernate.sqm.query.expression.domain.EntityTypeSqmExpression;
import org.hibernate.sqm.query.expression.domain.MapEntrySqmExpression;
import org.hibernate.sqm.query.expression.domain.MapKeyBinding;
import org.hibernate.sqm.query.expression.domain.MaxElementSqmExpression;
import org.hibernate.sqm.query.expression.domain.MaxIndexSqmExpression;
import org.hibernate.sqm.query.expression.domain.MinElementSqmExpression;
import org.hibernate.sqm.query.expression.domain.MinIndexSqmExpression;
import org.hibernate.sqm.query.expression.domain.PluralAttributeBinding;
import org.hibernate.sqm.query.expression.domain.PluralAttributeElementBinding;
import org.hibernate.sqm.query.expression.domain.PluralAttributeIndexedAccessBinding;
import org.hibernate.sqm.query.expression.domain.SingularAttributeBinding;
import org.hibernate.sqm.query.expression.function.AggregateFunctionSqmExpression;
import org.hibernate.sqm.query.expression.function.AvgFunctionSqmExpression;
import org.hibernate.sqm.query.expression.function.CastFunctionSqmExpression;
import org.hibernate.sqm.query.expression.function.ConcatFunctionSqmExpression;
import org.hibernate.sqm.query.expression.function.CountFunctionSqmExpression;
import org.hibernate.sqm.query.expression.function.CountStarFunctionSqmExpression;
import org.hibernate.sqm.query.expression.function.GenericFunctionSqmExpression;
import org.hibernate.sqm.query.expression.function.LowerFunctionSqmExpression;
import org.hibernate.sqm.query.expression.function.MaxFunctionSqmExpression;
import org.hibernate.sqm.query.expression.function.MinFunctionSqmExpression;
import org.hibernate.sqm.query.expression.function.SubstringFunctionSqmExpression;
import org.hibernate.sqm.query.expression.function.SumFunctionSqmExpression;
import org.hibernate.sqm.query.expression.function.TrimFunctionSqmExpression;
import org.hibernate.sqm.query.expression.function.UpperFunctionSqmExpression;
import org.hibernate.sqm.query.from.FromElementSpace;
import org.hibernate.sqm.query.from.SqmAttributeJoin;
import org.hibernate.sqm.query.from.SqmCrossJoin;
import org.hibernate.sqm.query.from.SqmFrom;
import org.hibernate.sqm.query.from.SqmFromClause;
import org.hibernate.sqm.query.from.SqmQualifiedJoin;
import org.hibernate.sqm.query.from.SqmRoot;
import org.hibernate.sqm.query.internal.ParameterCollector;
import org.hibernate.sqm.query.internal.SqmDeleteStatementImpl;
import org.hibernate.sqm.query.internal.SqmInsertSelectStatementImpl;
import org.hibernate.sqm.query.internal.SqmSelectStatementImpl;
import org.hibernate.sqm.query.internal.SqmUpdateStatementImpl;
import org.hibernate.sqm.query.order.OrderByClause;
import org.hibernate.sqm.query.order.SortOrder;
import org.hibernate.sqm.query.order.SortSpecification;
import org.hibernate.sqm.query.predicate.AndSqmPredicate;
import org.hibernate.sqm.query.predicate.BetweenSqmPredicate;
import org.hibernate.sqm.query.predicate.EmptinessSqmPredicate;
import org.hibernate.sqm.query.predicate.GroupedSqmPredicate;
import org.hibernate.sqm.query.predicate.InListSqmPredicate;
import org.hibernate.sqm.query.predicate.InSubQuerySqmPredicate;
import org.hibernate.sqm.query.predicate.LikeSqmPredicate;
import org.hibernate.sqm.query.predicate.MemberOfSqmPredicate;
import org.hibernate.sqm.query.predicate.NegatableSqmPredicate;
import org.hibernate.sqm.query.predicate.NegatedSqmPredicate;
import org.hibernate.sqm.query.predicate.NullnessSqmPredicate;
import org.hibernate.sqm.query.predicate.OrSqmPredicate;
import org.hibernate.sqm.query.predicate.RelationalSqmPredicate;
import org.hibernate.sqm.query.predicate.SqmPredicate;
import org.hibernate.sqm.query.predicate.SqmWhereClause;
import org.hibernate.sqm.query.select.SqmDynamicInstantiation;
import org.hibernate.sqm.query.select.SqmDynamicInstantiationArgument;
import org.hibernate.sqm.query.select.SqmSelectClause;
import org.hibernate.sqm.query.select.SqmSelection;

import org.jboss.logging.Logger;

import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.TerminalNode;

/**
 * @author Steve Ebersole
 */
public class SemanticQueryBuilder extends HqlParserBaseVisitor {
	private static final Logger log = Logger.getLogger( SemanticQueryBuilder.class );

	/**
	 * Main entry point into analysis of HQL/JPQL parse tree - producing a semantic model of the
	 * query.
	 *
	 * @param statement The statement to analyze.
	 * @param parsingContext Access to things needed to perform the analysis
	 *
	 * @return The semantic query model
	 */
	public static SqmStatement buildSemanticModel(HqlParser.StatementContext statement, ParsingContext parsingContext) {
		return new SemanticQueryBuilder( parsingContext ).visitStatement( statement );
	}

	private final ParsingContext parsingContext;

	private final Stack<PathResolver> pathResolverStack = new Stack<>();
	private final Stack<ParameterDeclarationContext> parameterDeclarationContextStack = new Stack<>();

	private boolean inWhereClause;
	private QuerySpecProcessingState currentQuerySpecProcessingState;
	private ParameterCollector parameterCollector;


	private  SemanticQueryBuilder(ParsingContext parsingContext) {
		this.parsingContext = parsingContext;
	}

	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// Grammar rules

	@Override
	public SqmStatement visitStatement(HqlParser.StatementContext ctx) {
		// parameters allow multi-valued bindings only in very limited cases, so for
		// the base case here we say false
		parameterDeclarationContextStack.push( () -> false );

		try {
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
		}
		finally {
			parameterDeclarationContextStack.pop();
		}

		throw new ParsingException( "Unexpected statement type [not INSERT, UPDATE, DELETE or SELECT] : " + ctx.getText() );
	}

	@Override
	public SqmSelectStatement visitSelectStatement(HqlParser.SelectStatementContext ctx) {
		if ( parsingContext.getConsumerContext().useStrictJpaCompliance() ) {
			if ( ctx.querySpec().selectClause() == null ) {
				throw new StrictJpaComplianceViolation(
						"Encountered implicit select-clause, but strict JPQL compliance was requested",
						StrictJpaComplianceViolation.Type.IMPLICIT_SELECT
				);
			}
		}

		final SqmSelectStatementImpl selectStatement = new SqmSelectStatementImpl();
		parameterCollector = selectStatement;

		try {
			selectStatement.applyQuerySpec( visitQuerySpec( ctx.querySpec() ) );

			if ( ctx.orderByClause() != null ) {
				pathResolverStack.push(
						new PathResolverBasicImpl( new OrderByResolutionContext( parsingContext, selectStatement ) )
				);
				try {
					selectStatement.applyOrderByClause( visitOrderByClause( ctx.orderByClause() ) );
				}
				finally {
					pathResolverStack.pop();
				}
			}
		}
		finally {
			selectStatement.wrapUp();
		}

		return selectStatement;
	}

	@Override
	public SqmQuerySpec visitQuerySpec(HqlParser.QuerySpecContext ctx) {
		currentQuerySpecProcessingState = new QuerySpecProcessingStateStandardImpl( parsingContext, currentQuerySpecProcessingState );
		pathResolverStack.push( new PathResolverBasicImpl( currentQuerySpecProcessingState ) );
		try {
			// visit from-clause first!!!
			visitFromClause( ctx.fromClause() );

			final SqmSelectClause selectClause;
			if ( ctx.selectClause() != null ) {
				selectClause = visitSelectClause( ctx.selectClause() );
			}
			else {
				log.info( "Encountered implicit select clause which is a deprecated feature : " + ctx.getText() );
				selectClause = buildInferredSelectClause( currentQuerySpecProcessingState.getFromClause() );
			}

			final SqmWhereClause whereClause;
			if ( ctx.whereClause() != null ) {
				whereClause = visitWhereClause( ctx.whereClause() );
			}
			else {
				whereClause = null;
			}
			return new SqmQuerySpec( currentQuerySpecProcessingState.getFromClause(), selectClause, whereClause );
		}
		finally {
			pathResolverStack.pop();
			currentQuerySpecProcessingState = currentQuerySpecProcessingState.getParent();
		}
	}

	protected SqmSelectClause buildInferredSelectClause(SqmFromClause fromClause) {
		// for now, this is slightly different than the legacy behavior where
		// the root and each non-fetched-join was selected.  For now, here, we simply
		// select the root
		final SqmSelectClause selectClause = new SqmSelectClause( true );
		final SqmFrom root = fromClause.getFromElementSpaces().get( 0 ).getRoot();
		selectClause.addSelection( new SqmSelection( root ) );
		return selectClause;
	}

	@Override
	public SqmSelectClause visitSelectClause(HqlParser.SelectClauseContext ctx) {
		pathResolverStack.push( new PathResolverSelectClauseImpl( currentQuerySpecProcessingState ) );

		try {
			final SqmSelectClause selectClause = new SqmSelectClause( ctx.DISTINCT() != null );
			for ( HqlParser.SelectionContext selectionContext : ctx.selectionList().selection() ) {
				selectClause.addSelection( visitSelection( selectionContext ) );
			}
			return selectClause;
		}
		finally {
			pathResolverStack.pop();
		}
	}

	@Override
	public SqmSelection visitSelection(HqlParser.SelectionContext ctx) {
		SqmExpression selectExpression = visitSelectExpression( ctx.selectExpression() );
		if ( selectExpression instanceof PluralAttributeBinding ) {
			selectExpression = new PluralAttributeElementBinding( (PluralAttributeBinding) selectExpression );
		}

		final SqmSelection selection = new SqmSelection(
				selectExpression,
				interpretResultIdentifier( ctx.resultIdentifier() )
		);
		currentQuerySpecProcessingState.getFromElementBuilder().getAliasRegistry().registerAlias( selection );
		return selection;
	}

	private String interpretResultIdentifier(HqlParser.ResultIdentifierContext resultIdentifierContext) {
		if ( resultIdentifierContext != null ) {
			final String explicitAlias;
			if ( resultIdentifierContext.AS() != null ) {
				final Token aliasToken = resultIdentifierContext.identifier().getStart();
				explicitAlias = aliasToken.getText();

				if ( aliasToken.getType() != HqlParser.IDENTIFIER ) {
					// we have a reserved word used as an identification variable.
					if ( parsingContext.getConsumerContext().useStrictJpaCompliance() ) {
						throw new StrictJpaComplianceViolation(
								String.format(
										Locale.ROOT,
										"Strict JPQL compliance was violated : %s [%s]",
										StrictJpaComplianceViolation.Type.RESERVED_WORD_USED_AS_ALIAS.description(),
										explicitAlias
								),
								StrictJpaComplianceViolation.Type.RESERVED_WORD_USED_AS_ALIAS
						);
					}
				}
			}
			else {
				explicitAlias = resultIdentifierContext.getText();
			}
			return explicitAlias;
		}

		return parsingContext.getImplicitAliasGenerator().buildUniqueImplicitAlias();
	}

	private String interpretAlias(HqlParser.IdentifierContext identifier) {
		if ( identifier == null || identifier.getText() == null ) {
			return parsingContext.getImplicitAliasGenerator().buildUniqueImplicitAlias();
		}
		return identifier.getText();
	}

	private String interpretAlias(TerminalNode aliasNode) {
		if ( aliasNode == null ) {
			return parsingContext.getImplicitAliasGenerator().buildUniqueImplicitAlias();
		}

		// todo : not sure I like asserts for this kind of thing.  They are generally disable in runtime environments.
		// either the thing is important to check or it isn't.
		assert aliasNode.getSymbol().getType() == HqlParser.IDENTIFIER;

		return aliasNode.getText();
	}

	@Override
	public SqmExpression visitSelectExpression(HqlParser.SelectExpressionContext ctx) {
		if ( ctx.dynamicInstantiation() != null ) {
			return visitDynamicInstantiation( ctx.dynamicInstantiation() );
		}
		else if ( ctx.jpaSelectObjectSyntax() != null ) {
			return visitJpaSelectObjectSyntax( ctx.jpaSelectObjectSyntax() );
		}
		else if ( ctx.expression() != null ) {
			return (SqmExpression) ctx.expression().accept( this );
		}

		throw new ParsingException( "Unexpected selection rule type : " + ctx.getText() );
	}

	@Override
	public SqmDynamicInstantiation visitDynamicInstantiation(HqlParser.DynamicInstantiationContext ctx) {
		final SqmDynamicInstantiation dynamicInstantiation;

		if ( ctx.dynamicInstantiationTarget().MAP() != null ) {
			dynamicInstantiation = SqmDynamicInstantiation.forMapInstantiation();
		}
		else if ( ctx.dynamicInstantiationTarget().LIST() != null ) {
			dynamicInstantiation = SqmDynamicInstantiation.forListInstantiation();
		}
		else {
			final String className = ctx.dynamicInstantiationTarget().dotIdentifierSequence().getText();
			try {
				final Class targetJavaType = parsingContext.getConsumerContext().classByName( className );
				dynamicInstantiation = SqmDynamicInstantiation.forClassInstantiation( targetJavaType );
			}
			catch (ClassNotFoundException e) {
				throw new SemanticException( "Unable to resolve class named for dynamic instantiation : " + className );
			}
		}

		for ( HqlParser.DynamicInstantiationArgContext arg : ctx.dynamicInstantiationArgs().dynamicInstantiationArg() ) {
			dynamicInstantiation.addArgument( visitDynamicInstantiationArg( arg ) );
		}

		return dynamicInstantiation;
	}

	@Override
	public SqmDynamicInstantiationArgument visitDynamicInstantiationArg(HqlParser.DynamicInstantiationArgContext ctx) {
		return new SqmDynamicInstantiationArgument(
				visitDynamicInstantiationArgExpression( ctx.dynamicInstantiationArgExpression() ),
				ctx.identifier() == null ? null : ctx.identifier().getText()
		);
	}

	@Override
	public SqmExpression visitDynamicInstantiationArgExpression(HqlParser.DynamicInstantiationArgExpressionContext ctx) {
		if ( ctx.dynamicInstantiation() != null ) {
			return visitDynamicInstantiation( ctx.dynamicInstantiation() );
		}
		else if ( ctx.expression() != null ) {
			return (SqmExpression) ctx.expression().accept( this );
		}

		throw new ParsingException( "Unexpected dynamic-instantiation-argument rule type : " + ctx.getText() );
	}

	@Override
	public SqmFrom visitJpaSelectObjectSyntax(HqlParser.JpaSelectObjectSyntaxContext ctx) {
		final String alias = ctx.identifier().getText();
		final DomainReferenceBinding binding = currentQuerySpecProcessingState.getFromElementBuilder().getAliasRegistry().findFromElementByAlias( alias );
		if ( binding == null ) {
			throw new SemanticException( "Unable to resolve alias [" +  alias + "] in selection [" + ctx.getText() + "]" );
		}
		return binding.getFromElement();
	}

	@Override
	public SqmWhereClause visitWhereClause(HqlParser.WhereClauseContext ctx) {
		inWhereClause = true;

		try {
			return new SqmWhereClause( (SqmPredicate) ctx.predicate().accept( this ) );
		}
		finally {
			inWhereClause = false;
		}
	}

	@Override
	public Object visitGroupByClause(HqlParser.GroupByClauseContext ctx) {
		return super.visitGroupByClause( ctx );
	}

	@Override
	public Object visitHavingClause(HqlParser.HavingClauseContext ctx) {
		return super.visitHavingClause( ctx );
	}

	@Override
	public GroupedSqmPredicate visitGroupedPredicate(HqlParser.GroupedPredicateContext ctx) {
		return new GroupedSqmPredicate( (SqmPredicate) ctx.predicate().accept( this ) );
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
		final SqmExpression sortExpression = (SqmExpression) ctx.expression().accept( this );
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

	@Override
	public SqmDeleteStatement visitDeleteStatement(HqlParser.DeleteStatementContext ctx) {
		currentQuerySpecProcessingState = new QuerySpecProcessingStateDmlImpl( parsingContext );
		try {
			final SqmRoot root = resolveDmlRootEntityReference( ctx.mainEntityPersisterReference() );
			final SqmDeleteStatementImpl deleteStatement = new SqmDeleteStatementImpl( root );

			parameterCollector = deleteStatement;

			pathResolverStack.push( new PathResolverBasicImpl( currentQuerySpecProcessingState ) );
			try {
				deleteStatement.getWhereClause().setPredicate( (SqmPredicate) ctx.whereClause()
						.predicate()
						.accept( this ) );
			}
			finally {
				pathResolverStack.pop();
				deleteStatement.wrapUp();
			}

			return deleteStatement;
		}
		finally {
			currentQuerySpecProcessingState = null;
		}
	}

	protected SqmRoot resolveDmlRootEntityReference(HqlParser.MainEntityPersisterReferenceContext rootEntityContext) {
		final EntityReference entityBinding = resolveEntityReference( rootEntityContext.dotIdentifierSequence() );
		String alias = interpretIdentificationVariable( rootEntityContext.identificationVariableDef() );
		if ( alias == null ) {
			alias = parsingContext.getImplicitAliasGenerator().buildUniqueImplicitAlias();
			log.debugf(
					"Generated implicit alias [%s] for DML root entity reference [%s]",
					alias,
					entityBinding.getEntityName()
			);
		}
		final SqmRoot root = new SqmRoot( null, parsingContext.makeUniqueIdentifier(), alias, entityBinding );
		parsingContext.registerFromElementByUniqueId( root );
		currentQuerySpecProcessingState.getFromElementBuilder().getAliasRegistry().registerAlias( root.getDomainReferenceBinding() );
		currentQuerySpecProcessingState.getFromClause().getFromElementSpaces().get( 0 ).setRoot( root );
		return root;
	}

	private String interpretIdentificationVariable(HqlParser.IdentificationVariableDefContext identificationVariableDef) {
		if ( identificationVariableDef != null ) {
			final String explicitAlias;
			if ( identificationVariableDef.AS() != null ) {
				final Token identificationVariableToken = identificationVariableDef.identificationVariable().identifier().getStart();
				if ( identificationVariableToken.getType() != HqlParser.IDENTIFIER ) {
					// we have a reserved word used as an identification variable.
					if ( parsingContext.getConsumerContext().useStrictJpaCompliance() ) {
						throw new StrictJpaComplianceViolation(
								String.format(
										Locale.ROOT,
										"Strict JPQL compliance was violated : %s [%s]",
										StrictJpaComplianceViolation.Type.RESERVED_WORD_USED_AS_ALIAS.description(),
										identificationVariableToken.getText()
								),
								StrictJpaComplianceViolation.Type.RESERVED_WORD_USED_AS_ALIAS
						);
					}
				}
				explicitAlias = identificationVariableToken.getText();
			}
			else {
				explicitAlias = identificationVariableDef.IDENTIFIER().getText();
			}
			return explicitAlias;
		}

		return parsingContext.getImplicitAliasGenerator().buildUniqueImplicitAlias();
	}

	@Override
	public SqmUpdateStatement visitUpdateStatement(HqlParser.UpdateStatementContext ctx) {
		currentQuerySpecProcessingState = new QuerySpecProcessingStateDmlImpl( parsingContext );
		try {
			final SqmRoot root = resolveDmlRootEntityReference( ctx.mainEntityPersisterReference() );
			final SqmUpdateStatementImpl updateStatement = new SqmUpdateStatementImpl( root );

			pathResolverStack.push( new PathResolverBasicImpl( currentQuerySpecProcessingState ) );
			parameterCollector = updateStatement;
			try {
				updateStatement.getWhereClause().setPredicate(
						(SqmPredicate) ctx.whereClause().predicate().accept( this )
				);

				for ( HqlParser.AssignmentContext assignmentContext : ctx.setClause().assignment() ) {
					final SingularAttributeBinding stateField = (SingularAttributeBinding) pathResolverStack.getCurrent().resolvePath(
							splitPathParts( assignmentContext.dotIdentifierSequence() )
					);
					// todo : validate "state field" expression
					updateStatement.getSetClause().addAssignment(
							stateField,
							(SqmExpression) assignmentContext.expression().accept( this )
					);
				}
			}
			finally {
				pathResolverStack.pop();
				updateStatement.wrapUp();
			}

			return updateStatement;
		}
		finally {
			currentQuerySpecProcessingState = null;
		}
	}

	private String[] splitPathParts(HqlParser.DotIdentifierSequenceContext path) {
		final String pathText = path.getText();
		log.debugf( "Splitting dotIdentifierSequence into path parts : %s", pathText );
		return PathHelper.split( pathText );
	}

	@Override
	public SqmInsertSelectStatement visitInsertStatement(HqlParser.InsertStatementContext ctx) {
		currentQuerySpecProcessingState = new QuerySpecProcessingStateDmlImpl( parsingContext );
		try {
			final EntityReference entityBinding = resolveEntityReference( ctx.insertSpec().intoSpec().dotIdentifierSequence() );
			String alias = parsingContext.getImplicitAliasGenerator().buildUniqueImplicitAlias();
			log.debugf(
					"Generated implicit alias [%s] for INSERT target [%s]",
					alias,
					entityBinding.getEntityName()
			);

			SqmRoot root = new SqmRoot( null, parsingContext.makeUniqueIdentifier(), alias, entityBinding );
			parsingContext.registerFromElementByUniqueId( root );
			currentQuerySpecProcessingState.getFromElementBuilder().getAliasRegistry().registerAlias( root.getDomainReferenceBinding() );
			currentQuerySpecProcessingState.getFromClause().getFromElementSpaces().get( 0 ).setRoot( root );

			// for now we only support the INSERT-SELECT form
			final SqmInsertSelectStatementImpl insertStatement = new SqmInsertSelectStatementImpl( root );
			parameterCollector = insertStatement;
			pathResolverStack.push( new PathResolverBasicImpl( currentQuerySpecProcessingState ) );

			try {
				insertStatement.setSelectQuery( visitQuerySpec( ctx.querySpec() ) );

				for ( HqlParser.DotIdentifierSequenceContext stateFieldCtx : ctx.insertSpec().targetFieldsSpec().dotIdentifierSequence() ) {
					final SingularAttributeBinding stateField = (SingularAttributeBinding) pathResolverStack.getCurrent().resolvePath(
							splitPathParts( stateFieldCtx )
					);
					// todo : validate each resolved stateField...
					insertStatement.addInsertTargetStateField( stateField );
				}
			}
			finally {
				pathResolverStack.pop();
				insertStatement.wrapUp();
			}

			return insertStatement;
		}
		finally {
			currentQuerySpecProcessingState = null;
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
	public SqmRoot visitFromElementSpaceRoot(HqlParser.FromElementSpaceRootContext ctx) {
		final EntityReference entityBinding = resolveEntityReference(
				ctx.mainEntityPersisterReference().dotIdentifierSequence()
		);

		if ( PolymorphicEntityReference.class.isInstance( entityBinding ) ) {
			if ( parsingContext.getConsumerContext().useStrictJpaCompliance() ) {
				throw new StrictJpaComplianceViolation(
						"Encountered unmapped polymorphic reference [" + entityBinding.getEntityName()
								+ "], but strict JPQL compliance was requested",
						StrictJpaComplianceViolation.Type.UNMAPPED_POLYMORPHISM
				);
			}

			// todo : disallow in subqueries as well
		}

		return currentQuerySpecProcessingState.getFromElementBuilder().makeRootEntityFromElement(
				currentFromElementSpace,
				entityBinding,
				interpretIdentificationVariable( ctx.mainEntityPersisterReference().identificationVariableDef() )
		);
	}

	private EntityReference resolveEntityReference(HqlParser.DotIdentifierSequenceContext dotIdentifierSequenceContext) {
		final String entityName = dotIdentifierSequenceContext.getText();
		final EntityReference entityTypeDescriptor = parsingContext.getConsumerContext()
				.getDomainMetamodel()
				.resolveEntityReference( entityName );
		if ( entityTypeDescriptor == null ) {
			throw new SemanticException( "Unresolved entity name : " + entityName );
		}
		return entityTypeDescriptor;
	}

	@Override
	public SqmCrossJoin visitCrossJoin(HqlParser.CrossJoinContext ctx) {
		final EntityReference entityBinding = resolveEntityReference(
				ctx.mainEntityPersisterReference().dotIdentifierSequence()
		);

		if ( PolymorphicEntityReference.class.isInstance( entityBinding ) ) {
			throw new SemanticException(
					"Unmapped polymorphic references are only valid as sqm root, not in cross join : " +
							entityBinding.getEntityName()
			);
		}

		return currentQuerySpecProcessingState.getFromElementBuilder().makeCrossJoinedFromElement(
				currentFromElementSpace,
				parsingContext.makeUniqueIdentifier(),
				entityBinding,
				interpretIdentificationVariable( ctx.mainEntityPersisterReference().identificationVariableDef() )
		);
	}

	@Override
	public SqmQualifiedJoin visitJpaCollectionJoin(HqlParser.JpaCollectionJoinContext ctx) {
		pathResolverStack.push(
				new PathResolverJoinAttributeImpl(
						currentQuerySpecProcessingState,
						currentFromElementSpace,
						JoinType.INNER,
						interpretIdentificationVariable( ctx.identificationVariableDef() ),
						false
				)
		);

		try {
			PluralAttributeBinding attributeBinding = asPluralAttribute( (DomainReferenceBinding) ctx.path().accept( this ) );
			return attributeBinding.getFromElement();
		}
		finally {
			pathResolverStack.pop();
		}
	}

	@Override
	public SqmQualifiedJoin visitQualifiedJoin(HqlParser.QualifiedJoinContext ctx) {
		final JoinType joinType;
		if ( ctx.OUTER() != null ) {
			// for outer joins, only left outer joins are currently supported
			if ( ctx.FULL() != null ) {
				throw new SemanticException( "FULL OUTER joins are not yet supported : " + ctx.getText() );
			}
			if ( ctx.RIGHT() != null ) {
				throw new SemanticException( "FULL OUTER joins are not yet supported : " + ctx.getText() );
			}

			joinType = JoinType.LEFT;
		}
		else {
			joinType = JoinType.INNER;
		}

		final String identificationVariable = interpretIdentificationVariable(
				ctx.qualifiedJoinRhs().identificationVariableDef()
		);

		pathResolverStack.push(
				new PathResolverJoinAttributeImpl(
						currentQuerySpecProcessingState,
						currentFromElementSpace,
						joinType,
						identificationVariable,
						ctx.FETCH() != null
				)
		);

		try {
			final SqmQualifiedJoin joinedFromElement;

			// Object because join-target might be either an Entity join (... join Address a on ...)
			// or an attribute-join (... from p.address a on ...)
			final Object joinedPath = ctx.qualifiedJoinRhs().path().accept( this );
			if ( joinedPath instanceof AttributeBinding ) {
				final AttributeBinding binding = (AttributeBinding) joinedPath;
				resolveAttributeJoinIfNot( binding, identificationVariable );
				joinedFromElement = binding.getFromElement();
			}
			else if ( joinedPath instanceof EntityTypeLiteralSqmExpression ) {
				joinedFromElement = currentQuerySpecProcessingState.getFromElementBuilder().buildEntityJoin(
						currentFromElementSpace,
						identificationVariable,
						( (EntityTypeLiteralSqmExpression) joinedPath ).getExpressionType(),
						joinType
				);
			}
			else {
				throw new ParsingException( "Unexpected qualifiedJoin.path resolution type : " + joinedPath );
			}

			currentJoinRhs = joinedFromElement;

			if ( parsingContext.getConsumerContext().useStrictJpaCompliance() ) {
				if ( !ImplicitAliasGenerator.isImplicitAlias( joinedFromElement.getIdentificationVariable() ) ) {
					if ( SingularAttributeBinding.class.isInstance( joinedPath ) ) {
						if ( SingularAttributeBinding.class.cast( joinedPath ).getFromElement().isFetched() ) {
							throw new StrictJpaComplianceViolation(
									"Encountered aliased fetch join, but strict JPQL compliance was requested",
									StrictJpaComplianceViolation.Type.ALIASED_FETCH_JOIN
							);
						}
					}
				}
			}

			if ( ctx.qualifiedJoinPredicate() != null ) {
				joinedFromElement.setOnClausePredicate( visitQualifiedJoinPredicate( ctx.qualifiedJoinPredicate() ) );
			}

			return joinedFromElement;
		}
		finally {
			currentJoinRhs = null;
			pathResolverStack.pop();
		}
	}

	private SqmQualifiedJoin currentJoinRhs;

	@Override
	public SqmPredicate visitQualifiedJoinPredicate(HqlParser.QualifiedJoinPredicateContext ctx) {
		if ( currentJoinRhs == null ) {
			throw new ParsingException( "Expecting join RHS to be set" );
		}

		pathResolverStack.push(
				new PathResolverJoinPredicateImpl( currentQuerySpecProcessingState, currentJoinRhs )
		);
		try {
			return (SqmPredicate) ctx.predicate().accept( this );
		}
		finally {

			pathResolverStack.pop();
		}
	}
	@Override
	public SqmPredicate visitAndPredicate(HqlParser.AndPredicateContext ctx) {
		return new AndSqmPredicate(
				(SqmPredicate) ctx.predicate( 0 ).accept( this ),
				(SqmPredicate) ctx.predicate( 1 ).accept( this )
		);
	}

	@Override
	public SqmPredicate visitOrPredicate(HqlParser.OrPredicateContext ctx) {
		return new OrSqmPredicate(
				(SqmPredicate) ctx.predicate( 0 ).accept( this ),
				(SqmPredicate) ctx.predicate( 1 ).accept( this )
		);
	}

	@Override
	public SqmPredicate visitNegatedPredicate(HqlParser.NegatedPredicateContext ctx) {
		SqmPredicate predicate = (SqmPredicate) ctx.predicate().accept( this );
		if ( predicate instanceof NegatableSqmPredicate ) {
			( (NegatableSqmPredicate) predicate ).negate();
			return predicate;
		}
		else {
			return new NegatedSqmPredicate( predicate );
		}
	}

	@Override
	public NullnessSqmPredicate visitIsNullPredicate(HqlParser.IsNullPredicateContext ctx) {
		return new NullnessSqmPredicate(
				(SqmExpression) ctx.expression().accept( this ),
				ctx.NOT() != null
		);
	}

	@Override
	public EmptinessSqmPredicate visitIsEmptyPredicate(HqlParser.IsEmptyPredicateContext ctx) {
		return new EmptinessSqmPredicate(
				(SqmExpression) ctx.expression().accept( this ),
				ctx.NOT() != null
		);
	}

	@Override
	public RelationalSqmPredicate visitEqualityPredicate(HqlParser.EqualityPredicateContext ctx) {
		final SqmExpression lhs = (SqmExpression) ctx.expression().get( 0 ).accept( this );
		final SqmExpression rhs = (SqmExpression) ctx.expression().get( 1 ).accept( this );

		if ( lhs.getInferableType() != null ) {
			if ( rhs instanceof ImpliedTypeSqmExpression ) {
				( (ImpliedTypeSqmExpression) rhs ).impliedType( lhs.getInferableType() );
			}
		}

		if ( rhs.getInferableType() != null ) {
			if ( lhs instanceof ImpliedTypeSqmExpression ) {
				( (ImpliedTypeSqmExpression) lhs ).impliedType( rhs.getInferableType() );
			}
		}

		return new RelationalSqmPredicate( RelationalSqmPredicate.Operator.EQUAL, lhs, rhs );
	}

	@Override
	public Object visitInequalityPredicate(HqlParser.InequalityPredicateContext ctx) {
		final SqmExpression lhs = (SqmExpression) ctx.expression().get( 0 ).accept( this );
		final SqmExpression rhs = (SqmExpression) ctx.expression().get( 1 ).accept( this );

		if ( lhs.getInferableType() != null ) {
			if ( rhs instanceof ImpliedTypeSqmExpression ) {
				( (ImpliedTypeSqmExpression) rhs ).impliedType( lhs.getInferableType() );
			}
		}

		if ( rhs.getInferableType() != null ) {
			if ( lhs instanceof ImpliedTypeSqmExpression ) {
				( (ImpliedTypeSqmExpression) lhs ).impliedType( rhs.getInferableType() );
			}
		}

		return new RelationalSqmPredicate( RelationalSqmPredicate.Operator.NOT_EQUAL, lhs, rhs );
	}

	@Override
	public Object visitGreaterThanPredicate(HqlParser.GreaterThanPredicateContext ctx) {
		final SqmExpression lhs = (SqmExpression) ctx.expression().get( 0 ).accept( this );
		final SqmExpression rhs = (SqmExpression) ctx.expression().get( 1 ).accept( this );

		if ( lhs.getInferableType() != null ) {
			if ( rhs instanceof ImpliedTypeSqmExpression ) {
				( (ImpliedTypeSqmExpression) rhs ).impliedType( lhs.getInferableType() );
			}
		}

		if ( rhs.getInferableType() != null ) {
			if ( lhs instanceof ImpliedTypeSqmExpression ) {
				( (ImpliedTypeSqmExpression) lhs ).impliedType( rhs.getInferableType() );
			}
		}

		return new RelationalSqmPredicate( RelationalSqmPredicate.Operator.GREATER_THAN, lhs, rhs );
	}

	@Override
	public Object visitGreaterThanOrEqualPredicate(HqlParser.GreaterThanOrEqualPredicateContext ctx) {
		final SqmExpression lhs = (SqmExpression) ctx.expression().get( 0 ).accept( this );
		final SqmExpression rhs = (SqmExpression) ctx.expression().get( 1 ).accept( this );

		if ( lhs.getInferableType() != null ) {
			if ( rhs instanceof ImpliedTypeSqmExpression ) {
				( (ImpliedTypeSqmExpression) rhs ).impliedType( lhs.getInferableType() );
			}
		}

		if ( rhs.getInferableType() != null ) {
			if ( lhs instanceof ImpliedTypeSqmExpression ) {
				( (ImpliedTypeSqmExpression) lhs ).impliedType( rhs.getInferableType() );
			}
		}

		return new RelationalSqmPredicate( RelationalSqmPredicate.Operator.GREATER_THAN_OR_EQUAL, lhs, rhs );
	}

	@Override
	public Object visitLessThanPredicate(HqlParser.LessThanPredicateContext ctx) {
		final SqmExpression lhs = (SqmExpression) ctx.expression().get( 0 ).accept( this );
		final SqmExpression rhs = (SqmExpression) ctx.expression().get( 1 ).accept( this );

		if ( lhs.getInferableType() != null ) {
			if ( rhs instanceof ImpliedTypeSqmExpression ) {
				( (ImpliedTypeSqmExpression) rhs ).impliedType( lhs.getInferableType() );
			}
		}

		if ( rhs.getInferableType() != null ) {
			if ( lhs instanceof ImpliedTypeSqmExpression ) {
				( (ImpliedTypeSqmExpression) lhs ).impliedType( rhs.getInferableType() );
			}
		}

		return new RelationalSqmPredicate( RelationalSqmPredicate.Operator.LESS_THAN, lhs, rhs );
	}

	@Override
	public Object visitLessThanOrEqualPredicate(HqlParser.LessThanOrEqualPredicateContext ctx) {
		final SqmExpression lhs = (SqmExpression) ctx.expression().get( 0 ).accept( this );
		final SqmExpression rhs = (SqmExpression) ctx.expression().get( 1 ).accept( this );

		if ( lhs.getInferableType() != null ) {
			if ( rhs instanceof ImpliedTypeSqmExpression ) {
				( (ImpliedTypeSqmExpression) rhs ).impliedType( lhs.getInferableType() );
			}
		}

		if ( rhs.getInferableType() != null ) {
			if ( lhs instanceof ImpliedTypeSqmExpression ) {
				( (ImpliedTypeSqmExpression) lhs ).impliedType( rhs.getInferableType() );
			}
		}

		return new RelationalSqmPredicate( RelationalSqmPredicate.Operator.LESS_THAN_OR_EQUAL, lhs, rhs );
	}

	@Override
	public Object visitBetweenPredicate(HqlParser.BetweenPredicateContext ctx) {
		final SqmExpression expression = (SqmExpression) ctx.expression().get( 0 ).accept( this );
		final SqmExpression lowerBound = (SqmExpression) ctx.expression().get( 1 ).accept( this );
		final SqmExpression upperBound = (SqmExpression) ctx.expression().get( 2 ).accept( this );

		if ( expression.getInferableType() != null ) {
			if ( lowerBound instanceof ImpliedTypeSqmExpression ) {
				( (ImpliedTypeSqmExpression) lowerBound ).impliedType( expression.getInferableType() );
			}
			if ( upperBound instanceof ImpliedTypeSqmExpression ) {
				( (ImpliedTypeSqmExpression) upperBound ).impliedType( expression.getInferableType() );
			}
		}
		else if ( lowerBound.getInferableType() != null ) {
			if ( expression instanceof ImpliedTypeSqmExpression ) {
				( (ImpliedTypeSqmExpression) expression ).impliedType( lowerBound.getInferableType() );
			}
			if ( upperBound instanceof ImpliedTypeSqmExpression ) {
				( (ImpliedTypeSqmExpression) upperBound ).impliedType( lowerBound.getInferableType() );
			}
		}
		else if ( upperBound.getInferableType() != null ) {
			if ( expression instanceof ImpliedTypeSqmExpression ) {
				( (ImpliedTypeSqmExpression) expression ).impliedType( upperBound.getInferableType() );
			}
			if ( lowerBound instanceof ImpliedTypeSqmExpression ) {
				( (ImpliedTypeSqmExpression) lowerBound ).impliedType( upperBound.getInferableType() );
			}
		}

		return new BetweenSqmPredicate(
				expression,
				lowerBound,
				upperBound,
				false
		);
	}

	@Override
	public SqmPredicate visitLikePredicate(HqlParser.LikePredicateContext ctx) {
		if ( ctx.likeEscape() != null ) {
			return new LikeSqmPredicate(
					(SqmExpression) ctx.expression().get( 0 ).accept( this ),
					(SqmExpression) ctx.expression().get( 1 ).accept( this ),
					(SqmExpression) ctx.likeEscape().expression().accept( this )
			);
		}
		else {
			return new LikeSqmPredicate(
					(SqmExpression) ctx.expression().get( 0 ).accept( this ),
					(SqmExpression) ctx.expression().get( 1 ).accept( this )
			);
		}
	}

	@Override
	public SqmPredicate visitMemberOfPredicate(HqlParser.MemberOfPredicateContext ctx) {
		final DomainReferenceBinding pathResolution = (DomainReferenceBinding) ctx.path().accept( this );

		if ( !SingularAttributeBinding.class.isInstance( pathResolution ) ) {
			throw new SemanticException( "Could not resolve path [" + ctx.path().getText() + "] as an attribute reference" );
		}

		final SingularAttributeBinding attributeBinding = (SingularAttributeBinding) pathResolution;
		if ( !PluralAttributeReference.class.isInstance( attributeBinding.getAttribute() ) ) {
			throw new SemanticException( "Path argument to MEMBER OF must be a collection" );
		}

		return new MemberOfSqmPredicate( attributeBinding );
	}

	@Override
	public SqmPredicate visitInPredicate(HqlParser.InPredicateContext ctx) {
		final SqmExpression testExpression = (SqmExpression) ctx.expression().accept( this );

		if ( HqlParser.ExplicitTupleInListContext.class.isInstance( ctx.inList() ) ) {
			final HqlParser.ExplicitTupleInListContext tupleExpressionListContext = (HqlParser.ExplicitTupleInListContext) ctx.inList();

			parameterDeclarationContextStack.push( () -> tupleExpressionListContext.expression().size() == 1 );
			try {
				final List<SqmExpression> listExpressions = new ArrayList<>( tupleExpressionListContext.expression().size() );
				for ( HqlParser.ExpressionContext expressionContext : tupleExpressionListContext.expression() ) {
					final SqmExpression listItemExpression = (SqmExpression) expressionContext.accept( this );

					if ( testExpression.getInferableType() != null ) {
						if ( listItemExpression instanceof ImpliedTypeSqmExpression ) {
							( (ImpliedTypeSqmExpression) listItemExpression ).impliedType( testExpression.getInferableType() );
						}
					}

					listExpressions.add( listItemExpression );
				}

				return new InListSqmPredicate( testExpression, listExpressions );
			}
			finally {
				parameterDeclarationContextStack.pop();
			}
		}
		else if ( HqlParser.SubQueryInListContext.class.isInstance( ctx.inList() ) ) {
			final HqlParser.SubQueryInListContext subQueryContext = (HqlParser.SubQueryInListContext) ctx.inList();
			final SqmExpression subQueryExpression = (SqmExpression) subQueryContext.expression().accept( this );

			if ( !SubQuerySqmExpression.class.isInstance( subQueryExpression ) ) {
				throw new ParsingException(
						"Was expecting a SubQueryExpression, but found " + subQueryExpression.getClass().getSimpleName()
								+ " : " + subQueryContext.expression().toString()
				);
			}

			return new InSubQuerySqmPredicate( testExpression, (SubQuerySqmExpression) subQueryExpression );
		}

		// todo : handle PersistentCollectionReferenceInList labeled branch

		throw new ParsingException( "Unexpected IN predicate type [" + ctx.getClass().getSimpleName() + "] : " + ctx.getText() );
	}

	@Override
	public Object visitEntityTypeExpression(HqlParser.EntityTypeExpressionContext ctx) {
		// can be one of 2 forms:
		//		1) TYPE( some.path )
		//		2) TYPE( :someParam )
		if ( ctx.entityTypeReference().parameter() != null ) {
			// we have form (2)
			return new ParameterizedEntityTypeSqmExpression(
					(ParameterSqmExpression) ctx.entityTypeReference().parameter().accept( this )
			);
		}
		else if ( ctx.entityTypeReference().path() != null ) {
			final DomainReferenceBinding binding = (DomainReferenceBinding) ctx.entityTypeReference().path().accept( this );
			validateBindingAsEntityTypeExpression( binding );
			return new EntityTypeSqmExpression( binding );
		}

		throw new ParsingException( "Could not interpret grammar context as 'entity type' expression : " + ctx.getText() );
	}

	private void validateBindingAsEntityTypeExpression(DomainReferenceBinding binding) {
		if ( binding instanceof EntityBinding ) {
			// its ok
			return;
		}

		if ( binding instanceof SingularAttributeBinding ) {
			final SingularAttributeBinding attrBinding = (SingularAttributeBinding) binding;

			if ( attrBinding.getAttribute() instanceof SingularAttributeReference ) {
				final SingularAttributeReference attrRef = (SingularAttributeReference) attrBinding.getAttribute();
				if ( attrRef.getAttributeTypeClassification() == SingularAttributeClassification.BASIC
						|| attrRef.getAttributeTypeClassification() == SingularAttributeClassification.EMBEDDED ) {
					throw new SemanticException(
							"Path used in TYPE() resolved to a singular attribute of non-entity type : "
									+ attrRef.getAttributeTypeClassification().name()
					);
				}

				return;
			}
		}

		// this ^^ is all JPA allows.  Any of these below should technically validate against strict compliance check

		if ( binding instanceof SingularAttributeBinding ) {
			final SingularAttributeBinding attrBinding = (SingularAttributeBinding) binding;

			if ( attrBinding.getAttribute() instanceof PluralAttributeReference ) {
				final PluralAttributeReference attrRef = (PluralAttributeReference) attrBinding.getAttribute();
				if ( attrRef.getElementReference().getClassification() == ElementClassification.ANY
						|| attrRef.getElementReference().getClassification() == ElementClassification.BASIC
						|| attrRef.getElementReference().getClassification() == ElementClassification.EMBEDDABLE ) {
					throw new SemanticException(
							"Path used in TYPE() resolved to a plural attribute of non-entity type elements : "
									+ attrRef.getElementReference().getClassification().name()
					);
				}

				return;
			}
		}

		if ( binding instanceof MapKeyBinding ) {
			final MapKeyBinding mapKeyBinding = (MapKeyBinding) binding;
			final PluralAttributeReference attrRef = (PluralAttributeReference) mapKeyBinding.getPluralAttributeBinding().getAttribute();
			if ( attrRef.getIndexReference().getClassification() == IndexClassification.ANY
					|| attrRef.getIndexReference().getClassification() == IndexClassification.BASIC
					|| attrRef.getIndexReference().getClassification() == IndexClassification.EMBEDDABLE ) {
				throw new SemanticException(
						"Path used in TYPE() [" + mapKeyBinding.asLoggableText() + "] resolved to a Map KEY() expression, but the Map's keys are of non-entity type : "
								+ attrRef.getIndexReference().getClassification().name()
				);
			}
		}

		if ( binding instanceof PluralAttributeElementBinding ) {
			final PluralAttributeElementBinding elementBinding = (PluralAttributeElementBinding) binding;
			final PluralAttributeReference attrRef = (PluralAttributeReference) elementBinding.getPluralAttributeBinding().getAttribute();
			if ( attrRef.getElementReference().getClassification() == ElementClassification.ANY
					|| attrRef.getElementReference().getClassification() == ElementClassification.BASIC
					|| attrRef.getElementReference().getClassification() == ElementClassification.EMBEDDABLE ) {
				throw new SemanticException(
						"Path used in TYPE() [" + elementBinding.asLoggableText() + "] resolved to a plural attribute VALUE() expression, but the elements are of non-entity type : "
								+ attrRef.getElementReference().getClassification().name()
				);
			}
		}

		if ( binding instanceof PluralAttributeIndexedAccessBinding ) {
			final PluralAttributeIndexedAccessBinding indexedAccessBinding = (PluralAttributeIndexedAccessBinding) binding;
			final PluralAttributeReference attrRef = (PluralAttributeReference) indexedAccessBinding.getPluralAttributeBinding().getAttribute();
			if ( attrRef.getElementReference().getClassification() == ElementClassification.ANY
					|| attrRef.getElementReference().getClassification() == ElementClassification.BASIC
					|| attrRef.getElementReference().getClassification() == ElementClassification.EMBEDDABLE ) {
				throw new SemanticException(
						"Path used in TYPE() [" + indexedAccessBinding.asLoggableText() + "] resolved to an index-access expression, but the elements are of non-entity type : "
								+ attrRef.getIndexReference().getClassification().name()
				);
			}
		}

	}

	@Override
	public SqmExpression visitSimplePath(HqlParser.SimplePathContext ctx) {
		// SimplePath might represent any number of things
		final DomainReferenceBinding binding = pathAsDomainReferenceBinding( splitPathParts( ctx.dotIdentifierSequence() ) );
		if ( binding != null ) {
			return binding;
		}

		final String pathText = ctx.getText();

		try {
			final EntityReference entityType = parsingContext.getConsumerContext().getDomainMetamodel().resolveEntityReference( pathText );
			if ( entityType != null ) {
				return new EntityTypeLiteralSqmExpression( entityType );
			}
		}
		catch (IllegalArgumentException ignore) {
		}

		try {
			return resolveConstantExpression( pathText );
		}
		catch (SemanticException e) {
			log.debug( e.getMessage() );
		}

		// if we get here we had a problem interpreting the dot-ident sequence
		throw new SemanticException( "Could not interpret token : " + pathText );
	}

	protected DomainReferenceBinding pathAsDomainReferenceBinding(String[] pathParts) {
		return pathResolverStack.getCurrent().resolvePath( pathParts );
	}

	@Override
	public MapEntrySqmExpression visitMapEntryPath(HqlParser.MapEntryPathContext ctx) {
		if ( inWhereClause ) {
			throw new SemanticException(
					"entry() function may only be used in SELECT clauses; specified "
							+ "path [" + ctx.pathAsMap().path().getText() + "] is used in WHERE clause" );
		}

		final PluralAttributeBinding pathResolution = asMap( (DomainReferenceBinding) ctx.pathAsMap().path().accept( this ) );
		resolveAttributeJoinIfNot( pathResolution );
		return new MapEntrySqmExpression( pathResolution );
	}

	private void resolveAttributeJoinIfNot(AttributeBinding binding) {
		resolveAttributeJoinIfNot( binding, null );
	}

	private void resolveAttributeJoinIfNot(AttributeBinding attributeBinding, String alias) {
		if ( attributeBinding.getFromElement() != null ) {
			return;
		}

		// binding can be any of:
		//		2) From
		//		1) AttributeBiding
		attributeBinding.injectAttributeJoin(
				currentQuerySpecProcessingState.getFromElementBuilder().buildAttributeJoin(
						attributeBinding,
						alias,
						null,
						attributeBinding.getLhs().getFromElement().asLoggableText() + '.' + attributeBinding.getAttribute().getAttributeName(),
						JoinType.INNER,
						false,
						true
				)
		);
	}

	@Override
	public DomainReferenceBinding visitIndexedPath(HqlParser.IndexedPathContext ctx) {
		final Object pathResolution = ctx.path().accept( this );
		if ( !PluralAttributeBinding.class.isInstance( pathResolution ) ) {
			// assume it is a semantic problem...
			throw new SemanticException(
					"Expecting reference to a plural-attribute, but path [" + ctx.path().getText() +
							"] resolved to : " + pathResolution
			);
		}

		final PluralAttributeBinding attributeBinding = (PluralAttributeBinding) pathResolution;
		if ( !PluralAttributeReference.class.isInstance( attributeBinding.getAttribute() )
				|| attributeBinding.getAttribute().getIndexReference() == null ) {
			throw new SemanticException(
					"Index operator only valid for indexed collections (maps, lists, arrays) : " +
							attributeBinding.getAttribute()
			);
		}

		final SqmExpression indexExpression = (SqmExpression) ctx.expression().accept( this );

		// todo : would be nice to validate the index's type against the Collection-index's type

		final PluralAttributeIndexedAccessBinding indexedReference = new PluralAttributeIndexedAccessBinding(
				attributeBinding,
				indexExpression
		);

		if ( ctx.pathTerminal() == null ) {
			return indexedReference;
		}

		// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// otherwise, we have a dereference of the pathRoot (as a pathTerminal)

		// the binding would additionally need to be an AttributeBindingSource
		// and expose a Bindable

		final PluralAttributeReference attRef = attributeBinding.getAttribute();
		if ( !canBeDereferenced( attRef.getElementReference().getClassification() ) ) {
			throw new SemanticException(
					String.format(
							Locale.ROOT,
							"Plural attribute elements [%s.%s - as resolved from %s] cannot be dereferenced - [%s]",
							attributeBinding.getLhs().getFromElement().asLoggableText(),
							attributeBinding.getAttribute().getAttributeName(),
							ctx.path().getText(),
							attRef.getElementReference().getClassification().name()
					)
			);
		}

		return pathResolverStack.getCurrent().resolvePath(
				indexedReference,
				PathHelper.split( ctx.pathTerminal().getText() )
		);
	}

	private boolean canBeDereferenced(ElementClassification elementClassification) {
		return elementClassification == ElementClassification.EMBEDDABLE
				|| elementClassification == ElementClassification.ONE_TO_MANY
				|| elementClassification == ElementClassification.MANY_TO_MANY;
	}

	private boolean canBeDereferenced(IndexClassification indexClassification) {
		return indexClassification == IndexClassification.EMBEDDABLE
				|| indexClassification == IndexClassification.ONE_TO_MANY
				|| indexClassification == IndexClassification.MANY_TO_MANY;
	}

	@Override
	public DomainReferenceBinding visitCompoundPath(HqlParser.CompoundPathContext ctx) {
		final DomainReferenceBinding root = (DomainReferenceBinding) ctx.pathRoot().accept( this );

		log.debugf(
				"Resolved CompoundPath pathRoot [%s] : %s",
				ctx.pathRoot().getText(),
				root
		);

		if ( ctx.pathTerminal() == null ) {
			return root;
		}

		return pathResolverStack.getCurrent().resolvePath(
				root,
				PathHelper.split( ctx.pathTerminal().getText() )
		);
	}

	@Override
	public Object visitMapKeyPathRoot(HqlParser.MapKeyPathRootContext ctx) {
		final PluralAttributeBinding pathResolution = visitPathAsMap( ctx.pathAsMap() );
		resolveAttributeJoinIfNot( pathResolution );
		return new MapKeyBinding( pathResolution );
	}

	private PluralAttributeBinding asMap(DomainReferenceBinding binding) {
		PluralAttributeBinding attributeBinding = asPluralAttribute( binding );
		if ( attributeBinding.getAttribute().getCollectionClassification() != CollectionClassification.MAP ) {
			throw new SemanticException( "Expecting persistent Map reference, but found : " + binding );
		}

		return attributeBinding;
	}

	@Override
	public PluralAttributeElementBinding visitCollectionValuePathRoot(HqlParser.CollectionValuePathRootContext ctx) {
		final PluralAttributeBinding attributeBinding = asPluralAttribute( (DomainReferenceBinding) ctx.collectionReference().accept( this ) );

		if ( parsingContext.getConsumerContext().useStrictJpaCompliance() ) {
			if ( attributeBinding.getAttribute().getCollectionClassification() != CollectionClassification.MAP ) {
				throw new StrictJpaComplianceViolation(
						"Encountered application of value() function to path expression which does not " +
								"resolve to a persistent Map, but strict JPQL compliance was requested. specified "
								+ "path [" + ctx.collectionReference().path().getText() + "] resolved to " + attributeBinding,
						StrictJpaComplianceViolation.Type.VALUE_FUNCTION_ON_NON_MAP
				);
			}
		}

		resolveAttributeJoinIfNot( attributeBinding );
		return new PluralAttributeElementBinding( attributeBinding );
	}

	@SuppressWarnings("RedundantIfStatement")
	private PluralAttributeBinding asPluralAttribute(DomainReferenceBinding attributeBinding) {
		if ( !PluralAttributeReference.class.isInstance( attributeBinding.getBoundDomainReference() ) ) {
			throw new SemanticException( "Expecting plural-attribute, but found : " + attributeBinding );
		}

		return (PluralAttributeBinding) attributeBinding;
	}

	@Override
	public PluralAttributeBinding visitCollectionReference(HqlParser.CollectionReferenceContext ctx) {
		return asPluralAttribute( (DomainReferenceBinding) ctx.path().accept( this ) );
	}

	@Override
	public PluralAttributeBinding visitPathAsMap(HqlParser.PathAsMapContext ctx) {
		final DomainReferenceBinding pathResolution = (DomainReferenceBinding) ctx.path().accept( this );
		return asMap( pathResolution );
	}

	@Override
	public DomainReferenceBinding visitTreatedPathRoot(HqlParser.TreatedPathRootContext ctx) {
		final String treatAsName = ctx.dotIdentifierSequence().get( 1 ).getText();
		final EntityReference treatAsTypeDescriptor = parsingContext.getConsumerContext()
				.getDomainMetamodel()
				.resolveEntityReference( treatAsName );
		if ( treatAsTypeDescriptor == null ) {
			throw new SemanticException( "TREAT-AS target type [" + treatAsName + "] did not reference an entity" );
		}

		return pathResolverStack.getCurrent().resolveTreatedPath(
				treatAsTypeDescriptor,
				splitPathParts( ctx.dotIdentifierSequence().get( 0 ) )
		);
	}

	@SuppressWarnings("unchecked")
	protected ConstantSqmExpression resolveConstantExpression(String reference) {
		// todo : hook in "import" resolution using the ParsingContext
		final int dotPosition = reference.lastIndexOf( '.' );
		final String className = reference.substring( 0, dotPosition - 1 );
		final String fieldName = reference.substring( dotPosition+1, reference.length() );

		try {
			final Class clazz = parsingContext.getConsumerContext().classByName( className );
			if ( clazz.isEnum() ) {
				try {
					return new ConstantEnumSqmExpression( Enum.valueOf( clazz, fieldName ) );
				}
				catch (IllegalArgumentException e) {
					throw new SemanticException( "Name [" + fieldName + "] does not represent an enum constant on enum class [" + className + "]" );
				}
			}
			else {
				try {
					final Field field = clazz.getField( fieldName );
					if ( !Modifier.isStatic( field.getModifiers() ) ) {
						throw new SemanticException( "Field [" + fieldName + "] is not static on class [" + className + "]" );
					}
					field.setAccessible( true );
					return new ConstantFieldSqmExpression( field, field.get( null ) );
				}
				catch (NoSuchFieldException e) {
					throw new SemanticException( "Name [" + fieldName + "] does not represent a field on class [" + className + "]", e );
				}
				catch (SecurityException e) {
					throw new SemanticException( "Field [" + fieldName + "] is not accessible on class [" + className + "]", e );
				}
				catch (IllegalAccessException e) {
					throw new SemanticException( "Unable to access field [" + fieldName + "] on class [" + className + "]", e );
				}
			}
		}
		catch (ClassNotFoundException e) {
			throw new SemanticException( "Cannot resolve class for sqm constant [" + reference + "]" );
		}
	}

	@Override
	public ConcatSqmExpression visitConcatenationExpression(HqlParser.ConcatenationExpressionContext ctx) {
		if ( ctx.expression().size() != 2 ) {
			throw new ParsingException( "Expecting 2 operands to the concat operator" );
		}
		return new ConcatSqmExpression(
				(SqmExpression) ctx.expression( 0 ).accept( this ),
				(SqmExpression) ctx.expression( 1 ).accept( this )
		);
	}

	@Override
	public Object visitAdditionExpression(HqlParser.AdditionExpressionContext ctx) {
		if ( ctx.expression().size() != 2 ) {
			throw new ParsingException( "Expecting 2 operands to the + operator" );
		}

		final SqmExpression firstOperand = (SqmExpression) ctx.expression( 0 ).accept( this );
		final SqmExpression secondOperand = (SqmExpression) ctx.expression( 1 ).accept( this );
		return new BinaryArithmeticSqmExpression(
				BinaryArithmeticSqmExpression.Operation.ADD,
				firstOperand,
				secondOperand,
				parsingContext.getConsumerContext().getDomainMetamodel().resolveArithmeticType(
						firstOperand.getExpressionType(),
						secondOperand.getExpressionType(),
						BinaryArithmeticSqmExpression.Operation.ADD
				)
		);
	}

	@Override
	public Object visitSubtractionExpression(HqlParser.SubtractionExpressionContext ctx) {
		if ( ctx.expression().size() != 2 ) {
			throw new ParsingException( "Expecting 2 operands to the - operator" );
		}

		final SqmExpression firstOperand = (SqmExpression) ctx.expression( 0 ).accept( this );
		final SqmExpression secondOperand = (SqmExpression) ctx.expression( 1 ).accept( this );
		return new BinaryArithmeticSqmExpression(
				BinaryArithmeticSqmExpression.Operation.SUBTRACT,
				firstOperand,
				secondOperand,
				parsingContext.getConsumerContext().getDomainMetamodel().resolveArithmeticType(
						firstOperand.getExpressionType(),
						secondOperand.getExpressionType(),
						BinaryArithmeticSqmExpression.Operation.SUBTRACT
				)
		);
	}

	@Override
	public Object visitMultiplicationExpression(HqlParser.MultiplicationExpressionContext ctx) {
		if ( ctx.expression().size() != 2 ) {
			throw new ParsingException( "Expecting 2 operands to the * operator" );
		}

		final SqmExpression firstOperand = (SqmExpression) ctx.expression( 0 ).accept( this );
		final SqmExpression secondOperand = (SqmExpression) ctx.expression( 1 ).accept( this );
		return new BinaryArithmeticSqmExpression(
				BinaryArithmeticSqmExpression.Operation.MULTIPLY,
				firstOperand,
				secondOperand,
				parsingContext.getConsumerContext().getDomainMetamodel().resolveArithmeticType(
						firstOperand.getExpressionType(),
						secondOperand.getExpressionType(),
						BinaryArithmeticSqmExpression.Operation.MULTIPLY
				)
		);
	}

	@Override
	public Object visitDivisionExpression(HqlParser.DivisionExpressionContext ctx) {
		if ( ctx.expression().size() != 2 ) {
			throw new ParsingException( "Expecting 2 operands to the / operator" );
		}

		final SqmExpression firstOperand = (SqmExpression) ctx.expression( 0 ).accept( this );
		final SqmExpression secondOperand = (SqmExpression) ctx.expression( 1 ).accept( this );
		return new BinaryArithmeticSqmExpression(
				BinaryArithmeticSqmExpression.Operation.DIVIDE,
				firstOperand,
				secondOperand,
				parsingContext.getConsumerContext().getDomainMetamodel().resolveArithmeticType(
						firstOperand.getExpressionType(),
						secondOperand.getExpressionType(),
						BinaryArithmeticSqmExpression.Operation.DIVIDE
				)
		);
	}

	@Override
	public Object visitModuloExpression(HqlParser.ModuloExpressionContext ctx) {
		if ( ctx.expression().size() != 2 ) {
			throw new ParsingException( "Expecting 2 operands to the % operator" );
		}

		final SqmExpression firstOperand = (SqmExpression) ctx.expression( 0 ).accept( this );
		final SqmExpression secondOperand = (SqmExpression) ctx.expression( 1 ).accept( this );
		return new BinaryArithmeticSqmExpression(
				BinaryArithmeticSqmExpression.Operation.MODULO,
				firstOperand,
				secondOperand,
				parsingContext.getConsumerContext().getDomainMetamodel().resolveArithmeticType(
						firstOperand.getExpressionType(),
						secondOperand.getExpressionType(),
						BinaryArithmeticSqmExpression.Operation.MODULO
				)
		);
	}

	@Override
	public Object visitUnaryPlusExpression(HqlParser.UnaryPlusExpressionContext ctx) {
		return new UnaryOperationSqmExpression(
				UnaryOperationSqmExpression.Operation.PLUS,
				(SqmExpression) ctx.expression().accept( this )
		);
	}

	@Override
	public Object visitUnaryMinusExpression(HqlParser.UnaryMinusExpressionContext ctx) {
		return new UnaryOperationSqmExpression(
				UnaryOperationSqmExpression.Operation.MINUS,
				(SqmExpression) ctx.expression().accept( this )
		);
	}

	@Override
	public CaseSimpleSqmExpression visitSimpleCaseStatement(HqlParser.SimpleCaseStatementContext ctx) {
		final CaseSimpleSqmExpression caseExpression = new CaseSimpleSqmExpression(
				(SqmExpression) ctx.expression().accept( this )
		);

		for ( HqlParser.SimpleCaseWhenContext simpleCaseWhen : ctx.simpleCaseWhen() ) {
			caseExpression.when(
					(SqmExpression) simpleCaseWhen.expression( 0 ).accept( this ),
					(SqmExpression) simpleCaseWhen.expression( 0 ).accept( this )
			);
		}

		if ( ctx.caseOtherwise() != null ) {
			caseExpression.otherwise( (SqmExpression) ctx.caseOtherwise().expression().accept( this ) );
		}

		return caseExpression;
	}

	@Override
	public CaseSearchedSqmExpression visitSearchedCaseStatement(HqlParser.SearchedCaseStatementContext ctx) {
		final CaseSearchedSqmExpression caseExpression = new CaseSearchedSqmExpression();

		for ( HqlParser.SearchedCaseWhenContext whenFragment : ctx.searchedCaseWhen() ) {
			caseExpression.when(
					(SqmPredicate) whenFragment.predicate().accept( this ),
					(SqmExpression) whenFragment.expression().accept( this )
			);
		}

		if ( ctx.caseOtherwise() != null ) {
			caseExpression.otherwise( (SqmExpression) ctx.caseOtherwise().expression().accept( this ) );
		}

		return caseExpression;
	}

	@Override
	public CoalesceSqmExpression visitCoalesceExpression(HqlParser.CoalesceExpressionContext ctx) {
		CoalesceSqmExpression coalesceExpression = new CoalesceSqmExpression();
		for ( HqlParser.ExpressionContext expressionContext : ctx.coalesce().expression() ) {
			coalesceExpression.value( (SqmExpression) expressionContext.accept( this ) );
		}
		return coalesceExpression;
	}

	@Override
	public NullifSqmExpression visitNullIfExpression(HqlParser.NullIfExpressionContext ctx) {
		return new NullifSqmExpression(
				(SqmExpression) ctx.nullIf().expression( 0 ).accept( this ),
				(SqmExpression) ctx.nullIf().expression( 1 ).accept( this )
		);
	}

	@Override
	@SuppressWarnings("UnnecessaryBoxing")
	public LiteralSqmExpression visitLiteralExpression(HqlParser.LiteralExpressionContext ctx) {
		if ( ctx.literal().CHARACTER_LITERAL() != null ) {
			return characterLiteral( ctx.literal().CHARACTER_LITERAL().getText() );
		}
		else if ( ctx.literal().STRING_LITERAL() != null ) {
			return stringLiteral( ctx.literal().STRING_LITERAL().getText() );
		}
		else if ( ctx.literal().INTEGER_LITERAL() != null ) {
			return integerLiteral( ctx.literal().INTEGER_LITERAL().getText() );
		}
		else if ( ctx.literal().LONG_LITERAL() != null ) {
			return longLiteral( ctx.literal().LONG_LITERAL().getText() );
		}
		else if ( ctx.literal().BIG_INTEGER_LITERAL() != null ) {
			return bigIntegerLiteral( ctx.literal().BIG_INTEGER_LITERAL().getText() );
		}
		else if ( ctx.literal().HEX_LITERAL() != null ) {
			final String text = ctx.literal().HEX_LITERAL().getText();
			if ( text.endsWith( "l" ) || text.endsWith( "L" ) ) {
				return longLiteral( text );
			}
			else {
				return integerLiteral( text );
			}
		}
		else if ( ctx.literal().OCTAL_LITERAL() != null ) {
			final String text = ctx.literal().OCTAL_LITERAL().getText();
			if ( text.endsWith( "l" ) || text.endsWith( "L" ) ) {
				return longLiteral( text );
			}
			else {
				return integerLiteral( text );
			}
		}
		else if ( ctx.literal().FLOAT_LITERAL() != null ) {
			return floatLiteral( ctx.literal().FLOAT_LITERAL().getText() );
		}
		else if ( ctx.literal().DOUBLE_LITERAL() != null ) {
			return doubleLiteral( ctx.literal().DOUBLE_LITERAL().getText() );
		}
		else if ( ctx.literal().BIG_DECIMAL_LITERAL() != null ) {
			return bigDecimalLiteral( ctx.literal().BIG_DECIMAL_LITERAL().getText() );
		}
		else if ( ctx.literal().FALSE() != null ) {
			booleanLiteral( false );
		}
		else if ( ctx.literal().TRUE() != null ) {
			booleanLiteral( true );
		}
		else if ( ctx.literal().NULL() != null ) {
			return new LiteralNullSqmExpression();
		}

		// otherwise we have a problem
		throw new ParsingException( "Unexpected literal expression type [" + ctx.getText() + "]" );
	}

	private LiteralSqmExpression<Boolean> booleanLiteral(boolean value) {
		return value
				? new LiteralTrueSqmExpression()
				: new LiteralFalseSqmExpression();
	}

	private LiteralCharacterSqmExpression characterLiteral(String text) {
		if ( text.length() > 1 ) {
			// todo : or just treat it as a String literal?
			throw new ParsingException( "Value for CHARACTER_LITERAL token was more than 1 character" );
		}
		return new LiteralCharacterSqmExpression( text.charAt( 0 ) );
	}

	private LiteralSqmExpression stringLiteral(String text) {
		return new LiteralStringSqmExpression( text );
	}

	protected LiteralIntegerSqmExpression integerLiteral(String text) {
		try {
			final Integer value = Integer.valueOf( text );
			return new LiteralIntegerSqmExpression( value );
		}
		catch (NumberFormatException e) {
			throw new LiteralNumberFormatException(
					"Unable to convert sqm literal [" + text + "] to Integer",
					e
			);
		}
	}

	protected LiteralLongSqmExpression longLiteral(String text) {
		final String originalText = text;
		try {
			if ( text.endsWith( "l" ) || text.endsWith( "L" ) ) {
				text = text.substring( 0, text.length() - 1 );
			}
			final Long value = Long.valueOf( text );
			return new LiteralLongSqmExpression( value );
		}
		catch (NumberFormatException e) {
			throw new LiteralNumberFormatException(
					"Unable to convert sqm literal [" + originalText + "] to Long",
					e
			);
		}
	}

	protected LiteralBigIntegerSqmExpression bigIntegerLiteral(String text) {
		final String originalText = text;
		try {
			if ( text.endsWith( "bi" ) || text.endsWith( "BI" ) ) {
				text = text.substring( 0, text.length() - 2 );
			}
			return new LiteralBigIntegerSqmExpression( new BigInteger( text ) );
		}
		catch (NumberFormatException e) {
			throw new LiteralNumberFormatException(
					"Unable to convert sqm literal [" + originalText + "] to BigInteger",
					e
			);
		}
	}

	protected LiteralFloatSqmExpression floatLiteral(String text) {
		try {
			return new LiteralFloatSqmExpression( Float.valueOf( text ) );
		}
		catch (NumberFormatException e) {
			throw new LiteralNumberFormatException(
					"Unable to convert sqm literal [" + text + "] to Float",
					e
			);
		}
	}

	protected LiteralDoubleSqmExpression doubleLiteral(String text) {
		try {
			return new LiteralDoubleSqmExpression( Double.valueOf( text ) );
		}
		catch (NumberFormatException e) {
			throw new LiteralNumberFormatException(
					"Unable to convert sqm literal [" + text + "] to Double",
					e
			);
		}
	}

	protected LiteralBigDecimalSqmExpression bigDecimalLiteral(String text) {
		final String originalText = text;
		try {
			if ( text.endsWith( "bd" ) || text.endsWith( "BD" ) ) {
				text = text.substring( 0, text.length() - 2 );
			}
			return new LiteralBigDecimalSqmExpression( new BigDecimal( text ) );
		}
		catch (NumberFormatException e) {
			throw new LiteralNumberFormatException(
					"Unable to convert sqm literal [" + originalText + "] to BigDecimal",
					e
			);
		}
	}

	@Override
	public Object visitParameterExpression(HqlParser.ParameterExpressionContext ctx) {
		return ctx.parameter().accept( this );
	}

	@Override
	public NamedParameterSqmExpression visitNamedParameter(HqlParser.NamedParameterContext ctx) {
		final NamedParameterSqmExpression param = new NamedParameterSqmExpression(
				ctx.identifier().getText(),
				parameterDeclarationContextStack.getCurrent().isMultiValuedBindingAllowed()
		);
		parameterCollector.addParameter( param );
		return param;
	}

	@Override
	public PositionalParameterSqmExpression visitPositionalParameter(HqlParser.PositionalParameterContext ctx) {
		final PositionalParameterSqmExpression param = new PositionalParameterSqmExpression(
				Integer.valueOf( ctx.INTEGER_LITERAL().getText() ),
				parameterDeclarationContextStack.getCurrent().isMultiValuedBindingAllowed()
		);
		parameterCollector.addParameter( param );
		return param;
	}

	@Override
	public GenericFunctionSqmExpression visitJpaNonStandardFunction(HqlParser.JpaNonStandardFunctionContext ctx) {
		final String functionName = ctx.nonStandardFunctionName().getText();
		final List<SqmExpression> functionArguments = visitNonStandardFunctionArguments( ctx.nonStandardFunctionArguments() );

		// todo : integrate some form of SqlFunction look-up using the ParsingContext so we can resolve the "type"
		return new GenericFunctionSqmExpression( functionName, null, functionArguments );
	}

	@Override
	public GenericFunctionSqmExpression visitNonStandardFunction(HqlParser.NonStandardFunctionContext ctx) {
		if ( parsingContext.getConsumerContext().useStrictJpaCompliance() ) {
			throw new StrictJpaComplianceViolation(
					"Encountered non-compliant non-standard function call [" +
							ctx.nonStandardFunctionName() + "], but strict JPQL compliance was requested; use JPA's FUNCTION(functionName[,...]) syntax name instead",
					StrictJpaComplianceViolation.Type.FUNCTION_CALL
			);
		}

		final String functionName = ctx.nonStandardFunctionName().getText();
		final List<SqmExpression> functionArguments = visitNonStandardFunctionArguments( ctx.nonStandardFunctionArguments() );

		// todo : integrate some form of SqlFunction look-up using the ParsingContext so we can resolve the "type"
		return new GenericFunctionSqmExpression( functionName, null, functionArguments );
	}

	@Override
	public List<SqmExpression> visitNonStandardFunctionArguments(HqlParser.NonStandardFunctionArgumentsContext ctx) {
		final List<SqmExpression> arguments = new ArrayList<SqmExpression>();

		for ( int i=0, x=ctx.expression().size(); i<x; i++ ) {
			// we handle the final argument differently...
			if ( i == x-1 ) {
				arguments.add( visitFinalFunctionArgument( ctx.expression( i ) ) );
			}
			else {
				arguments.add( (SqmExpression) ctx.expression( i ).accept( this ) );
			}
		}

		return arguments;
	}

	private SqmExpression visitFinalFunctionArgument(HqlParser.ExpressionContext expression) {
		// the final argument to a function may accept multi-value parameter (varargs),
		// 		but only if we are operating in non-strict JPA mode
		parameterDeclarationContextStack.push( () -> parsingContext.getConsumerContext().useStrictJpaCompliance() );
		try {
			return (SqmExpression) expression.accept( this );
		}
		finally {
			parameterDeclarationContextStack.pop();
		}
	}

	@Override
	public AggregateFunctionSqmExpression visitAggregateFunction(HqlParser.AggregateFunctionContext ctx) {
		return (AggregateFunctionSqmExpression) super.visitAggregateFunction( ctx );
	}

	@Override
	public AvgFunctionSqmExpression visitAvgFunction(HqlParser.AvgFunctionContext ctx) {
		final SqmExpression expr = (SqmExpression) ctx.expression().accept( this );
		return new AvgFunctionSqmExpression(
				expr,
				ctx.DISTINCT() != null,
				expr.getExpressionType()
		);
	}

	@Override
	public CastFunctionSqmExpression visitCastFunction(HqlParser.CastFunctionContext ctx) {
		return new CastFunctionSqmExpression(
				(SqmExpression) ctx.expression().accept( this ),
				parsingContext.getConsumerContext().getDomainMetamodel().resolveCastTargetType( ctx.dataType().IDENTIFIER().getText() )
		);
	}

	@Override
	public ConcatFunctionSqmExpression visitConcatFunction(HqlParser.ConcatFunctionContext ctx) {
		final List<SqmExpression> arguments = new ArrayList<>();
		for ( HqlParser.ExpressionContext argument : ctx.expression() ) {
			arguments.add( (SqmExpression) argument.accept( this ) );
		}

		return new ConcatFunctionSqmExpression( arguments.get( 0 ).getExpressionType(), arguments );
	}

	@Override
	public AggregateFunctionSqmExpression visitCountFunction(HqlParser.CountFunctionContext ctx) {
		final DomainReference longType = parsingContext.getConsumerContext().getDomainMetamodel().resolveBasicType( Long.class );
		if ( ctx.ASTERISK() != null ) {
			return new CountStarFunctionSqmExpression( ctx.DISTINCT() != null, longType );
		}
		else {
			return new CountFunctionSqmExpression(
					(SqmExpression) ctx.expression().accept( this ),
					ctx.DISTINCT() != null,
					longType
			);
		}
	}

	@Override
	public MaxFunctionSqmExpression visitMaxFunction(HqlParser.MaxFunctionContext ctx) {
		final SqmExpression expr = (SqmExpression) ctx.expression().accept( this );
		return new MaxFunctionSqmExpression(
				expr,
				ctx.DISTINCT() != null,
				expr.getExpressionType()
		);
	}

	@Override
	public MinFunctionSqmExpression visitMinFunction(HqlParser.MinFunctionContext ctx) {
		final SqmExpression expr = (SqmExpression) ctx.expression().accept( this );
		return new MinFunctionSqmExpression(
				expr,
				ctx.DISTINCT() != null,
				expr.getExpressionType()
		);
	}

	@Override
	public SubstringFunctionSqmExpression visitSubstringFunction(HqlParser.SubstringFunctionContext ctx) {
		final SqmExpression source = (SqmExpression) ctx.expression().accept( this );
		final SqmExpression start = (SqmExpression) ctx.substringFunctionStartArgument().accept( this );
		final SqmExpression length = ctx.substringFunctionLengthArgument() == null
				? null
				: (SqmExpression) ctx.substringFunctionLengthArgument().accept( this );
		return new SubstringFunctionSqmExpression( source.getExpressionType(), source, start, length );
	}

	@Override
	public SumFunctionSqmExpression visitSumFunction(HqlParser.SumFunctionContext ctx) {
		final SqmExpression expr = (SqmExpression) ctx.expression().accept( this );
		return new SumFunctionSqmExpression(
				expr,
				ctx.DISTINCT() != null,
				parsingContext.getConsumerContext().getDomainMetamodel().resolveSumFunctionType( expr.getExpressionType() )
		);
	}

	@Override
	public TrimFunctionSqmExpression visitTrimFunction(HqlParser.TrimFunctionContext ctx) {
		final SqmExpression source = (SqmExpression) ctx.expression().accept( this );
		return new TrimFunctionSqmExpression(
				source.getExpressionType(),
				visitTrimSpecification( ctx.trimSpecification() ),
				visitTrimCharacter( ctx.trimCharacter() ),
				source
		);
	}

	@Override
	public TrimFunctionSqmExpression.Specification visitTrimSpecification(HqlParser.TrimSpecificationContext ctx) {
		if ( ctx.LEADING() != null ) {
			return TrimFunctionSqmExpression.Specification.LEADING;
		}
		else if ( ctx.TRAILING() != null ) {
			return TrimFunctionSqmExpression.Specification.TRAILING;
		}

		// JPA says the default is BOTH
		return TrimFunctionSqmExpression.Specification.BOTH;
	}

	@Override
	public LiteralCharacterSqmExpression visitTrimCharacter(HqlParser.TrimCharacterContext ctx) {
		if ( ctx.CHARACTER_LITERAL() != null ) {
			final String trimCharText = ctx.CHARACTER_LITERAL().getText();
			if ( trimCharText.length() != 1 ) {
				throw new SemanticException( "Expecting [trim character] for TRIM function to be  single character, found : " + trimCharText );
			}
			return new LiteralCharacterSqmExpression( trimCharText.charAt( 0 ) );
		}
		if ( ctx.STRING_LITERAL() != null ) {
			final String trimCharText = ctx.STRING_LITERAL().getText();
			if ( trimCharText.length() != 1 ) {
				throw new SemanticException( "Expecting [trim character] for TRIM function to be  single character, found : " + trimCharText );
			}
			return new LiteralCharacterSqmExpression( trimCharText.charAt( 0 ) );
		}

		// JPA says space is the default
		return new LiteralCharacterSqmExpression( ' ' );
	}

	@Override
	public UpperFunctionSqmExpression visitUpperFunction(HqlParser.UpperFunctionContext ctx) {
		final SqmExpression expression = (SqmExpression) ctx.expression().accept( this );
		return new UpperFunctionSqmExpression(
				expression.getExpressionType(),
				expression
		);
	}

	@Override
	public LowerFunctionSqmExpression visitLowerFunction(HqlParser.LowerFunctionContext ctx) {
		final SqmExpression expression = (SqmExpression) ctx.expression().accept( this );
		return new LowerFunctionSqmExpression(
				expression.getExpressionType(),
				expression
		);
	}

	@Override
	public CollectionSizeSqmExpression visitCollectionSizeFunction(HqlParser.CollectionSizeFunctionContext ctx) {
		final PluralAttributeBinding attributeBinding = asPluralAttribute( (DomainReferenceBinding) ctx.path().accept( this ) );
		return new CollectionSizeSqmExpression( attributeBinding );
	}

	@Override
	public CollectionIndexSqmExpression visitCollectionIndexFunction(HqlParser.CollectionIndexFunctionContext ctx) {
		final String alias = ctx.identifier().getText();

		final DomainReferenceBinding binding = currentQuerySpecProcessingState.getFromElementBuilder().getAliasRegistry().findFromElementByAlias( alias );

		PluralAttributeBinding attributeBinding = null;
		if ( PluralAttributeBinding.class.isInstance( binding ) ) {
			attributeBinding = (PluralAttributeBinding) binding;
		}
		else if ( SqmAttributeJoin.class.isInstance( binding ) ) {
			final SqmAttributeJoin join = (SqmAttributeJoin) binding;
			if ( PluralAttributeBinding.class.isInstance( join.getAttributeBinding() ) ) {
				attributeBinding = (PluralAttributeBinding) join.getAttributeBinding();
			}
		}


		if ( attributeBinding == null ) {
			// most likely a semantic problem, but not necessarily...
			throw new ParsingException( "Could not resolve identification variable [" + alias + "] as plural-attribute" );
		}

		if ( !isIndexedPluralAttribute( attributeBinding ) ) {
			throw new SemanticException(
					"index() function can only be applied to identification variables which resolve to an " +
							"indexed collection (map,list); specified identification variable [" + alias +
							"] resolved to " + attributeBinding
			);
		}

		return new CollectionIndexSqmExpression( attributeBinding );
	}

	private boolean isIndexedPluralAttribute(PluralAttributeBinding attributeBinding) {
		return attributeBinding.getAttribute().getCollectionClassification() == CollectionClassification.MAP
				|| attributeBinding.getAttribute().getCollectionClassification() == CollectionClassification.LIST;
	}

	private boolean isList(PluralAttributeBinding attributeBinding) {
		return attributeBinding.getAttribute().getCollectionClassification() == CollectionClassification.LIST;
	}

	@Override
	public MaxElementSqmExpression visitMaxElementFunction(HqlParser.MaxElementFunctionContext ctx) {
		if ( parsingContext.getConsumerContext().useStrictJpaCompliance() ) {
			throw new StrictJpaComplianceViolation( StrictJpaComplianceViolation.Type.HQL_COLLECTION_FUNCTION );
		}

		return new MaxElementSqmExpression( asPluralAttribute( (DomainReferenceBinding) ctx.path().accept( this ) ) );
	}

	@Override
	public MinElementSqmExpression visitMinElementFunction(HqlParser.MinElementFunctionContext ctx) {
		if ( parsingContext.getConsumerContext().useStrictJpaCompliance() ) {
			throw new StrictJpaComplianceViolation( StrictJpaComplianceViolation.Type.HQL_COLLECTION_FUNCTION );
		}

		return new MinElementSqmExpression( asPluralAttribute( (DomainReferenceBinding) ctx.path().accept( this ) ) );
	}

	@Override
	public MaxIndexSqmExpression visitMaxIndexFunction(HqlParser.MaxIndexFunctionContext ctx) {
		if ( parsingContext.getConsumerContext().useStrictJpaCompliance() ) {
			throw new StrictJpaComplianceViolation( StrictJpaComplianceViolation.Type.HQL_COLLECTION_FUNCTION );
		}

		final PluralAttributeBinding attributeBinding = asPluralAttribute( (DomainReferenceBinding) ctx.path().accept( this ) );
		if (!isIndexedPluralAttribute( attributeBinding ) ) {
			throw new SemanticException(
					"maxindex() function can only be applied to path expressions which resolve to an " +
							"indexed collection (list,map); specified path [" + ctx.path().getText() +
							"] resolved to " + attributeBinding.getAttribute()
			);
		}

		return new MaxIndexSqmExpression( attributeBinding );
	}

	@Override
	public MinIndexSqmExpression visitMinIndexFunction(HqlParser.MinIndexFunctionContext ctx) {
		if ( parsingContext.getConsumerContext().useStrictJpaCompliance() ) {
			throw new StrictJpaComplianceViolation( StrictJpaComplianceViolation.Type.HQL_COLLECTION_FUNCTION );
		}

		final PluralAttributeBinding attributeBinding = asPluralAttribute( (DomainReferenceBinding) ctx.path().accept( this ) );
		if (!isIndexedPluralAttribute( attributeBinding ) ) {
			throw new SemanticException(
					"minindex() function can only be applied to path expressions which resolve to an " +
							"indexed collection (list,map); specified path [" + ctx.path().getText() +
							"] resolved to " + attributeBinding.getAttribute()
			);
		}

		return new MinIndexSqmExpression( attributeBinding );
	}

	@Override
	public SubQuerySqmExpression visitSubQueryExpression(HqlParser.SubQueryExpressionContext ctx) {
		final SqmQuerySpec querySpec = visitQuerySpec( ctx.querySpec() );
		return new SubQuerySqmExpression( querySpec, determineTypeDescriptor( querySpec.getSelectClause() ) );
	}

	private static DomainReference determineTypeDescriptor(SqmSelectClause selectClause) {
		if ( selectClause.getSelections().size() != 0 ) {
			return null;
		}

		final SqmSelection selection = selectClause.getSelections().get( 0 );
		return selection.getExpression().getExpressionType();
	}
}
