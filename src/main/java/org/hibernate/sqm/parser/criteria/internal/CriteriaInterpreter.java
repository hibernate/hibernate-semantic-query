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
import org.hibernate.sqm.query.expression.AttributeReferenceExpression;
import org.hibernate.sqm.query.expression.function.AvgFunction;
import org.hibernate.sqm.query.expression.BinaryArithmeticExpression;
import org.hibernate.sqm.query.expression.function.CastFunctionExpression;
import org.hibernate.sqm.query.expression.ConcatExpression;
import org.hibernate.sqm.query.expression.ConstantEnumExpression;
import org.hibernate.sqm.query.expression.ConstantFieldExpression;
import org.hibernate.sqm.query.expression.function.CountFunction;
import org.hibernate.sqm.query.expression.function.CountStarFunction;
import org.hibernate.sqm.query.expression.EntityTypeExpression;
import org.hibernate.sqm.query.expression.Expression;
import org.hibernate.sqm.query.expression.function.GenericFunctionExpression;
import org.hibernate.sqm.query.expression.LiteralBigDecimalExpression;
import org.hibernate.sqm.query.expression.LiteralBigIntegerExpression;
import org.hibernate.sqm.query.expression.LiteralCharacterExpression;
import org.hibernate.sqm.query.expression.LiteralDoubleExpression;
import org.hibernate.sqm.query.expression.LiteralExpression;
import org.hibernate.sqm.query.expression.LiteralFalseExpression;
import org.hibernate.sqm.query.expression.LiteralFloatExpression;
import org.hibernate.sqm.query.expression.LiteralIntegerExpression;
import org.hibernate.sqm.query.expression.LiteralLongExpression;
import org.hibernate.sqm.query.expression.LiteralNullExpression;
import org.hibernate.sqm.query.expression.LiteralStringExpression;
import org.hibernate.sqm.query.expression.LiteralTrueExpression;
import org.hibernate.sqm.query.expression.function.MaxFunction;
import org.hibernate.sqm.query.expression.function.MinFunction;
import org.hibernate.sqm.query.expression.NamedParameterExpression;
import org.hibernate.sqm.query.expression.ParameterExpression;
import org.hibernate.sqm.query.expression.PositionalParameterExpression;
import org.hibernate.sqm.query.expression.SubQueryExpression;
import org.hibernate.sqm.query.expression.function.SumFunction;
import org.hibernate.sqm.query.expression.UnaryOperationExpression;
import org.hibernate.sqm.query.from.FromClause;
import org.hibernate.sqm.query.from.FromElement;
import org.hibernate.sqm.query.from.FromElementSpace;
import org.hibernate.sqm.query.from.QualifiedAttributeJoinFromElement;
import org.hibernate.sqm.query.from.RootEntityFromElement;
import org.hibernate.sqm.query.order.OrderByClause;
import org.hibernate.sqm.query.order.SortOrder;
import org.hibernate.sqm.query.order.SortSpecification;
import org.hibernate.sqm.query.predicate.AndPredicate;
import org.hibernate.sqm.query.predicate.BetweenPredicate;
import org.hibernate.sqm.query.predicate.BooleanExpressionPredicate;
import org.hibernate.sqm.query.predicate.EmptinessPredicate;
import org.hibernate.sqm.query.predicate.InListPredicate;
import org.hibernate.sqm.query.predicate.InSubQueryPredicate;
import org.hibernate.sqm.query.predicate.LikePredicate;
import org.hibernate.sqm.query.predicate.MemberOfPredicate;
import org.hibernate.sqm.query.predicate.NegatedPredicate;
import org.hibernate.sqm.query.predicate.NullnessPredicate;
import org.hibernate.sqm.query.predicate.OrPredicate;
import org.hibernate.sqm.query.predicate.Predicate;
import org.hibernate.sqm.query.predicate.RelationalPredicate;
import org.hibernate.sqm.query.predicate.WhereClause;
import org.hibernate.sqm.query.select.AliasedExpressionContainer;
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

	private void applySelection(Selection<?> selection, AliasedExpressionContainer container) {
		if ( selection instanceof SelectionImplementor ) {
			( (SelectionImplementor) selection ).visitSelections( this, container );
		}
		else if ( selection.isCompoundSelection() ) {
			final AliasedExpressionContainer containerForSelections;
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

	private Expression visitExpression(javax.persistence.criteria.Expression<?> expression) {
		return ( (CriteriaExpression) expression ).visitExpression( this );
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T extends Enum> ConstantEnumExpression<T> visitEnumConstant(T value) {
		return new ConstantEnumExpression<T>(
				value,
				(BasicType<T>) parsingContext.getConsumerContext().getDomainMetamodel().getBasicType( value.getClass() )
		);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> ConstantFieldExpression<T> visitConstant(T value) {
		if ( value == null ) {
			throw new NullPointerException( "Value passed as `constant value` cannot be null" );
		}

		return visitConstant(
				value,
				(BasicType<T>) parsingContext.getConsumerContext().getDomainMetamodel().getBasicType( value.getClass() )
		);
	}

	@Override
	public <T> ConstantFieldExpression<T> visitConstant(T value, BasicType<T> typeDescriptor) {
		return new ConstantFieldExpression<T>( value, typeDescriptor );
	}

	@Override
	public UnaryOperationExpression visitUnaryOperation(
			UnaryOperationExpression.Operation operation,
			javax.persistence.criteria.Expression expression) {
		return new UnaryOperationExpression( operation, visitExpression( expression ) );
	}

	@Override
	public UnaryOperationExpression visitUnaryOperation(
			UnaryOperationExpression.Operation operation,
			javax.persistence.criteria.Expression expression,
			BasicType resultType) {
		return new UnaryOperationExpression( operation, visitExpression( expression ), resultType );
	}

	@Override
	public BinaryArithmeticExpression visitArithmetic(
			BinaryArithmeticExpression.Operation operation,
			javax.persistence.criteria.Expression expression1,
			javax.persistence.criteria.Expression expression2) {
		final Expression firstOperand = visitExpression( expression1 );
		final Expression secondOperand = visitExpression( expression2 );
		return new BinaryArithmeticExpression(
				operation,
				firstOperand,
				secondOperand,
				ExpressionTypeHelper.resolveArithmeticType(
						(BasicType) firstOperand.getExpressionType(),
						(BasicType) secondOperand.getExpressionType(),
						parsingContext.getConsumerContext(),
						operation == BinaryArithmeticExpression.Operation.DIVIDE
				)
		);
	}

	@Override
	public BinaryArithmeticExpression visitArithmetic(
			BinaryArithmeticExpression.Operation operation,
			javax.persistence.criteria.Expression expression1,
			javax.persistence.criteria.Expression expression2,
			BasicType resultType) {
		return new BinaryArithmeticExpression(
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
	public AttributeReferenceExpression visitAttributeReference(From attributeSource, String attributeName) {
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
		return new AttributeReferenceExpression( source, attributeDescriptor );
	}

	@Override
	public GenericFunctionExpression visitFunction(
			String name,
			BasicType resultTypeDescriptor,
			List<javax.persistence.criteria.Expression<?>> expressions) {
		final List<Expression> sqmExpressions = new ArrayList<Expression>();
		for ( javax.persistence.criteria.Expression expression : expressions ) {
			sqmExpressions.add( visitExpression( expression ) );
		}

		return new GenericFunctionExpression( name, resultTypeDescriptor, sqmExpressions );
	}

	@Override
	public GenericFunctionExpression visitFunction(
			String name,
			BasicType resultTypeDescriptor,
			javax.persistence.criteria.Expression<?>... expressions) {
		// todo : handle the standard function calls specially...
		// for now always use the generic expression
		final List<Expression> arguments = new ArrayList<Expression>();
		if ( expressions != null ) {
			for ( javax.persistence.criteria.Expression expression : expressions ) {
				arguments.add( visitExpression( expression ) );
			}

		}
		return new GenericFunctionExpression(
				name,
				resultTypeDescriptor,
				arguments
		);
	}

	@Override
	public AvgFunction visitAvgFunction(javax.persistence.criteria.Expression expression, boolean distinct) {
		final Expression sqmExpression = visitExpression( expression );
		return new AvgFunction(
				sqmExpression,
				distinct,
				(BasicType) sqmExpression.getExpressionType()
		);
	}

	@Override
	public AvgFunction visitAvgFunction(javax.persistence.criteria.Expression expression, boolean distinct, BasicType resultType) {
		return new AvgFunction( visitExpression( expression ), distinct, resultType );
	}

	@Override
	public CountFunction visitCountFunction(javax.persistence.criteria.Expression expression, boolean distinct) {
		final Expression sqmExpression = visitExpression( expression );
		return new CountFunction(
				sqmExpression,
				distinct,
				(BasicType) sqmExpression.getExpressionType()
		);
	}

	@Override
	public CountFunction visitCountFunction(javax.persistence.criteria.Expression expression, boolean distinct, BasicType resultType) {
		return new CountFunction( visitExpression( expression ), distinct, resultType );
	}

	@Override
	public CountStarFunction visitCountStarFunction(boolean distinct) {
		return new CountStarFunction(
				distinct,
				parsingContext.getConsumerContext().getDomainMetamodel().getBasicType( Long.class )
		);
	}

	@Override
	public CountStarFunction visitCountStarFunction(boolean distinct, BasicType resultType) {
		return new CountStarFunction( distinct, resultType );
	}

	@Override
	public MaxFunction visitMaxFunction(javax.persistence.criteria.Expression expression, boolean distinct) {
		final Expression sqmExpression = visitExpression( expression );
		return new MaxFunction( sqmExpression, distinct, (BasicType) sqmExpression.getExpressionType() );
	}

	@Override
	public MaxFunction visitMaxFunction(javax.persistence.criteria.Expression expression, boolean distinct, BasicType resultType) {
		return new MaxFunction( visitExpression( expression ), distinct, resultType );
	}

	@Override
	public MinFunction visitMinFunction(javax.persistence.criteria.Expression expression, boolean distinct) {
		final Expression sqmExpression = visitExpression( expression );
		return new MinFunction( sqmExpression, distinct, (BasicType) sqmExpression.getExpressionType() );
	}

	@Override
	public MinFunction visitMinFunction(javax.persistence.criteria.Expression expression, boolean distinct, BasicType resultType) {
		return new MinFunction( visitExpression( expression ), distinct, resultType );
	}

	@Override
	public SumFunction visitSumFunction(javax.persistence.criteria.Expression expression, boolean distinct) {
		final Expression sqmExpression = visitExpression( expression );
		return new SumFunction(
				sqmExpression,
				distinct,
				ExpressionTypeHelper.resolveSingleNumericType(
						(BasicType) sqmExpression.getExpressionType(),
						parsingContext.getConsumerContext()
				)
		);
	}

	@Override
	public SumFunction visitSumFunction(javax.persistence.criteria.Expression expression, boolean distinct, BasicType resultType) {
		return new SumFunction( visitExpression( expression ), distinct, resultType );
	}

	@Override
	public ConcatExpression visitConcat(
			javax.persistence.criteria.Expression expression1,
			javax.persistence.criteria.Expression expression2) {
		return new ConcatExpression(
				visitExpression( expression1 ),
				visitExpression( expression2 )
		);
	}

	@Override
	public ConcatExpression visitConcat(
			javax.persistence.criteria.Expression expression1,
			javax.persistence.criteria.Expression expression2,
			BasicType resultType) {
		return new ConcatExpression(
				visitExpression( expression1 ),
				visitExpression( expression2 ),
				resultType
		);
	}

	@Override
	public EntityTypeExpression visitEntityType(String identificationVariable) {
		final FromElement fromElement = currentQuerySpecProcessingState.findFromElementByIdentificationVariable( identificationVariable );
		return new EntityTypeExpression( (EntityType) fromElement.getBoundModelType() );
	}

	@Override
	public EntityTypeExpression visitEntityType(String identificationVariable, String attributeName) {
		final FromElement fromElement = currentQuerySpecProcessingState.findFromElementByIdentificationVariable( identificationVariable );
		return new EntityTypeExpression( (EntityType) fromElement.resolveAttribute( attributeName ) );
	}

	@Override
	public SubQueryExpression visitSubQuery(Subquery subquery) {
		// todo : need to work out the "proper" Type here...
		return new SubQueryExpression( visitQuerySpec( subquery ), null );
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

	private Predicate visitPredicate(javax.persistence.criteria.Predicate predicate) {
		return ( (CriteriaPredicate) predicate ).visitPredicate( this );
	}

	private Predicate visitPredicate(javax.persistence.criteria.Expression<Boolean> predicate) {
		return ( (CriteriaPredicate) predicate ).visitPredicate( this );
	}

	@Override
	public AndPredicate visitAndPredicate(List<javax.persistence.criteria.Expression<Boolean>> predicates) {
		final int predicateCount = predicates.size();

		if ( predicateCount < 2 ) {
			throw new QueryException(
					"Expecting 2 or more predicate expressions to form conjunction (AND), but found [" + predicateCount + "]"
			);
		}

		AndPredicate result = new AndPredicate(
				visitPredicate( predicates.get( 0 ) ),
				visitPredicate( predicates.get( 1 ) )
		);

		if ( predicateCount > 2 ) {
			for ( int i = 2; i < predicateCount; i++ ) {
				result = new AndPredicate(
						result,
						visitPredicate( predicates.get( i ) )
				);
			}
		}

		return result;
	}

	@Override
	public OrPredicate visitOrPredicate(List<javax.persistence.criteria.Expression<Boolean>> predicates) {
		final int predicateCount = predicates.size();

		if ( predicateCount < 2 ) {
			throw new QueryException(
					"Expecting 2 or more predicate expressions to form disjunction (OR), but found [" + predicateCount + "]"
			);
		}

		OrPredicate result = new OrPredicate(
				visitPredicate( predicates.get( 0 ) ),
				visitPredicate( predicates.get( 1 ) )
		);

		if ( predicateCount > 2 ) {
			for ( int i = 2; i < predicateCount; i++ ) {
				result = new OrPredicate(
						result,
						visitPredicate( predicates.get( i ) )
				);
			}
		}

		return result;
	}

	@Override
	public EmptinessPredicate visitEmptinessPredicate(From attributeSource, String attributeName, boolean negated) {
		final AttributeReferenceExpression attributeReference = visitAttributeReference( attributeSource, attributeName );
		return new EmptinessPredicate( attributeReference, negated );
	}

	@Override
	public MemberOfPredicate visitMemberOfPredicate(From attributeSource, String attributeName, boolean negated) {
		throw new NotYetImplementedException();
	}

	@Override
	public BetweenPredicate visitBetweenPredicate(
			javax.persistence.criteria.Expression expression,
			javax.persistence.criteria.Expression lowerBound,
			javax.persistence.criteria.Expression upperBound,
			boolean negated) {
		return new BetweenPredicate(
				visitExpression( expression ),
				visitExpression( lowerBound ),
				visitExpression( upperBound ),
				negated
		);
	}

	@Override
	public LikePredicate visitLikePredicate(
			javax.persistence.criteria.Expression<String> matchExpression,
			javax.persistence.criteria.Expression<String> pattern,
			javax.persistence.criteria.Expression<Character> escapeCharacter,
			boolean negated) {
		return new LikePredicate(
				visitExpression( matchExpression ),
				visitExpression( pattern ),
				visitExpression( escapeCharacter ),
				negated
		);
	}

	@Override
	public InSubQueryPredicate visitInSubQueryPredicate(
			javax.persistence.criteria.Expression testExpression,
			Subquery subquery,
			boolean negated) {
		return new InSubQueryPredicate(
				visitExpression( testExpression ),
				visitSubQuery( subquery ),
				negated
		);
	}

	@Override
	public InListPredicate visitInTupleListPredicate(
			javax.persistence.criteria.Expression testExpression,
			List<javax.persistence.criteria.Expression> expressionsList,
			boolean negated) {
		final List<Expression> expressions = new ArrayList<Expression>();
		for ( javax.persistence.criteria.Expression expression : expressionsList ) {
			expressions.add( visitExpression( expression ) );
		}

		return new InListPredicate(
				visitExpression( testExpression ),
				expressions,
				negated
		);
	}

	@Override
	public BooleanExpressionPredicate visitBooleanExpressionPredicate(BooleanExpressionCriteriaPredicate predicate) {
		return new BooleanExpressionPredicate( visitExpression( predicate.getOperand() ) );
	}

	@Override
	public Expression visitRoot(RootImplementor root) {
		final FromElement fromElement = currentQuerySpecProcessingState.findFromElementByIdentificationVariable( root.getAlias() );
		assert fromElement != null;

		return fromElement;
	}


	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// New sigs

	@Override
	@SuppressWarnings("unchecked")
	public <T> LiteralExpression<T> visitLiteral(LiteralCriteriaExpression expression) {
		if ( expression.getLiteral() == null ) {
			return (LiteralExpression<T>) new LiteralNullExpression();
		}

		final Class literalJavaType = expression.getJavaType();
		if ( Boolean.class.isAssignableFrom( literalJavaType ) ) {
			if ( (Boolean) expression.getLiteral() ) {
				return (LiteralExpression<T>) new LiteralTrueExpression( (BasicType<Boolean>) expression.getLiteral() );
			}
			else {
				return (LiteralExpression<T>) new LiteralFalseExpression( (BasicType<Boolean>) expression.getLiteral() );
			}
		}
		else if ( Integer.class.isAssignableFrom( literalJavaType ) ) {
			return (LiteralExpression<T>) new LiteralIntegerExpression(
					(Integer) expression.getLiteral(),
					(BasicType<Integer>) expression.getExpressionSqmType()
			);
		}
		else if ( Long.class.isAssignableFrom( literalJavaType ) ) {
			return (LiteralExpression<T>) new LiteralLongExpression(
					(Long) expression.getLiteral(),
					(BasicType<Long>) expression.getExpressionSqmType()
			);
		}
		else if ( Float.class.isAssignableFrom( literalJavaType ) ) {
			return (LiteralExpression<T>) new LiteralFloatExpression(
					(Float) expression.getLiteral(),
					(BasicType<Float>) expression.getExpressionSqmType()
			);
		}
		else if ( Double.class.isAssignableFrom( literalJavaType ) ) {
			return (LiteralExpression<T>) new LiteralDoubleExpression(
					(Double) expression.getLiteral(),
					(BasicType<Double>) expression.getExpressionSqmType()
			);
		}
		else if ( BigInteger.class.isAssignableFrom( literalJavaType ) ) {
			return (LiteralExpression<T>) new LiteralBigIntegerExpression(
					(BigInteger) expression.getLiteral(),
					(BasicType<BigInteger>) expression.getExpressionSqmType()
			);
		}
		else if ( BigDecimal.class.isAssignableFrom( literalJavaType ) ) {
			return (LiteralExpression<T>) new LiteralBigDecimalExpression(
					(BigDecimal) expression.getLiteral(),
					(BasicType<BigDecimal>) expression.getExpressionSqmType()
			);
		}
		else if ( Character.class.isAssignableFrom( literalJavaType ) ) {
			return (LiteralExpression<T>) new LiteralCharacterExpression(
					(Character) expression.getLiteral(),
					(BasicType<Character>) expression.getExpressionSqmType()
			);
		}
		else if ( String.class.isAssignableFrom( literalJavaType ) ) {
			return (LiteralExpression<T>) new LiteralStringExpression(
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
	public <T> ParameterExpression visitParameter(ParameterCriteriaExpression<T> expression) {
		if ( isNotEmpty( expression.getName() ) ) {
			return new NamedParameterExpression( expression.getName(), expression.getExpressionSqmType() );
		}
		else if ( expression.getPosition() != null ) {
			return new PositionalParameterExpression( expression.getPosition(), expression.getExpressionSqmType() );
		}

		throw new QueryException( "ParameterExpression did not define name nor position" );
	}

	@Override
	public <T,Y> CastFunctionExpression visitCastFunction(CastFunctionCriteriaExpression<T,Y> function) {
		return new CastFunctionExpression(
				function.getExpressionToCast().visitExpression( this ),
				function.getFunctionResultType()
		);
	}

	@Override
	public <T> GenericFunctionExpression visitGenericFunction(GenericFunctionCriteriaExpression<T> function) {
		final List<Expression> arguments;
		if ( function.getArguments() != null && !function.getArguments().isEmpty() ) {
			arguments = new ArrayList<Expression>();
			for ( CriteriaExpression<?> argument : function.getArguments() ) {
				arguments.add( argument.visitExpression( this ) );
			}
		}
		else {
			arguments = Collections.emptyList();
		}
		return new GenericFunctionExpression(
				function.getFunctionName(),
				function.getFunctionResultType(),
				arguments
		);
	}

	@Override
	public RelationalPredicate visitRelationalPredicate(ComparisonCriteriaPredicate predicate) {
		return new RelationalPredicate(
				predicate.getComparisonOperator(),
				visitExpression( predicate.getLeftHandOperand() ),
				visitExpression( predicate.getRightHandOperand() )
		);
	}

	@Override
	public NegatedPredicate visitNegatedPredicate(NegatedCriteriaPredicate predicate) {
		return new NegatedPredicate( visitPredicate( predicate.getPredicateToBeNegated() ) );
	}

	@Override
	public NullnessPredicate visitNullnessPredicate(NullnessCriteriaPredicate predicate) {
		return new NullnessPredicate( visitExpression( predicate.getOperand() ) );
	}

}
