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

import org.hibernate.sqm.NotYetImplementedException;
import org.hibernate.sqm.StrictJpaComplianceViolation;
import org.hibernate.sqm.domain.SqmExpressableType;
import org.hibernate.sqm.domain.SqmExpressableTypeBasic;
import org.hibernate.sqm.domain.SqmExpressableTypeEntity;
import org.hibernate.sqm.domain.SqmExpressableTypeEntityPolymorphicEntity;
import org.hibernate.sqm.domain.SqmNavigableSource;
import org.hibernate.sqm.domain.SqmPluralAttribute;
import org.hibernate.sqm.domain.SqmPluralAttribute.CollectionClassification;
import org.hibernate.sqm.domain.SqmPluralAttributeElement;
import org.hibernate.sqm.domain.type.SqmDomainType;
import org.hibernate.sqm.domain.type.SqmDomainTypeBasic;
import org.hibernate.sqm.parser.LiteralNumberFormatException;
import org.hibernate.sqm.parser.ParsingException;
import org.hibernate.sqm.parser.SemanticException;
import org.hibernate.sqm.parser.UnknownEntityException;
import org.hibernate.sqm.parser.common.ImplicitAliasGenerator;
import org.hibernate.sqm.parser.common.NavigableBindingHelper;
import org.hibernate.sqm.parser.common.ParameterDeclarationContext;
import org.hibernate.sqm.parser.common.ParsingContext;
import org.hibernate.sqm.parser.common.QuerySpecProcessingState;
import org.hibernate.sqm.parser.common.QuerySpecProcessingStateDmlImpl;
import org.hibernate.sqm.parser.common.QuerySpecProcessingStateStandardImpl;
import org.hibernate.sqm.parser.common.Stack;
import org.hibernate.sqm.parser.hql.internal.antlr.HqlParser;
import org.hibernate.sqm.parser.hql.internal.antlr.HqlParserBaseVisitor;
import org.hibernate.sqm.parser.hql.internal.navigable.NavigableBindingResolver;
import org.hibernate.sqm.parser.hql.internal.navigable.PathHelper;
import org.hibernate.sqm.parser.hql.internal.navigable.PathResolverBasicImpl;
import org.hibernate.sqm.parser.hql.internal.navigable.PathResolverJoinAttributeImpl;
import org.hibernate.sqm.parser.hql.internal.navigable.PathResolverJoinPredicateImpl;
import org.hibernate.sqm.parser.hql.internal.navigable.PathResolverSelectClauseImpl;
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
import org.hibernate.sqm.query.expression.domain.CollectionElementBinding;
import org.hibernate.sqm.query.expression.domain.CollectionElementBindingBasic;
import org.hibernate.sqm.query.expression.domain.CollectionElementBindingEmbedded;
import org.hibernate.sqm.query.expression.domain.CollectionElementBindingEntity;
import org.hibernate.sqm.query.expression.domain.CollectionIndexBinding;
import org.hibernate.sqm.query.expression.domain.EntityBindingImpl;
import org.hibernate.sqm.query.expression.domain.IndexedElementBindingBasic;
import org.hibernate.sqm.query.expression.domain.IndexedElementBindingEmbedded;
import org.hibernate.sqm.query.expression.domain.IndexedElementBindingEntity;
import org.hibernate.sqm.query.expression.domain.MapEntryBinding;
import org.hibernate.sqm.query.expression.domain.MaxElementBindingBasic;
import org.hibernate.sqm.query.expression.domain.MaxIndexBinding;
import org.hibernate.sqm.query.expression.domain.MaxIndexBindingBasic;
import org.hibernate.sqm.query.expression.domain.MaxIndexBindingEmbedded;
import org.hibernate.sqm.query.expression.domain.MaxIndexBindingEntity;
import org.hibernate.sqm.query.expression.domain.MinElementBinding;
import org.hibernate.sqm.query.expression.domain.MinElementBindingBasic;
import org.hibernate.sqm.query.expression.domain.MinElementBindingEmbedded;
import org.hibernate.sqm.query.expression.domain.MinElementBindingEntity;
import org.hibernate.sqm.query.expression.domain.MinIndexBinding;
import org.hibernate.sqm.query.expression.domain.MinIndexBindingBasic;
import org.hibernate.sqm.query.expression.domain.MinIndexBindingEmbeddable;
import org.hibernate.sqm.query.expression.domain.MinIndexBindingEntity;
import org.hibernate.sqm.query.expression.domain.SqmAttributeBinding;
import org.hibernate.sqm.query.expression.domain.SqmEmbeddableTypedBinding;
import org.hibernate.sqm.query.expression.domain.SqmEntityTypeSqmExpression;
import org.hibernate.sqm.query.expression.domain.SqmEntityTypedBinding;
import org.hibernate.sqm.query.expression.domain.SqmNavigableBinding;
import org.hibernate.sqm.query.expression.domain.SqmNavigableResolutionContext;
import org.hibernate.sqm.query.expression.domain.SqmNavigableSourceBinding;
import org.hibernate.sqm.query.expression.domain.SqmPluralAttributeBinding;
import org.hibernate.sqm.query.expression.domain.SqmRestrictedCollectionElementBinding;
import org.hibernate.sqm.query.expression.domain.SqmSingularAttributeBinding;
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
import org.hibernate.sqm.query.from.SqmFromExporter;
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
import org.hibernate.sqm.query.paging.LimitOffsetClause;
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
import org.hibernate.sqm.query.predicate.RelationalPredicateOperator;
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
public class SemanticQueryBuilder extends HqlParserBaseVisitor implements SqmNavigableResolutionContext {
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

	private final Stack<NavigableBindingResolver> pathResolverStack = new Stack<>();
	private final Stack<ParameterDeclarationContext> parameterDeclarationContextStack = new Stack<>();
	private final Stack<QuerySpecProcessingState> querySpecProcessingStateStack = new Stack<>();

	private boolean inWhereClause;
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
		}
		finally {
			selectStatement.wrapUp();
		}

		return selectStatement;
	}

	@Override
	public SqmQuerySpec visitQuerySpec(HqlParser.QuerySpecContext ctx) {
		querySpecProcessingStateStack.push(
				new QuerySpecProcessingStateStandardImpl(
						parsingContext,
						querySpecProcessingStateStack.getCurrent()
				)
		);
		pathResolverStack.push( new PathResolverBasicImpl( querySpecProcessingStateStack.getCurrent() ) );
		try {
			// visit from-clause first!!!
			visitFromClause( ctx.fromClause() );

			final SqmSelectClause selectClause;
			if ( ctx.selectClause() != null ) {
				selectClause = visitSelectClause( ctx.selectClause() );
			}
			else {
				log.info( "Encountered implicit select clause which is a deprecated feature : " + ctx.getText() );
				selectClause = buildInferredSelectClause( querySpecProcessingStateStack.getCurrent().getFromClause() );
			}

			final SqmWhereClause whereClause;
			if ( ctx.whereClause() != null ) {
				whereClause = visitWhereClause( ctx.whereClause() );
			}
			else {
				whereClause = null;
			}

			final OrderByClause orderByClause;
			if ( ctx.orderByClause() != null ) {
				if ( parsingContext.getConsumerContext().useStrictJpaCompliance()
						&& querySpecProcessingStateStack.getCurrent().getContainingQueryState() != null ) {
					throw new StrictJpaComplianceViolation(
							StrictJpaComplianceViolation.Type.SUBQUERY_ORDER_BY
					);
				}

				pathResolverStack.push(
						new PathResolverBasicImpl(
								new OrderByResolutionContext(
										parsingContext,
										querySpecProcessingStateStack.getCurrent().getFromClause(),
										selectClause
								)
						)
				);
				try {
					orderByClause = visitOrderByClause( ctx.orderByClause() );
				}
				finally {
					pathResolverStack.pop();
				}
			}
			else {
				orderByClause = null;
			}

			final LimitOffsetClause limitOffsetClause;
			if ( ctx.limitClause() != null || ctx.offsetClause() != null ) {
				if ( parsingContext.getConsumerContext().useStrictJpaCompliance() ) {
					throw new StrictJpaComplianceViolation(
							StrictJpaComplianceViolation.Type.LIMIT_OFFSET_CLAUSE
					);
				}

				if ( querySpecProcessingStateStack.getCurrent().getContainingQueryState() != null
						&& orderByClause == null ) {
					throw new SemanticException( "limit and offset clause require an order-by clause" );
				}

				final SqmExpression limitExpression;
				if ( ctx.limitClause() != null ) {
					limitExpression = visitLimitClause( ctx.limitClause() );
				} else {
					limitExpression = null;
				}

				final SqmExpression offsetExpression;
				if ( ctx.offsetClause() != null ) {
					offsetExpression = visitOffsetClause( ctx.offsetClause() );
				} else {
					offsetExpression = null;
				}

				limitOffsetClause = new LimitOffsetClause( limitExpression, offsetExpression );
			}
			else {
				limitOffsetClause = null;
			}

			return new SqmQuerySpec( querySpecProcessingStateStack.getCurrent().getFromClause(), selectClause, whereClause, orderByClause, limitOffsetClause );
		}
		finally {
			pathResolverStack.pop();
			querySpecProcessingStateStack.pop();
		}
	}

	protected SqmSelectClause buildInferredSelectClause(SqmFromClause fromClause) {
		// for now, this is slightly different than the legacy behavior where
		// the root and each non-fetched-join was selected.  For now, here, we simply
		// select the root
		final SqmSelectClause selectClause = new SqmSelectClause( false );
		final SqmFrom root = fromClause.getFromElementSpaces().get( 0 ).getRoot();
		selectClause.addSelection( new SqmSelection( root.getBinding() ) );
		return selectClause;
	}

	@Override
	public SqmSelectClause visitSelectClause(HqlParser.SelectClauseContext ctx) {
		pathResolverStack.push( new PathResolverSelectClauseImpl( querySpecProcessingStateStack.getCurrent() ) );

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
		if ( selectExpression instanceof SqmPluralAttributeBinding ) {
			final SqmPluralAttributeBinding pluralAttributeBinding = (SqmPluralAttributeBinding) selectExpression;
			final SqmPluralAttributeElement elementReference = pluralAttributeBinding.getBoundNavigable().getElementReference();
			switch ( elementReference.getClassification() ) {
				case ANY: {
					throw new NotYetImplementedException(  );
				}
				case BASIC: {
					selectExpression = new CollectionElementBindingBasic( pluralAttributeBinding );
					break;
				}
				case EMBEDDABLE: {
					selectExpression = new CollectionElementBindingEmbedded( pluralAttributeBinding );
					break;
				}
				case ONE_TO_MANY:
				case MANY_TO_MANY: {
					selectExpression = new CollectionElementBindingEntity( pluralAttributeBinding );
					break;
				}
			}
		}

		final SqmSelection selection = new SqmSelection(
				selectExpression,
				interpretResultIdentifier( ctx.resultIdentifier() )
		);
		querySpecProcessingStateStack.getCurrent().getFromElementBuilder().getAliasRegistry().registerAlias( selection );
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
			dynamicInstantiation = SqmDynamicInstantiation.forMapInstantiation( parsingContext );
		}
		else if ( ctx.dynamicInstantiationTarget().LIST() != null ) {
			dynamicInstantiation = SqmDynamicInstantiation.forListInstantiation( parsingContext );
		}
		else {
			final String className = ctx.dynamicInstantiationTarget().dotIdentifierSequence().getText();
			try {
				final Class targetJavaType = parsingContext.getConsumerContext().classByName( className );
				dynamicInstantiation = SqmDynamicInstantiation.forClassInstantiation( targetJavaType, parsingContext );
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
	public SqmNavigableBinding visitJpaSelectObjectSyntax(HqlParser.JpaSelectObjectSyntaxContext ctx) {
		final String alias = ctx.identifier().getText();
		final SqmNavigableBinding binding = querySpecProcessingStateStack.getCurrent().getFromElementBuilder().getAliasRegistry().findFromElementByAlias( alias );
		if ( binding == null ) {
			throw new SemanticException( "Unable to resolve alias [" +  alias + "] in selection [" + ctx.getText() + "]" );
		}
		return binding;
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

	@Override
	public SqmExpression visitLimitClause(HqlParser.LimitClauseContext ctx) {
		return (SqmExpression) ctx.parameterOrNumberLiteral().accept( this );
	}

	@Override
	public SqmExpression visitOffsetClause(HqlParser.OffsetClauseContext ctx) {
		return (SqmExpression) ctx.parameterOrNumberLiteral().accept( this );
	}

	@Override
	public SqmExpression visitParameterOrNumberLiteral(HqlParser.ParameterOrNumberLiteralContext ctx) {
		if ( ctx.INTEGER_LITERAL() != null ) {
			return integerLiteral( ctx.INTEGER_LITERAL().getText() );
		}
		if ( ctx.parameter() != null ) {
			return (SqmExpression) ctx.parameter().accept( this );
		}

		return null;
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
		querySpecProcessingStateStack.push( new QuerySpecProcessingStateDmlImpl( parsingContext ) );
		try {
			final SqmRoot root = resolveDmlRootEntityReference( ctx.mainEntityPersisterReference() );
			final SqmDeleteStatementImpl deleteStatement = new SqmDeleteStatementImpl( root );

			parameterCollector = deleteStatement;

			pathResolverStack.push( new PathResolverBasicImpl( querySpecProcessingStateStack.getCurrent() ) );
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
			querySpecProcessingStateStack.pop();
		}
	}

	protected SqmRoot resolveDmlRootEntityReference(HqlParser.MainEntityPersisterReferenceContext rootEntityContext) {
		final String entityName = rootEntityContext.dotIdentifierSequence().getText();
		final SqmExpressableTypeEntity entityReference = resolveEntityReference( entityName );
		if ( entityReference == null ) {
			throw new UnknownEntityException( "Could not resolve entity name [" + entityName + "] as DML target", entityName );
		}

		String alias = interpretIdentificationVariable( rootEntityContext.identificationVariableDef() );
		if ( alias == null ) {
			alias = parsingContext.getImplicitAliasGenerator().buildUniqueImplicitAlias();
			log.debugf(
					"Generated implicit alias [%s] for DML root entity reference [%s]",
					alias,
					entityReference.getEntityName()
			);
		}
		final SqmRoot root = new SqmRoot( null, parsingContext.makeUniqueIdentifier(), alias, entityReference );
		parsingContext.registerFromElementByUniqueId( root );
		querySpecProcessingStateStack.getCurrent().getFromElementBuilder().getAliasRegistry().registerAlias( root.getBinding() );
		querySpecProcessingStateStack.getCurrent().getFromClause().getFromElementSpaces().get( 0 ).setRoot( root );
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
		querySpecProcessingStateStack.push( new QuerySpecProcessingStateDmlImpl( parsingContext ) );
		try {
			final SqmRoot root = resolveDmlRootEntityReference( ctx.mainEntityPersisterReference() );
			final SqmUpdateStatementImpl updateStatement = new SqmUpdateStatementImpl( root );

			pathResolverStack.push( new PathResolverBasicImpl( querySpecProcessingStateStack.getCurrent() ) );
			parameterCollector = updateStatement;
			try {
				updateStatement.getWhereClause().setPredicate(
						(SqmPredicate) ctx.whereClause().predicate().accept( this )
				);

				for ( HqlParser.AssignmentContext assignmentContext : ctx.setClause().assignment() ) {
					final SqmSingularAttributeBinding stateField = (SqmSingularAttributeBinding) pathResolverStack.getCurrent().resolvePath(
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
			querySpecProcessingStateStack.pop();
		}
	}

	private String[] splitPathParts(HqlParser.DotIdentifierSequenceContext path) {
		final String pathText = path.getText();
		log.debugf( "Splitting dotIdentifierSequence into path parts : %s", pathText );
		return PathHelper.split( pathText );
	}

	@Override
	public SqmInsertSelectStatement visitInsertStatement(HqlParser.InsertStatementContext ctx) {
		querySpecProcessingStateStack.push( new QuerySpecProcessingStateDmlImpl( parsingContext ) );
		try {
			final String entityName = ctx.insertSpec().intoSpec().dotIdentifierSequence().getText();
			final SqmExpressableTypeEntity entityReference = resolveEntityReference( entityName );
			if ( entityReference == null ) {
				throw new UnknownEntityException( "Could not resolve entity name [" + entityName + "] as INSERT target", entityName );
			}

			String alias = parsingContext.getImplicitAliasGenerator().buildUniqueImplicitAlias();
			log.debugf(
					"Generated implicit alias [%s] for INSERT target [%s]",
					alias,
					entityReference.getEntityName()
			);

			SqmRoot root = new SqmRoot( null, parsingContext.makeUniqueIdentifier(), alias, entityReference );
			parsingContext.registerFromElementByUniqueId( root );
			querySpecProcessingStateStack.getCurrent().getFromElementBuilder().getAliasRegistry().registerAlias( root.getBinding() );
			querySpecProcessingStateStack.getCurrent().getFromClause().getFromElementSpaces().get( 0 ).setRoot( root );

			// for now we only support the INSERT-SELECT form
			final SqmInsertSelectStatementImpl insertStatement = new SqmInsertSelectStatementImpl( root );
			parameterCollector = insertStatement;
			pathResolverStack.push( new PathResolverBasicImpl( querySpecProcessingStateStack.getCurrent() ) );

			try {
				insertStatement.setSelectQuery( visitQuerySpec( ctx.querySpec() ) );

				for ( HqlParser.DotIdentifierSequenceContext stateFieldCtx : ctx.insertSpec().targetFieldsSpec().dotIdentifierSequence() ) {
					final SqmSingularAttributeBinding stateField = (SqmSingularAttributeBinding) pathResolverStack.getCurrent().resolvePath(
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
			querySpecProcessingStateStack.pop();
		}
	}

	private FromElementSpace currentFromElementSpace;

	@Override
	public Object visitFromElementSpace(HqlParser.FromElementSpaceContext ctx) {
		currentFromElementSpace = querySpecProcessingStateStack.getCurrent().getFromClause().makeFromElementSpace();

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
		final String entityName = ctx.mainEntityPersisterReference().dotIdentifierSequence().getText();
		final SqmExpressableTypeEntity entityReference = resolveEntityReference( entityName );
		if ( entityReference == null ) {
			throw new UnknownEntityException( "Could not resolve entity name [" + entityName + "] used as root", entityName );
		}

		if ( SqmExpressableTypeEntityPolymorphicEntity.class.isInstance( entityReference ) ) {
			if ( parsingContext.getConsumerContext().useStrictJpaCompliance() ) {
				throw new StrictJpaComplianceViolation(
						"Encountered unmapped polymorphic reference [" + entityReference.getEntityName()
								+ "], but strict JPQL compliance was requested",
						StrictJpaComplianceViolation.Type.UNMAPPED_POLYMORPHISM
				);
			}

			// todo : disallow in subqueries as well
		}

		return querySpecProcessingStateStack.getCurrent().getFromElementBuilder().makeRootEntityFromElement(
				currentFromElementSpace,
				entityReference,
				interpretIdentificationVariable( ctx.mainEntityPersisterReference().identificationVariableDef() )
		);
	}

	private SqmExpressableTypeEntity resolveEntityReference(String entityName) {
		log.debugf( "Attempting to resolve path [%s] as entity reference...", entityName );
		SqmExpressableTypeEntity reference = null;
		try {
			reference = parsingContext.getConsumerContext()
					.getDomainMetamodel()
					.resolveEntityReference( entityName );
		}
		catch (Exception ignore) {
		}

		return reference;
	}

	@Override
	public SqmCrossJoin visitCrossJoin(HqlParser.CrossJoinContext ctx) {
		final String entityName = ctx.mainEntityPersisterReference().dotIdentifierSequence().getText();
		final SqmExpressableTypeEntity entityReference = resolveEntityReference( entityName );
		if ( entityReference == null ) {
			throw new UnknownEntityException( "Could not resolve entity name [" + entityName + "] used as CROSS JOIN target", entityName );
		}

		if ( SqmExpressableTypeEntityPolymorphicEntity.class.isInstance( entityReference ) ) {
			throw new SemanticException(
					"Unmapped polymorphic references are only valid as sqm root, not in cross join : " +
							entityReference.getEntityName()
			);
		}

		return querySpecProcessingStateStack.getCurrent().getFromElementBuilder().makeCrossJoinedFromElement(
				currentFromElementSpace,
				parsingContext.makeUniqueIdentifier(),
				entityReference,
				interpretIdentificationVariable( ctx.mainEntityPersisterReference().identificationVariableDef() )
		);
	}

	@Override
	public SqmQualifiedJoin visitJpaCollectionJoin(HqlParser.JpaCollectionJoinContext ctx) {
		pathResolverStack.push(
				new PathResolverJoinAttributeImpl(
						querySpecProcessingStateStack.getCurrent(),
						currentFromElementSpace,
						JoinType.INNER,
						interpretIdentificationVariable( ctx.identificationVariableDef() ),
						false
				)
		);

		try {
			SqmPluralAttributeBinding attributeBinding = asPluralAttribute( (SqmNavigableBinding) ctx.path().accept( this ) );
			return attributeBinding.getExportedFromElement();
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
				throw new SemanticException( "RIGHT OUTER joins are not yet supported : " + ctx.getText() );
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
						querySpecProcessingStateStack.getCurrent(),
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
			final Object joinRhsResolution = ctx.qualifiedJoinRhs().path().accept( this );
			final SqmNavigableBinding navigableBinding;
			if ( joinRhsResolution instanceof EntityTypeLiteralSqmExpression ) {
				// convert the EntityTypeLiteralSqmExpression into an EntityBinding
				final EntityTypeLiteralSqmExpression entityReference = (EntityTypeLiteralSqmExpression) joinRhsResolution;
				navigableBinding = new EntityBindingImpl( entityReference.getExpressionType() );
			}
			else {
				navigableBinding = (SqmNavigableBinding) joinRhsResolution;
			}

			if ( navigableBinding instanceof SqmAttributeBinding ) {
				resolveAttributeJoinIfNot( (SqmAttributeBinding) navigableBinding, identificationVariable );
			}
			else if ( navigableBinding instanceof CollectionElementBinding ) {
				resolveCollectionElementJoinIfNot( (CollectionElementBinding) navigableBinding, identificationVariable );
			}
			else if ( navigableBinding instanceof CollectionIndexBinding ) {
				resolveCollectionIndexJoinIfNot( (CollectionIndexBinding) navigableBinding, identificationVariable );
			}
			else if ( navigableBinding instanceof SqmEntityTypedBinding ) {
				// should be an EntityBindingImpl
				assert navigableBinding instanceof EntityBindingImpl;

				final SqmEntityTypedBinding entityBinding = (SqmEntityTypedBinding) navigableBinding;
				if ( entityBinding.getExportedFromElement() == null ) {
					entityBinding.injectExportedFromElement(
							querySpecProcessingStateStack.getCurrent().getFromElementBuilder().buildEntityJoin(
									currentFromElementSpace,
									identificationVariable,
									entityBinding.getBoundNavigable(),
									JoinType.INNER
							)
					);
				}
			}
			else if ( navigableBinding instanceof SqmEmbeddableTypedBinding ) {
				final SqmEmbeddableTypedBinding compositeBinding = (SqmEmbeddableTypedBinding) navigableBinding;
				if ( compositeBinding.getExportedFromElement() == null ) {
					// todo : can we create a SqmFrom element specific to a CompositeBinding?
					throw new NotYetImplementedException( "Support for SqmFrom generation specific to CompositeBinding not yet implemented" );
				}
			}
			else {
				throw new ParsingException( "Unexpected qualifiedJoin.path resolution type : " + navigableBinding );
			}

			joinedFromElement = (SqmQualifiedJoin) ( (SqmFromExporter) navigableBinding ).getExportedFromElement();
			currentJoinRhs = joinedFromElement;

			if ( parsingContext.getConsumerContext().useStrictJpaCompliance() ) {
				if ( !ImplicitAliasGenerator.isImplicitAlias( joinedFromElement.getIdentificationVariable() ) ) {
					if ( SqmSingularAttributeBinding.class.isInstance( joinedFromElement.getBinding() )
							&& SqmFromExporter.class.isInstance( joinedFromElement.getBinding() ) ) {
						final SqmAttributeJoin attributeJoin = (SqmAttributeJoin) joinedFromElement;
						if ( attributeJoin.isFetched() ) {
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

	private void resolveCollectionElementJoinIfNot(CollectionElementBinding binding, String identificationVariable) {
		throw new NotYetImplementedException(  );
	}

	private void resolveCollectionIndexJoinIfNot(CollectionIndexBinding binding, String identificationVariable) {
		throw new NotYetImplementedException(  );
	}

	private SqmQualifiedJoin currentJoinRhs;

	@Override
	public SqmPredicate visitQualifiedJoinPredicate(HqlParser.QualifiedJoinPredicateContext ctx) {
		if ( currentJoinRhs == null ) {
			throw new ParsingException( "Expecting join RHS to be set" );
		}

		pathResolverStack.push(
				new PathResolverJoinPredicateImpl( querySpecProcessingStateStack.getCurrent(), currentJoinRhs )
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
				(SqmPluralAttributeBinding) ctx.expression().accept( this ),
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

		return new RelationalSqmPredicate( RelationalPredicateOperator.EQUAL, lhs, rhs );
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

		return new RelationalSqmPredicate( RelationalPredicateOperator.NOT_EQUAL, lhs, rhs );
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

		return new RelationalSqmPredicate( RelationalPredicateOperator.GREATER_THAN, lhs, rhs );
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

		return new RelationalSqmPredicate( RelationalPredicateOperator.GREATER_THAN_OR_EQUAL, lhs, rhs );
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

		return new RelationalSqmPredicate( RelationalPredicateOperator.LESS_THAN, lhs, rhs );
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

		return new RelationalSqmPredicate( RelationalPredicateOperator.LESS_THAN_OR_EQUAL, lhs, rhs );
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
		final SqmNavigableBinding pathResolution = (SqmNavigableBinding) ctx.path().accept( this );

		if ( !SqmSingularAttributeBinding.class.isInstance( pathResolution ) ) {
			throw new SemanticException( "Could not resolve path [" + ctx.path().getText() + "] as an attribute reference" );
		}

		final SqmSingularAttributeBinding attributeBinding = (SqmSingularAttributeBinding) pathResolution;
		if ( !SqmPluralAttribute.class.isInstance( attributeBinding.getBoundNavigable() ) ) {
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
			final SqmNavigableBinding binding = (SqmNavigableBinding) ctx.entityTypeReference().path().accept( this );
			validateBindingAsEntityTypeExpression( binding );
			return new SqmEntityTypeSqmExpression( binding );
		}

		throw new ParsingException( "Could not interpret grammar context as 'entity type' expression : " + ctx.getText() );
	}

	private void validateBindingAsEntityTypeExpression(SqmNavigableBinding binding) {
		if ( binding instanceof SqmEntityTypedBinding ) {
			// its ok
			return;
		}

		throw new SemanticException(
				"Path used in TYPE() resolved to a non-EntityBinding : " + binding
		);
	}

	@Override
	public SqmExpression visitSimplePath(HqlParser.SimplePathContext ctx) {
		// SimplePath might represent any number of things
		final SqmNavigableBinding binding = pathAsNavigableBinding( splitPathParts( ctx.dotIdentifierSequence() ) );
		if ( binding != null ) {
			return binding;
		}

		final String pathText = ctx.getText();

		final SqmExpressableTypeEntity entityReference = resolveEntityReference( pathText );
		if ( entityReference != null ) {
			// todo : how this should be handled depends on the parent context...
			// 		inside a FROM-CLAUSE (although be careful of treats) -> EntityBindingImpl
			//		otherwise -> EntityTypeLiteralSqmExpression
			//
			// an example of the warning about treats...
			//		from Person p cross join (treat Person as Person)...

			return new EntityTypeLiteralSqmExpression( entityReference );
//			return new EntityBindingImpl( entityReference );
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

	protected SqmNavigableBinding pathAsNavigableBinding(String[] pathParts) {
		return pathResolverStack.getCurrent().resolvePath( pathParts );
	}

	@Override
	public MapEntryBinding visitMapEntryPath(HqlParser.MapEntryPathContext ctx) {
		if ( inWhereClause ) {
			throw new SemanticException(
					"entry() function may only be used in SELECT clauses; specified "
							+ "path [" + ctx.pathAsMap().path().getText() + "] is used in WHERE clause" );
		}

		final SqmPluralAttributeBinding pathResolution = asMap( (SqmNavigableBinding) ctx.pathAsMap().path().accept( this ) );
		resolveAttributeJoinIfNot( pathResolution );
		return new MapEntryBinding( pathResolution );
	}

	private void resolveAttributeJoinIfNot(SqmAttributeBinding binding) {
		resolveAttributeJoinIfNot( binding, null );
	}

	private void resolveAttributeJoinIfNot(SqmAttributeBinding attributeBinding, String alias) {
		if ( attributeBinding.getExportedFromElement() != null ) {
			return;
		}

		attributeBinding.injectExportedFromElement(
				querySpecProcessingStateStack.getCurrent().getFromElementBuilder().buildAttributeJoin(
						attributeBinding,
						alias,
						null,
						JoinType.INNER,
						false,
						true
				)
		);
	}

	@Override
	public SqmNavigableBinding visitIndexedPath(HqlParser.IndexedPathContext ctx) {
		final Object pathResolution = ctx.path().accept( this );
		if ( !SqmPluralAttributeBinding.class.isInstance( pathResolution ) ) {
			// assume it is a semantic problem...
			throw new SemanticException(
					"Expecting reference to a plural-attribute, but path [" + ctx.path().getText() +
							"] resolved to : " + pathResolution
			);
		}

		final SqmPluralAttributeBinding attributeBinding = (SqmPluralAttributeBinding) pathResolution;
		if ( !SqmPluralAttribute.class.isInstance( attributeBinding.getBoundNavigable() )
				|| attributeBinding.getBoundNavigable().getIndexReference() == null ) {
			throw new SemanticException(
					"Index operator only valid for indexed collections (maps, lists, arrays) : " +
							attributeBinding.getBoundNavigable()
			);
		}

		final SqmPluralAttribute attRef = attributeBinding.getBoundNavigable();
		final SqmPluralAttributeElement elementReference = attributeBinding.getBoundNavigable().getElementReference();

		final SqmExpression indexExpression = (SqmExpression) ctx.expression().accept( this );

		// todo : would be nice to validate the index's type against the Collection-index's type
		assertAreSame( attributeBinding.getBoundNavigable().getIndexReference().getExportedDomainType(), indexExpression.getExportedDomainType() );

		// determine the kind of IndexedElementBinding to create
		final SqmRestrictedCollectionElementBinding indexedReference;
		switch ( elementReference.getClassification() ) {
			case ANY: {
				throw new NotYetImplementedException(  );
			}
			case BASIC: {
				indexedReference = new IndexedElementBindingBasic( attributeBinding, indexExpression );
				break;
			}
			case EMBEDDABLE: {
				indexedReference = new IndexedElementBindingEmbedded( attributeBinding, indexExpression );
				break;
			}
			case ONE_TO_MANY:
			case MANY_TO_MANY: {
				indexedReference = new IndexedElementBindingEntity(
						attributeBinding,
						indexExpression
				);
				break;
			}
			default: {
				throw new UnsupportedOperationException(
						"Unrecognized plural-attribute element classification : " +
								elementReference.getClassification().name()
				);
			}
		}

		if ( ctx.pathTerminal() == null ) {
			return indexedReference;
		}

		// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// otherwise, we have a dereference of the pathRoot (as a pathTerminal)

		// the binding would additionally need to be an AttributeBindingSource
		// and expose a Bindable

		if ( ! SqmNavigableSource.class.isInstance( elementReference ) ) {
			throw new SemanticException(
					String.format(
							Locale.ROOT,
							"Plural attribute elements [%s.%s - as resolved from %s] cannot be dereferenced - [%s]",
							attributeBinding.getSourceBinding().asLoggableText(),
							attributeBinding.getBoundNavigable().getAttributeName(),
							ctx.path().getText(),
							attRef.getElementReference().getClassification().name()
					)
			);
		}

		return pathResolverStack.getCurrent().resolvePath(
				(SqmNavigableSourceBinding) indexedReference,
				PathHelper.split( ctx.pathTerminal().getText() )
		);
	}

	private void assertAreSame(SqmDomainType expected, SqmDomainType actual) {
		if ( !expected.equals( actual ) ) {
			log.debugf( "Expected domain type [%s] but found [%s]", expected, actual  );
		}
	}

	@Override
	public SqmNavigableBinding visitCompoundPath(HqlParser.CompoundPathContext ctx) {
		SqmNavigableBinding rootPath = (SqmNavigableBinding) ctx.pathRoot().accept( this );
		if ( ctx.pathTerminal() == null ) {
			// can happen for non-dereferenced KEY() and VALUE() references...
			return rootPath;

			// todo : a better solution is to better structure the grammar rules.
		}

		final SqmNavigableSourceBinding root = (SqmNavigableSourceBinding) rootPath ;

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
	public SqmNavigableBinding visitMapKeyPathRoot(HqlParser.MapKeyPathRootContext ctx) {
		final SqmPluralAttributeBinding pathResolution = visitPathAsMap( ctx.pathAsMap() );
		resolveAttributeJoinIfNot( pathResolution );
		return NavigableBindingHelper.createCollectionIndexBinding( pathResolution, pathResolution.getBoundNavigable().getIndexReference() );
	}

	private SqmPluralAttributeBinding asMap(SqmNavigableBinding binding) {
		SqmPluralAttributeBinding attributeBinding = asPluralAttribute( binding );
		if ( attributeBinding.getBoundNavigable().getCollectionClassification() != CollectionClassification.MAP ) {
			throw new SemanticException( "Expecting persistent Map reference, but found : " + binding );
		}

		return attributeBinding;
	}

	@Override
	public CollectionElementBinding visitCollectionValuePathRoot(HqlParser.CollectionValuePathRootContext ctx) {
		final SqmPluralAttributeBinding attributeBinding = asPluralAttribute( (SqmNavigableBinding) ctx.collectionReference().accept( this ) );

		if ( parsingContext.getConsumerContext().useStrictJpaCompliance() ) {
			if ( attributeBinding.getBoundNavigable().getCollectionClassification() != CollectionClassification.MAP ) {
				throw new StrictJpaComplianceViolation(
						"Encountered application of value() function to path expression which does not " +
								"resolve to a persistent Map, but strict JPQL compliance was requested. specified "
								+ "path [" + ctx.collectionReference().path().getText() + "] resolved to " + attributeBinding,
						StrictJpaComplianceViolation.Type.VALUE_FUNCTION_ON_NON_MAP
				);
			}
		}

		resolveAttributeJoinIfNot( attributeBinding );

		return NavigableBindingHelper.createCollectionElementBinding( attributeBinding, attributeBinding.getBoundNavigable().getElementReference() );
	}

	@SuppressWarnings("RedundantIfStatement")
	private SqmPluralAttributeBinding asPluralAttribute(SqmNavigableBinding attributeBinding) {
		if ( !SqmPluralAttribute.class.isInstance( attributeBinding.getBoundNavigable() ) ) {
			throw new SemanticException( "Expecting plural-attribute, but found : " + attributeBinding );
		}

		return (SqmPluralAttributeBinding) attributeBinding;
	}

	@Override
	public SqmPluralAttributeBinding visitCollectionReference(HqlParser.CollectionReferenceContext ctx) {
		return asPluralAttribute( (SqmNavigableBinding) ctx.path().accept( this ) );
	}

	@Override
	public SqmPluralAttributeBinding visitPathAsMap(HqlParser.PathAsMapContext ctx) {
		final SqmNavigableBinding pathResolution = (SqmNavigableBinding) ctx.path().accept( this );
		return asMap( pathResolution );
	}

	@Override
	public SqmNavigableBinding visitTreatedPathRoot(HqlParser.TreatedPathRootContext ctx) {
		final String treatAsName = ctx.dotIdentifierSequence().get( 1 ).getText();
		final SqmExpressableTypeEntity treatAsTypeDescriptor = resolveEntityReference( treatAsName );
		if ( treatAsTypeDescriptor == null ) {
			throw new UnknownEntityException( "Could not resolve entity name [" + treatAsName + "] as TREAT target", treatAsName );
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
						(SqmExpressableTypeBasic) firstOperand.getExpressionType(),
						(SqmExpressableTypeBasic) secondOperand.getExpressionType(),
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
						(SqmDomainTypeBasic) firstOperand.getExpressionType(),
						(SqmDomainTypeBasic) secondOperand.getExpressionType(),
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
						(SqmDomainTypeBasic) firstOperand.getExpressionType(),
						(SqmDomainTypeBasic) secondOperand.getExpressionType(),
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
						(SqmDomainTypeBasic) firstOperand.getExpressionType(),
						(SqmDomainTypeBasic) secondOperand.getExpressionType(),
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
						(SqmDomainTypeBasic) firstOperand.getExpressionType(),
						(SqmDomainTypeBasic) secondOperand.getExpressionType(),
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
					(SqmExpression) simpleCaseWhen.expression( 1 ).accept( this )
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
		final SqmExpressableTypeBasic expressionType = resolveExpressableTypeBasic( Boolean.class );
		return value
				? new LiteralTrueSqmExpression( expressionType )
				: new LiteralFalseSqmExpression( expressionType );
	}

	private LiteralCharacterSqmExpression characterLiteral(String text) {
		if ( text.length() > 1 ) {
			// todo : or just treat it as a String literal?
			throw new ParsingException( "Value for CHARACTER_LITERAL token was more than 1 character" );
		}
		return new LiteralCharacterSqmExpression(
				text.charAt( 0 ),
				resolveExpressableTypeBasic( Character.class )
		);
	}

	private LiteralSqmExpression stringLiteral(String text) {
		return new LiteralStringSqmExpression(
				text,
				resolveExpressableTypeBasic( String.class )
		);
	}

	protected LiteralIntegerSqmExpression integerLiteral(String text) {
		try {
			final Integer value = Integer.valueOf( text );
			return new LiteralIntegerSqmExpression(
					value,
					resolveExpressableTypeBasic( Integer.class )
			);
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
			return new LiteralLongSqmExpression(
					value,
					resolveExpressableTypeBasic( Long.class )
			);
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
			return new LiteralBigIntegerSqmExpression(
					new BigInteger( text ),
					resolveExpressableTypeBasic( BigInteger.class  )
			);
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
			return new LiteralFloatSqmExpression(
					Float.valueOf( text ),
					resolveExpressableTypeBasic( Float.class )
			);
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
			return new LiteralDoubleSqmExpression(
					Double.valueOf( text ),
					resolveExpressableTypeBasic( Double.class )
			);
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
			return new LiteralBigDecimalSqmExpression(
					new BigDecimal( text ),
					resolveExpressableTypeBasic( BigDecimal.class )
			);
		}
		catch (NumberFormatException e) {
			throw new LiteralNumberFormatException(
					"Unable to convert sqm literal [" + originalText + "] to BigDecimal",
					e
			);
		}
	}

	private SqmExpressableTypeBasic resolveExpressableTypeBasic(Class javaType) {
		return parsingContext.getConsumerContext().getDomainMetamodel().resolveBasicType( javaType );
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
		if ( ctx.INTEGER_LITERAL() == null ) {
			throw new SemanticException( "Encountered positional parameter which did not declare position (? instead of, e.g., ?1)" );
		}
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
				(SqmDomainTypeBasic) expr.getExpressionType()
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

		return new ConcatFunctionSqmExpression( (SqmDomainTypeBasic) arguments.get( 0 ).getExpressionType(), arguments );
	}

	@Override
	public AggregateFunctionSqmExpression visitCountFunction(HqlParser.CountFunctionContext ctx) {
		final SqmExpressableTypeBasic longType = resolveExpressableTypeBasic( Long.class );
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
				(SqmDomainTypeBasic) expr.getExpressionType()
		);
	}

	@Override
	public MinFunctionSqmExpression visitMinFunction(HqlParser.MinFunctionContext ctx) {
		final SqmExpression expr = (SqmExpression) ctx.expression().accept( this );
		return new MinFunctionSqmExpression(
				expr,
				ctx.DISTINCT() != null,
				(SqmDomainTypeBasic) expr.getExpressionType()
		);
	}

	@Override
	public SubstringFunctionSqmExpression visitSubstringFunction(HqlParser.SubstringFunctionContext ctx) {
		final SqmExpression source = (SqmExpression) ctx.expression().accept( this );
		final SqmExpression start = (SqmExpression) ctx.substringFunctionStartArgument().accept( this );
		final SqmExpression length = ctx.substringFunctionLengthArgument() == null
				? null
				: (SqmExpression) ctx.substringFunctionLengthArgument().accept( this );
		return new SubstringFunctionSqmExpression( (SqmDomainTypeBasic) source.getExpressionType(), source, start, length );
	}

	@Override
	public SumFunctionSqmExpression visitSumFunction(HqlParser.SumFunctionContext ctx) {
		final SqmExpression expr = (SqmExpression) ctx.expression().accept( this );
		return new SumFunctionSqmExpression(
				expr,
				ctx.DISTINCT() != null,
				parsingContext.getConsumerContext()
						.getDomainMetamodel()
						.resolveSumFunctionType( (SqmExpressableTypeBasic) expr.getExpressionType() )
		);
	}

	@Override
	public TrimFunctionSqmExpression visitTrimFunction(HqlParser.TrimFunctionContext ctx) {
		final SqmExpression source = (SqmExpression) ctx.expression().accept( this );
		return new TrimFunctionSqmExpression(
				(SqmDomainTypeBasic) source.getExpressionType(),
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
			return new LiteralCharacterSqmExpression(
					trimCharText.charAt( 0 ),
					resolveExpressableTypeBasic( Character.class )
			);
		}
		if ( ctx.STRING_LITERAL() != null ) {
			final String trimCharText = ctx.STRING_LITERAL().getText();
			if ( trimCharText.length() != 1 ) {
				throw new SemanticException( "Expecting [trim character] for TRIM function to be  single character, found : " + trimCharText );
			}
			return new LiteralCharacterSqmExpression(
					trimCharText.charAt( 0 ),
					resolveExpressableTypeBasic( Character.class ));
		}

		// JPA says space is the default
		return new LiteralCharacterSqmExpression(
				' ',
				resolveExpressableTypeBasic( Character.class )
		);
	}

	@Override
	public UpperFunctionSqmExpression visitUpperFunction(HqlParser.UpperFunctionContext ctx) {
		final SqmExpression expression = (SqmExpression) ctx.expression().accept( this );
		return new UpperFunctionSqmExpression(
				(SqmDomainTypeBasic) expression.getExpressionType(),
				expression
		);
	}

	@Override
	public LowerFunctionSqmExpression visitLowerFunction(HqlParser.LowerFunctionContext ctx) {
		final SqmExpression expression = (SqmExpression) ctx.expression().accept( this );
		return new LowerFunctionSqmExpression(
				(SqmDomainTypeBasic) expression.getExpressionType(),
				expression
		);
	}

	@Override
	public CollectionSizeSqmExpression visitCollectionSizeFunction(HqlParser.CollectionSizeFunctionContext ctx) {
		final SqmPluralAttributeBinding attributeBinding = asPluralAttribute( (SqmNavigableBinding) ctx.path().accept( this ) );
		return new CollectionSizeSqmExpression(
				attributeBinding,
				resolveExpressableTypeBasic( Integer.class )
		);
	}

	@Override
	public CollectionIndexBinding visitCollectionIndexFunction(HqlParser.CollectionIndexFunctionContext ctx) {
		final String alias = ctx.identifier().getText();

		final SqmNavigableBinding binding = querySpecProcessingStateStack.getCurrent().getFromElementBuilder().getAliasRegistry().findFromElementByAlias( alias );

		if ( binding == null || !SqmPluralAttributeBinding.class.isInstance( binding ) ) {
			// most likely a semantic problem, but not necessarily...
			throw new ParsingException( "Could not resolve identification variable [" + alias + "] as plural-attribute, encountered : " + binding );
		}

		final SqmPluralAttributeBinding attributeBinding = (SqmPluralAttributeBinding) binding;

		if ( !isIndexedPluralAttribute( attributeBinding ) ) {
			throw new SemanticException(
					"index() function can only be applied to identification variables which resolve to an " +
							"indexed collection (map,list); specified identification variable [" + alias +
							"] resolved to " + attributeBinding
			);
		}

		return NavigableBindingHelper.createCollectionIndexBinding( attributeBinding, attributeBinding.getBoundNavigable().getIndexReference() );
	}

	private boolean isIndexedPluralAttribute(SqmPluralAttributeBinding attributeBinding) {
		return attributeBinding.getBoundNavigable().getCollectionClassification() == CollectionClassification.MAP
				|| attributeBinding.getBoundNavigable().getCollectionClassification() == CollectionClassification.LIST;
	}

	private boolean isList(SqmPluralAttributeBinding attributeBinding) {
		return attributeBinding.getBoundNavigable().getCollectionClassification() == CollectionClassification.LIST;
	}

	@Override
	public MaxElementBindingBasic visitMaxElementFunction(HqlParser.MaxElementFunctionContext ctx) {
		if ( parsingContext.getConsumerContext().useStrictJpaCompliance() ) {
			throw new StrictJpaComplianceViolation( StrictJpaComplianceViolation.Type.HQL_COLLECTION_FUNCTION );
		}

		return new MaxElementBindingBasic( asPluralAttribute( (SqmNavigableBinding) ctx.path().accept( this ) ) );
	}

	@Override
	public MinElementBinding visitMinElementFunction(HqlParser.MinElementFunctionContext ctx) {
		if ( parsingContext.getConsumerContext().useStrictJpaCompliance() ) {
			throw new StrictJpaComplianceViolation( StrictJpaComplianceViolation.Type.HQL_COLLECTION_FUNCTION );
		}

		final SqmPluralAttributeBinding pluralAttributeBinding = asPluralAttribute( (SqmNavigableBinding) ctx.path().accept( this ) );
		switch ( pluralAttributeBinding.getBoundNavigable().getElementReference().getClassification() ) {
			case BASIC: {
				return new MinElementBindingBasic( pluralAttributeBinding );
			}
			case EMBEDDABLE: {
				return new MinElementBindingEmbedded( pluralAttributeBinding );
			}
			case ONE_TO_MANY:
			case MANY_TO_MANY: {
				return new MinElementBindingEntity( pluralAttributeBinding );
			}
			default: {
				throw new NotYetImplementedException( "min-element function not yet supported for ANY elements" );
			}
		}
	}

	@Override
	public MaxIndexBinding visitMaxIndexFunction(HqlParser.MaxIndexFunctionContext ctx) {
		if ( parsingContext.getConsumerContext().useStrictJpaCompliance() ) {
			throw new StrictJpaComplianceViolation( StrictJpaComplianceViolation.Type.HQL_COLLECTION_FUNCTION );
		}

		final SqmPluralAttributeBinding attributeBinding = asPluralAttribute( (SqmNavigableBinding) ctx.path().accept( this ) );
		if ( !isIndexedPluralAttribute( attributeBinding ) ) {
			throw new SemanticException(
					"maxindex() function can only be applied to path expressions which resolve to an " +
							"indexed collection (list,map); specified path [" + ctx.path().getText() +
							"] resolved to " + attributeBinding.getBoundNavigable()
			);
		}

		switch ( attributeBinding.getBoundNavigable().getIndexReference().getClassification() ) {
			case BASIC: {
				return new MaxIndexBindingBasic( attributeBinding );
			}
			case EMBEDDABLE: {
				return new MaxIndexBindingEmbedded( attributeBinding );
			}
			case ONE_TO_MANY:
			case MANY_TO_MANY: {
				return new MaxIndexBindingEntity( attributeBinding );
			}
			default: {
				throw new NotYetImplementedException(  );
			}
		}
	}

	@Override
	public MinIndexBinding visitMinIndexFunction(HqlParser.MinIndexFunctionContext ctx) {
		if ( parsingContext.getConsumerContext().useStrictJpaCompliance() ) {
			throw new StrictJpaComplianceViolation( StrictJpaComplianceViolation.Type.HQL_COLLECTION_FUNCTION );
		}

		final SqmPluralAttributeBinding attributeBinding = asPluralAttribute( (SqmNavigableBinding) ctx.path().accept( this ) );
		if ( !isIndexedPluralAttribute( attributeBinding ) ) {
			throw new SemanticException(
					"minindex() function can only be applied to path expressions which resolve to an " +
							"indexed collection (list,map); specified path [" + ctx.path().getText() +
							"] resolved to " + attributeBinding.getBoundNavigable()
			);
		}

		switch ( attributeBinding.getBoundNavigable().getIndexReference().getClassification() ) {
			case BASIC: {
				return new MinIndexBindingBasic( attributeBinding );
			}
			case EMBEDDABLE: {
				return new MinIndexBindingEmbeddable( attributeBinding );
			}
			case ONE_TO_MANY:
			case MANY_TO_MANY: {
				return new MinIndexBindingEntity( attributeBinding );
			}
			default: {
				throw new NotYetImplementedException(  );
			}
		}
	}

	@Override
	public SubQuerySqmExpression visitSubQueryExpression(HqlParser.SubQueryExpressionContext ctx) {
		final SqmQuerySpec querySpec = visitQuerySpec( ctx.querySpec() );
		return new SubQuerySqmExpression( querySpec, determineTypeDescriptor( querySpec.getSelectClause() ) );
	}

	private static SqmExpressableType determineTypeDescriptor(SqmSelectClause selectClause) {
		if ( selectClause.getSelections().size() != 0 ) {
			return null;
		}

		final SqmSelection selection = selectClause.getSelections().get( 0 );
		return selection.getExpression().getExpressionType();
	}
}
