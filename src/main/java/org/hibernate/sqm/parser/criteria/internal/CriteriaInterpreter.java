/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.parser.criteria.internal;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.persistence.Tuple;
import javax.persistence.criteria.AbstractQuery;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Fetch;
import javax.persistence.criteria.FetchParent;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Selection;
import javax.persistence.criteria.Subquery;

import org.hibernate.sqm.NotYetImplementedException;
import org.hibernate.sqm.domain.Attribute;
import org.hibernate.sqm.domain.BasicType;
import org.hibernate.sqm.domain.EntityType;
import org.hibernate.sqm.domain.PluralAttribute;
import org.hibernate.sqm.domain.SingularAttribute;
import org.hibernate.sqm.domain.Type;
import org.hibernate.sqm.parser.ParsingException;
import org.hibernate.sqm.parser.QueryException;
import org.hibernate.sqm.parser.common.ExpressionTypeHelper;
import org.hibernate.sqm.parser.common.ParsingContext;
import org.hibernate.sqm.parser.common.QuerySpecProcessingState;
import org.hibernate.sqm.parser.common.QuerySpecProcessingStateStandardImpl;
import org.hibernate.sqm.parser.criteria.spi.CriteriaVisitor;
import org.hibernate.sqm.parser.criteria.spi.expression.BooleanExpressionCriteriaPredicate;
import org.hibernate.sqm.parser.criteria.spi.expression.CriteriaExpression;
import org.hibernate.sqm.parser.criteria.spi.expression.LiteralCriteriaExpression;
import org.hibernate.sqm.parser.criteria.spi.expression.ParameterCriteriaExpression;
import org.hibernate.sqm.parser.criteria.spi.predicate.ComparisonCriteriaPredicate;
import org.hibernate.sqm.parser.criteria.spi.predicate.CriteriaPredicate;
import org.hibernate.sqm.parser.criteria.spi.SelectionImplementor;
import org.hibernate.sqm.parser.criteria.spi.expression.function.CastFunctionCriteriaExpression;
import org.hibernate.sqm.parser.criteria.spi.expression.function.GenericFunctionCriteriaExpression;
import org.hibernate.sqm.parser.criteria.spi.path.RootImplementor;
import org.hibernate.sqm.parser.criteria.spi.predicate.NegatedCriteriaPredicate;
import org.hibernate.sqm.parser.criteria.spi.predicate.NullnessCriteriaPredicate;
import org.hibernate.sqm.path.FromElementBinding;
import org.hibernate.sqm.query.QuerySpec;
import org.hibernate.sqm.query.SelectStatement;
import org.hibernate.sqm.query.expression.AttributeReferenceSqmExpression;
import org.hibernate.sqm.query.expression.function.AvgFunctionSqmExpression;
import org.hibernate.sqm.query.expression.BinaryArithmeticSqmExpression;
import org.hibernate.sqm.query.expression.function.CastFunctionSqmExpression;
import org.hibernate.sqm.query.expression.ConcatSqmExpression;
import org.hibernate.sqm.query.expression.ConstantEnumSqmExpression;
import org.hibernate.sqm.query.expression.ConstantFieldSqmExpression;
import org.hibernate.sqm.query.expression.function.CountFunctionSqmExpression;
import org.hibernate.sqm.query.expression.function.CountStarFunctionSqmExpression;
import org.hibernate.sqm.query.expression.EntityTypeSqmExpression;
import org.hibernate.sqm.query.expression.SqmExpression;
import org.hibernate.sqm.query.expression.function.GenericFunctionSqmExpression;
import org.hibernate.sqm.query.expression.LiteralBigDecimalSqmExpression;
import org.hibernate.sqm.query.expression.LiteralBigIntegerSqmExpression;
import org.hibernate.sqm.query.expression.LiteralCharacterSqmExpression;
import org.hibernate.sqm.query.expression.LiteralDoubleSqmExpression;
import org.hibernate.sqm.query.expression.LiteralSqmExpression;
import org.hibernate.sqm.query.expression.LiteralFalseSqmExpression;
import org.hibernate.sqm.query.expression.LiteralFloatSqmExpression;
import org.hibernate.sqm.query.expression.LiteralIntegerSqmExpression;
import org.hibernate.sqm.query.expression.LiteralLongSqmExpression;
import org.hibernate.sqm.query.expression.LiteralNullSqmExpression;
import org.hibernate.sqm.query.expression.LiteralStringSqmExpression;
import org.hibernate.sqm.query.expression.LiteralTrueSqmExpression;
import org.hibernate.sqm.query.expression.function.MaxFunctionSqmExpression;
import org.hibernate.sqm.query.expression.function.MinFunctionSqmExpression;
import org.hibernate.sqm.query.expression.NamedParameterSqmExpression;
import org.hibernate.sqm.query.expression.ParameterSqmExpression;
import org.hibernate.sqm.query.expression.PositionalParameterSqmExpression;
import org.hibernate.sqm.query.expression.SubQuerySqmExpression;
import org.hibernate.sqm.query.expression.function.SumFunctionSqmExpression;
import org.hibernate.sqm.query.expression.UnaryOperationSqmExpression;
import org.hibernate.sqm.query.from.FromClause;
import org.hibernate.sqm.query.from.FromElement;
import org.hibernate.sqm.query.from.FromElementSpace;
import org.hibernate.sqm.query.from.QualifiedAttributeJoinFromElement;
import org.hibernate.sqm.query.from.RootEntityFromElement;
import org.hibernate.sqm.query.order.OrderByClause;
import org.hibernate.sqm.query.order.SortOrder;
import org.hibernate.sqm.query.order.SortSpecification;
import org.hibernate.sqm.query.predicate.AndSqmPredicate;
import org.hibernate.sqm.query.predicate.BetweenSqmPredicate;
import org.hibernate.sqm.query.predicate.BooleanExpressionSqmPredicate;
import org.hibernate.sqm.query.predicate.EmptinessSqmPredicate;
import org.hibernate.sqm.query.predicate.InListSqmPredicate;
import org.hibernate.sqm.query.predicate.InSubQuerySqmPredicate;
import org.hibernate.sqm.query.predicate.LikeSqmPredicate;
import org.hibernate.sqm.query.predicate.MemberOfSqmPredicate;
import org.hibernate.sqm.query.predicate.NegatedSqmPredicate;
import org.hibernate.sqm.query.predicate.NullnessSqmPredicate;
import org.hibernate.sqm.query.predicate.OrSqmPredicate;
import org.hibernate.sqm.query.predicate.SqmPredicate;
import org.hibernate.sqm.query.predicate.RelationalSqmPredicate;
import org.hibernate.sqm.query.predicate.WhereClause;
import org.hibernate.sqm.query.select.AliasedSqmExpressionContainer;
import org.hibernate.sqm.query.select.DynamicInstantiation;
import org.hibernate.sqm.query.select.SelectClause;

/**
 * @author Steve Ebersole
 */
public class CriteriaInterpreter implements CriteriaVisitor {
	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// top level statement visitation

	public static SelectStatement interpretSelectCriteria(CriteriaQuery query, ParsingContext parsingContext) {
		CriteriaInterpreter interpreter = new CriteriaInterpreter( parsingContext );

		final SelectStatement selectStatement = new SelectStatement();
		selectStatement.applyQuerySpec( interpreter.visitQuerySpec( query ) );
		selectStatement.applyOrderByClause( interpreter.visitOrderBy( query ) );

		return selectStatement;
	}


	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// visitation

	private final ParsingContext parsingContext;

	private QuerySpecProcessingState currentQuerySpecProcessingState;

	private CriteriaInterpreter(ParsingContext parsingContext) {
		this.parsingContext = parsingContext;
	}

	private OrderByClause visitOrderBy(CriteriaQuery<?> jpaCriteria) {
		final OrderByClause sqmOrderByClause = new OrderByClause();
		if ( !jpaCriteria.getOrderList().isEmpty() ) {
			for ( Order orderItem : jpaCriteria.getOrderList() ) {
				sqmOrderByClause.addSortSpecification(
						new SortSpecification(
								visitExpression( orderItem.getExpression() ),
								orderItem.isAscending() ? SortOrder.ASCENDING : SortOrder.DESCENDING
						)
				);
			}
		}
		return sqmOrderByClause;
	}


	private QuerySpec visitQuerySpec(AbstractQuery jpaCriteria) {
		currentQuerySpecProcessingState = new QuerySpecProcessingStateStandardImpl( parsingContext, currentQuerySpecProcessingState );
		try {
			return new QuerySpec(
					visitFromClause( jpaCriteria ),
					visitSelectClause( jpaCriteria ),
					visitWhereClause( jpaCriteria )
			);
		}
		finally {
			currentQuerySpecProcessingState = currentQuerySpecProcessingState.getParent();
		}
	}

	private FromClause visitFromClause(AbstractQuery<?> jpaCriteria) {
		final FromClause fromClause = new FromClause();
		for ( Root<?> jpaRoot : jpaCriteria.getRoots() ) {
			final RootImplementor root = (RootImplementor) jpaRoot;
			root.prepareAlias( parsingContext.getImplicitAliasGenerator() );
			final FromElementSpace space = fromClause.makeFromElementSpace();
			final RootEntityFromElement sqmRoot = currentQuerySpecProcessingState.getFromElementBuilder().makeRootEntityFromElement(
					space,
					// todo : coordinate with JPA type system
					root.getEntityType(),
					root.getAlias()
			);
			space.setRoot( sqmRoot );
			bindJoins( root, sqmRoot, space );
			bindFetches( root, sqmRoot, space );
		}

		return fromClause;
	}

	private void bindJoins(From<?,?> lhs, FromElement sqmLhs, FromElementSpace space) {
		for ( Join<?, ?> join : lhs.getJoins() ) {
			final String alias = join.getAlias();
			// todo : we could theoretically reconstruct the "fetch path" via parent refs if we deem it useful..
			@SuppressWarnings("UnnecessaryLocalVariable") final String path  = alias;

			final QualifiedAttributeJoinFromElement sqmJoin = currentQuerySpecProcessingState.getFromElementBuilder().buildAttributeJoin(
					space,
					alias,
					sqmLhs.resolveAttribute( join.getAttribute().getName() ),
					// todo : this should probably leverage the JPA type system
					parsingContext.getConsumerContext().getDomainMetamodel().resolveEntityType( join.getJavaType() ),
					path,
					convert( join.getJoinType() ),
					sqmLhs,
					false
			);
			space.addJoin( sqmJoin );
			bindJoins( join, sqmJoin, space );
		}
	}

	private void bindFetches(FetchParent<?, ?> lhs, FromElement sqmLhs, FromElementSpace space) {
		for ( Fetch<?, ?> fetch : lhs.getFetches() ) {
			final String alias = parsingContext.getImplicitAliasGenerator().buildUniqueImplicitAlias();
			// todo : we could theoretically reconstruct the "fetch path" via parent refs if we deem it useful..
			@SuppressWarnings("UnnecessaryLocalVariable") final String path  = alias;

			final QualifiedAttributeJoinFromElement sqmFetch = currentQuerySpecProcessingState.getFromElementBuilder().buildAttributeJoin(
					space,
					alias,
					sqmLhs.resolveAttribute( fetch.getAttribute().getName() ),
					// todo : this should probably leverage the JPA type system
					parsingContext.getConsumerContext().getDomainMetamodel().resolveEntityType( fetch.getAttribute().getJavaType() ),
					path,
					convert( fetch.getJoinType() ),
					sqmLhs,
					true
			);
			space.addJoin( sqmFetch );
			bindFetches( fetch, sqmFetch, space );
		}
	}

	private org.hibernate.sqm.query.JoinType convert(JoinType joinType) {
		switch ( joinType ) {
			case INNER: {
				return org.hibernate.sqm.query.JoinType.INNER;
			}
			case LEFT: {
				return org.hibernate.sqm.query.JoinType.LEFT;
			}
			case RIGHT: {
				return org.hibernate.sqm.query.JoinType.RIGHT;
			}
		}

		throw new ParsingException( "Unrecognized JPA JoinType : " + joinType );
	}

	private SelectClause visitSelectClause(AbstractQuery jpaCriteria) {
		final SelectClause sqmSelectClause = new SelectClause( jpaCriteria.isDistinct() );

		applySelection( jpaCriteria.getSelection(), sqmSelectClause );

		return sqmSelectClause;
	}

	private void applySelection(Selection<?> selection, AliasedSqmExpressionContainer container) {
		if ( selection instanceof SelectionImplementor ) {
			( (SelectionImplementor) selection ).visitSelections( this, container );
		}
		else if ( selection.isCompoundSelection() ) {
			final AliasedSqmExpressionContainer containerForSelections;
			final Class selectionResultType = selection.getJavaType();
			if ( Tuple.class.isAssignableFrom( selectionResultType )
					|| selectionResultType.isArray()
					|| selectionResultType.equals( Object.class ) ) {
				containerForSelections = container;
			}
			else if ( List.class.equals( selectionResultType ) ) {
				containerForSelections = DynamicInstantiation.forListInstantiation();
			}
			else if ( Map.class.equals( selectionResultType ) ) {
				containerForSelections = DynamicInstantiation.forMapInstantiation();
			}
			else {
				containerForSelections = DynamicInstantiation.forClassInstantiation( selectionResultType );
			}

			for ( Selection<?> nestedSelection : selection.getCompoundSelectionItems() ) {
				applySelection( nestedSelection, containerForSelections );
			}
		}
		else if ( selection instanceof javax.persistence.criteria.Expression ) {
			container.add(
					visitExpression( (javax.persistence.criteria.Expression) selection ),
					interpretAlias( selection.getAlias() )
			);
		}
		else {
			// check the "compound selection items" anyway..
			if ( selection.getCompoundSelectionItems().size() == 1 ) {
				applySelection( selection.getCompoundSelectionItems().get( 0 ), container );
			}
			else {
				throw new QueryException(
						String.format(
								Locale.ROOT,
								"Unexpected JPA Criteria sqm Selection type [%s] encountered; " +
										"was expecting Selection with either #isCompoundSelection()==true or " +
										"ExpressionImplementor implementation",
								selection.getClass().getName()
						)
				);
			}
		}
	}

	private String interpretAlias(String explicitAlias) {
		return isNotEmpty( explicitAlias )
				? explicitAlias
				: parsingContext.getImplicitAliasGenerator().buildUniqueImplicitAlias();
	}

	private static boolean isNotEmpty(String string) {
		return !isEmpty( string );
	}

	private static boolean isEmpty(String string) {
		return string == null || string.isEmpty();
	}


	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// Expressions

	private SqmExpression visitExpression(javax.persistence.criteria.Expression<?> expression) {
		return ( (CriteriaExpression) expression ).visitExpression( this );
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T extends Enum> ConstantEnumSqmExpression<T> visitEnumConstant(T value) {
		return new ConstantEnumSqmExpression<T>(
				value,
				(BasicType<T>) parsingContext.getConsumerContext().getDomainMetamodel().getBasicType( value.getClass() )
		);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> ConstantFieldSqmExpression<T> visitConstant(T value) {
		if ( value == null ) {
			throw new NullPointerException( "Value passed as `constant value` cannot be null" );
		}

		return visitConstant(
				value,
				(BasicType<T>) parsingContext.getConsumerContext().getDomainMetamodel().getBasicType( value.getClass() )
		);
	}

	@Override
	public <T> ConstantFieldSqmExpression<T> visitConstant(T value, BasicType<T> typeDescriptor) {
		return new ConstantFieldSqmExpression<T>( value, typeDescriptor );
	}

	@Override
	public UnaryOperationSqmExpression visitUnaryOperation(
			UnaryOperationSqmExpression.Operation operation,
			javax.persistence.criteria.Expression expression) {
		return new UnaryOperationSqmExpression( operation, visitExpression( expression ) );
	}

	@Override
	public UnaryOperationSqmExpression visitUnaryOperation(
			UnaryOperationSqmExpression.Operation operation,
			javax.persistence.criteria.Expression expression,
			BasicType resultType) {
		return new UnaryOperationSqmExpression( operation, visitExpression( expression ), resultType );
	}

	@Override
	public BinaryArithmeticSqmExpression visitArithmetic(
			BinaryArithmeticSqmExpression.Operation operation,
			javax.persistence.criteria.Expression expression1,
			javax.persistence.criteria.Expression expression2) {
		final SqmExpression firstOperand = visitExpression( expression1 );
		final SqmExpression secondOperand = visitExpression( expression2 );
		return new BinaryArithmeticSqmExpression(
				operation,
				firstOperand,
				secondOperand,
				ExpressionTypeHelper.resolveArithmeticType(
						(BasicType) firstOperand.getExpressionType(),
						(BasicType) secondOperand.getExpressionType(),
						parsingContext.getConsumerContext(),
						operation == BinaryArithmeticSqmExpression.Operation.DIVIDE
				)
		);
	}

	@Override
	public BinaryArithmeticSqmExpression visitArithmetic(
			BinaryArithmeticSqmExpression.Operation operation,
			javax.persistence.criteria.Expression expression1,
			javax.persistence.criteria.Expression expression2,
			BasicType resultType) {
		return new BinaryArithmeticSqmExpression(
				operation,
				visitExpression( expression1 ),
				visitExpression( expression2 ),
				resultType
		);
	}

	@Override
	public FromElementBinding visitIdentificationVariableReference(From reference) {
		return currentQuerySpecProcessingState.findFromElementByIdentificationVariable( reference.getAlias() );
	}

	@Override
	public AttributeReferenceSqmExpression visitAttributeReference(From attributeSource, String attributeName) {
		final FromElement source = currentQuerySpecProcessingState.findFromElementByIdentificationVariable( attributeSource.getAlias() );
		final Attribute attributeDescriptor = source.resolveAttribute( attributeName );
		final Type type;
		if ( attributeDescriptor instanceof SingularAttribute ) {
			type = ( (SingularAttribute) attributeDescriptor ).getType();
		}
		else if ( attributeDescriptor instanceof PluralAttribute ) {
			type = ( (PluralAttribute) attributeDescriptor ).getElementType();
		}
		else {
			throw new ParsingException( "Resolved attribute was neither javax.persistence.metamodel.SingularAttribute nor javax.persistence.metamodel.PluralAttribute" );
		}
		return new AttributeReferenceSqmExpression( source, attributeDescriptor );
	}

	@Override
	public GenericFunctionSqmExpression visitFunction(
			String name,
			BasicType resultTypeDescriptor,
			List<javax.persistence.criteria.Expression<?>> expressions) {
		final List<SqmExpression> sqmExpressions = new ArrayList<SqmExpression>();
		for ( javax.persistence.criteria.Expression expression : expressions ) {
			sqmExpressions.add( visitExpression( expression ) );
		}

		return new GenericFunctionSqmExpression( name, resultTypeDescriptor, sqmExpressions );
	}

	@Override
	public GenericFunctionSqmExpression visitFunction(
			String name,
			BasicType resultTypeDescriptor,
			javax.persistence.criteria.Expression<?>... expressions) {
		// todo : handle the standard function calls specially...
		// for now always use the generic expression
		final List<SqmExpression> arguments = new ArrayList<SqmExpression>();
		if ( expressions != null ) {
			for ( javax.persistence.criteria.Expression expression : expressions ) {
				arguments.add( visitExpression( expression ) );
			}

		}
		return new GenericFunctionSqmExpression(
				name,
				resultTypeDescriptor,
				arguments
		);
	}

	@Override
	public AvgFunctionSqmExpression visitAvgFunction(javax.persistence.criteria.Expression expression, boolean distinct) {
		final SqmExpression sqmExpression = visitExpression( expression );
		return new AvgFunctionSqmExpression(
				sqmExpression,
				distinct,
				(BasicType) sqmExpression.getExpressionType()
		);
	}

	@Override
	public AvgFunctionSqmExpression visitAvgFunction(javax.persistence.criteria.Expression expression, boolean distinct, BasicType resultType) {
		return new AvgFunctionSqmExpression( visitExpression( expression ), distinct, resultType );
	}

	@Override
	public CountFunctionSqmExpression visitCountFunction(javax.persistence.criteria.Expression expression, boolean distinct) {
		final SqmExpression sqmExpression = visitExpression( expression );
		return new CountFunctionSqmExpression(
				sqmExpression,
				distinct,
				(BasicType) sqmExpression.getExpressionType()
		);
	}

	@Override
	public CountFunctionSqmExpression visitCountFunction(javax.persistence.criteria.Expression expression, boolean distinct, BasicType resultType) {
		return new CountFunctionSqmExpression( visitExpression( expression ), distinct, resultType );
	}

	@Override
	public CountStarFunctionSqmExpression visitCountStarFunction(boolean distinct) {
		return new CountStarFunctionSqmExpression(
				distinct,
				parsingContext.getConsumerContext().getDomainMetamodel().getBasicType( Long.class )
		);
	}

	@Override
	public CountStarFunctionSqmExpression visitCountStarFunction(boolean distinct, BasicType resultType) {
		return new CountStarFunctionSqmExpression( distinct, resultType );
	}

	@Override
	public MaxFunctionSqmExpression visitMaxFunction(javax.persistence.criteria.Expression expression, boolean distinct) {
		final SqmExpression sqmExpression = visitExpression( expression );
		return new MaxFunctionSqmExpression( sqmExpression, distinct, (BasicType) sqmExpression.getExpressionType() );
	}

	@Override
	public MaxFunctionSqmExpression visitMaxFunction(javax.persistence.criteria.Expression expression, boolean distinct, BasicType resultType) {
		return new MaxFunctionSqmExpression( visitExpression( expression ), distinct, resultType );
	}

	@Override
	public MinFunctionSqmExpression visitMinFunction(javax.persistence.criteria.Expression expression, boolean distinct) {
		final SqmExpression sqmExpression = visitExpression( expression );
		return new MinFunctionSqmExpression( sqmExpression, distinct, (BasicType) sqmExpression.getExpressionType() );
	}

	@Override
	public MinFunctionSqmExpression visitMinFunction(javax.persistence.criteria.Expression expression, boolean distinct, BasicType resultType) {
		return new MinFunctionSqmExpression( visitExpression( expression ), distinct, resultType );
	}

	@Override
	public SumFunctionSqmExpression visitSumFunction(javax.persistence.criteria.Expression expression, boolean distinct) {
		final SqmExpression sqmExpression = visitExpression( expression );
		return new SumFunctionSqmExpression(
				sqmExpression,
				distinct,
				ExpressionTypeHelper.resolveSingleNumericType(
						(BasicType) sqmExpression.getExpressionType(),
						parsingContext.getConsumerContext()
				)
		);
	}

	@Override
	public SumFunctionSqmExpression visitSumFunction(javax.persistence.criteria.Expression expression, boolean distinct, BasicType resultType) {
		return new SumFunctionSqmExpression( visitExpression( expression ), distinct, resultType );
	}

	@Override
	public ConcatSqmExpression visitConcat(
			javax.persistence.criteria.Expression expression1,
			javax.persistence.criteria.Expression expression2) {
		return new ConcatSqmExpression(
				visitExpression( expression1 ),
				visitExpression( expression2 )
		);
	}

	@Override
	public ConcatSqmExpression visitConcat(
			javax.persistence.criteria.Expression expression1,
			javax.persistence.criteria.Expression expression2,
			BasicType resultType) {
		return new ConcatSqmExpression(
				visitExpression( expression1 ),
				visitExpression( expression2 ),
				resultType
		);
	}

	@Override
	public EntityTypeSqmExpression visitEntityType(String identificationVariable) {
		final FromElement fromElement = currentQuerySpecProcessingState.findFromElementByIdentificationVariable( identificationVariable );
		return new EntityTypeSqmExpression( (EntityType) fromElement.getBoundModelType() );
	}

	@Override
	public EntityTypeSqmExpression visitEntityType(String identificationVariable, String attributeName) {
		final FromElement fromElement = currentQuerySpecProcessingState.findFromElementByIdentificationVariable( identificationVariable );
		return new EntityTypeSqmExpression( (EntityType) fromElement.resolveAttribute( attributeName ) );
	}

	@Override
	public SubQuerySqmExpression visitSubQuery(Subquery subquery) {
		// todo : need to work out the "proper" Type here...
		return new SubQuerySqmExpression( visitQuerySpec( subquery ), null );
	}


	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// Predicates


	private WhereClause visitWhereClause(AbstractQuery<?> jpaCriteria) {
		final WhereClause whereClause = new WhereClause();
		if ( jpaCriteria.getRestriction() != null ) {
			whereClause.setPredicate( visitPredicate( jpaCriteria.getRestriction() ) );
		}
		return whereClause;
	}

	private SqmPredicate visitPredicate(javax.persistence.criteria.Predicate predicate) {
		return ( (CriteriaPredicate) predicate ).visitPredicate( this );
	}

	private SqmPredicate visitPredicate(javax.persistence.criteria.Expression<Boolean> predicate) {
		return ( (CriteriaPredicate) predicate ).visitPredicate( this );
	}

	@Override
	public AndSqmPredicate visitAndPredicate(List<javax.persistence.criteria.Expression<Boolean>> predicates) {
		final int predicateCount = predicates.size();

		if ( predicateCount < 2 ) {
			throw new QueryException(
					"Expecting 2 or more predicate expressions to form conjunction (AND), but found [" + predicateCount + "]"
			);
		}

		AndSqmPredicate result = new AndSqmPredicate(
				visitPredicate( predicates.get( 0 ) ),
				visitPredicate( predicates.get( 1 ) )
		);

		if ( predicateCount > 2 ) {
			for ( int i = 2; i < predicateCount; i++ ) {
				result = new AndSqmPredicate(
						result,
						visitPredicate( predicates.get( i ) )
				);
			}
		}

		return result;
	}

	@Override
	public OrSqmPredicate visitOrPredicate(List<javax.persistence.criteria.Expression<Boolean>> predicates) {
		final int predicateCount = predicates.size();

		if ( predicateCount < 2 ) {
			throw new QueryException(
					"Expecting 2 or more predicate expressions to form disjunction (OR), but found [" + predicateCount + "]"
			);
		}

		OrSqmPredicate result = new OrSqmPredicate(
				visitPredicate( predicates.get( 0 ) ),
				visitPredicate( predicates.get( 1 ) )
		);

		if ( predicateCount > 2 ) {
			for ( int i = 2; i < predicateCount; i++ ) {
				result = new OrSqmPredicate(
						result,
						visitPredicate( predicates.get( i ) )
				);
			}
		}

		return result;
	}

	@Override
	public EmptinessSqmPredicate visitEmptinessPredicate(From attributeSource, String attributeName, boolean negated) {
		final AttributeReferenceSqmExpression attributeReference = visitAttributeReference( attributeSource, attributeName );
		return new EmptinessSqmPredicate( attributeReference, negated );
	}

	@Override
	public MemberOfSqmPredicate visitMemberOfPredicate(From attributeSource, String attributeName, boolean negated) {
		throw new NotYetImplementedException();
	}

	@Override
	public BetweenSqmPredicate visitBetweenPredicate(
			javax.persistence.criteria.Expression expression,
			javax.persistence.criteria.Expression lowerBound,
			javax.persistence.criteria.Expression upperBound,
			boolean negated) {
		return new BetweenSqmPredicate(
				visitExpression( expression ),
				visitExpression( lowerBound ),
				visitExpression( upperBound ),
				negated
		);
	}

	@Override
	public LikeSqmPredicate visitLikePredicate(
			javax.persistence.criteria.Expression<String> matchExpression,
			javax.persistence.criteria.Expression<String> pattern,
			javax.persistence.criteria.Expression<Character> escapeCharacter,
			boolean negated) {
		return new LikeSqmPredicate(
				visitExpression( matchExpression ),
				visitExpression( pattern ),
				visitExpression( escapeCharacter ),
				negated
		);
	}

	@Override
	public InSubQuerySqmPredicate visitInSubQueryPredicate(
			javax.persistence.criteria.Expression testExpression,
			Subquery subquery,
			boolean negated) {
		return new InSubQuerySqmPredicate(
				visitExpression( testExpression ),
				visitSubQuery( subquery ),
				negated
		);
	}

	@Override
	public InListSqmPredicate visitInTupleListPredicate(
			javax.persistence.criteria.Expression testExpression,
			List<javax.persistence.criteria.Expression> expressionsList,
			boolean negated) {
		final List<SqmExpression> expressions = new ArrayList<SqmExpression>();
		for ( javax.persistence.criteria.Expression expression : expressionsList ) {
			expressions.add( visitExpression( expression ) );
		}

		return new InListSqmPredicate(
				visitExpression( testExpression ),
				expressions,
				negated
		);
	}

	@Override
	public BooleanExpressionSqmPredicate visitBooleanExpressionPredicate(BooleanExpressionCriteriaPredicate predicate) {
		return new BooleanExpressionSqmPredicate( visitExpression( predicate.getOperand() ) );
	}

	@Override
	public SqmExpression visitRoot(RootImplementor root) {
		final FromElement fromElement = currentQuerySpecProcessingState.findFromElementByIdentificationVariable( root.getAlias() );
		assert fromElement != null;

		return fromElement;
	}


	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// New sigs

	@Override
	@SuppressWarnings("unchecked")
	public <T> LiteralSqmExpression<T> visitLiteral(LiteralCriteriaExpression expression) {
		if ( expression.getLiteral() == null ) {
			return (LiteralSqmExpression<T>) new LiteralNullSqmExpression();
		}

		final Class literalJavaType = expression.getJavaType();
		if ( Boolean.class.isAssignableFrom( literalJavaType ) ) {
			if ( (Boolean) expression.getLiteral() ) {
				return (LiteralSqmExpression<T>) new LiteralTrueSqmExpression( (BasicType<Boolean>) expression.getLiteral() );
			}
			else {
				return (LiteralSqmExpression<T>) new LiteralFalseSqmExpression( (BasicType<Boolean>) expression.getLiteral() );
			}
		}
		else if ( Integer.class.isAssignableFrom( literalJavaType ) ) {
			return (LiteralSqmExpression<T>) new LiteralIntegerSqmExpression(
					(Integer) expression.getLiteral(),
					(BasicType<Integer>) expression.getExpressionSqmType()
			);
		}
		else if ( Long.class.isAssignableFrom( literalJavaType ) ) {
			return (LiteralSqmExpression<T>) new LiteralLongSqmExpression(
					(Long) expression.getLiteral(),
					(BasicType<Long>) expression.getExpressionSqmType()
			);
		}
		else if ( Float.class.isAssignableFrom( literalJavaType ) ) {
			return (LiteralSqmExpression<T>) new LiteralFloatSqmExpression(
					(Float) expression.getLiteral(),
					(BasicType<Float>) expression.getExpressionSqmType()
			);
		}
		else if ( Double.class.isAssignableFrom( literalJavaType ) ) {
			return (LiteralSqmExpression<T>) new LiteralDoubleSqmExpression(
					(Double) expression.getLiteral(),
					(BasicType<Double>) expression.getExpressionSqmType()
			);
		}
		else if ( BigInteger.class.isAssignableFrom( literalJavaType ) ) {
			return (LiteralSqmExpression<T>) new LiteralBigIntegerSqmExpression(
					(BigInteger) expression.getLiteral(),
					(BasicType<BigInteger>) expression.getExpressionSqmType()
			);
		}
		else if ( BigDecimal.class.isAssignableFrom( literalJavaType ) ) {
			return (LiteralSqmExpression<T>) new LiteralBigDecimalSqmExpression(
					(BigDecimal) expression.getLiteral(),
					(BasicType<BigDecimal>) expression.getExpressionSqmType()
			);
		}
		else if ( Character.class.isAssignableFrom( literalJavaType ) ) {
			return (LiteralSqmExpression<T>) new LiteralCharacterSqmExpression(
					(Character) expression.getLiteral(),
					(BasicType<Character>) expression.getExpressionSqmType()
			);
		}
		else if ( String.class.isAssignableFrom( literalJavaType ) ) {
			return (LiteralSqmExpression<T>) new LiteralStringSqmExpression(
					(String) expression.getLiteral(),
					(BasicType<String>) expression.getExpressionSqmType()
			);
		}

		throw new QueryException(
				"Unexpected literal expression [value=" + expression.getLiteral() +
						", javaType=" + literalJavaType.getName() +
						"]; expecting boolean, int, long, float, double, BigInteger, BigDecimal, char, or String"
		);
	}

	@Override
	public <T> ParameterSqmExpression visitParameter(ParameterCriteriaExpression<T> expression) {
		if ( isNotEmpty( expression.getName() ) ) {
			return new NamedParameterSqmExpression( expression.getName(), expression.getExpressionSqmType() );
		}
		else if ( expression.getPosition() != null ) {
			return new PositionalParameterSqmExpression( expression.getPosition(), expression.getExpressionSqmType() );
		}

		throw new QueryException( "ParameterExpression did not define name nor position" );
	}

	@Override
	public <T,Y> CastFunctionSqmExpression visitCastFunction(CastFunctionCriteriaExpression<T,Y> function) {
		return new CastFunctionSqmExpression(
				function.getExpressionToCast().visitExpression( this ),
				function.getFunctionResultType()
		);
	}

	@Override
	public <T> GenericFunctionSqmExpression visitGenericFunction(GenericFunctionCriteriaExpression<T> function) {
		final List<SqmExpression> arguments;
		if ( function.getArguments() != null && !function.getArguments().isEmpty() ) {
			arguments = new ArrayList<SqmExpression>();
			for ( CriteriaExpression<?> argument : function.getArguments() ) {
				arguments.add( argument.visitExpression( this ) );
			}
		}
		else {
			arguments = Collections.emptyList();
		}
		return new GenericFunctionSqmExpression(
				function.getFunctionName(),
				function.getFunctionResultType(),
				arguments
		);
	}

	@Override
	public RelationalSqmPredicate visitRelationalPredicate(ComparisonCriteriaPredicate predicate) {
		return new RelationalSqmPredicate(
				predicate.getComparisonOperator(),
				visitExpression( predicate.getLeftHandOperand() ),
				visitExpression( predicate.getRightHandOperand() )
		);
	}

	@Override
	public NegatedSqmPredicate visitNegatedPredicate(NegatedCriteriaPredicate predicate) {
		return new NegatedSqmPredicate( visitPredicate( predicate.getPredicateToBeNegated() ) );
	}

	@Override
	public NullnessSqmPredicate visitNullnessPredicate(NullnessCriteriaPredicate predicate) {
		return new NullnessSqmPredicate( visitExpression( predicate.getOperand() ) );
	}

}
