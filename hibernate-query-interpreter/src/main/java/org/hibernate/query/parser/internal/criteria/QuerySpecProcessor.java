/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.query.parser.internal.criteria;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.persistence.Tuple;
import javax.persistence.criteria.AbstractQuery;
import javax.persistence.criteria.Fetch;
import javax.persistence.criteria.FetchParent;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Selection;
import javax.persistence.criteria.Subquery;

import org.hibernate.query.parser.ParsingException;
import org.hibernate.query.parser.QueryException;
import org.hibernate.query.parser.criteria.CriteriaVisitor;
import org.hibernate.query.parser.criteria.ExpressionImplementor;
import org.hibernate.query.parser.criteria.PredicateImplementor;
import org.hibernate.query.parser.internal.FromClauseIndex;
import org.hibernate.query.parser.internal.FromElementBuilder;
import org.hibernate.query.parser.internal.ParsingContext;
import org.hibernate.sqm.domain.AttributeDescriptor;
import org.hibernate.sqm.domain.BasicTypeDescriptor;
import org.hibernate.sqm.domain.EntityTypeDescriptor;
import org.hibernate.sqm.domain.StandardBasicTypeDescriptors;
import org.hibernate.sqm.domain.TypeDescriptor;
import org.hibernate.sqm.query.QuerySpec;
import org.hibernate.sqm.query.expression.AttributeReferenceExpression;
import org.hibernate.sqm.query.expression.AvgFunction;
import org.hibernate.sqm.query.expression.BinaryArithmeticExpression;
import org.hibernate.sqm.query.expression.CollectionIndexFunction;
import org.hibernate.sqm.query.expression.CollectionSizeFunction;
import org.hibernate.sqm.query.expression.CollectionValueFunction;
import org.hibernate.sqm.query.expression.ConcatExpression;
import org.hibernate.sqm.query.expression.ConstantEnumExpression;
import org.hibernate.sqm.query.expression.ConstantFieldExpression;
import org.hibernate.sqm.query.expression.CountFunction;
import org.hibernate.sqm.query.expression.CountStarFunction;
import org.hibernate.sqm.query.expression.EntityTypeExpression;
import org.hibernate.sqm.query.expression.Expression;
import org.hibernate.sqm.query.expression.FromElementReferenceExpression;
import org.hibernate.sqm.query.expression.FunctionExpression;
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
import org.hibernate.sqm.query.expression.MapEntryFunction;
import org.hibernate.sqm.query.expression.MapKeyFunction;
import org.hibernate.sqm.query.expression.MaxFunction;
import org.hibernate.sqm.query.expression.MinFunction;
import org.hibernate.sqm.query.expression.NamedParameterExpression;
import org.hibernate.sqm.query.expression.ParameterExpression;
import org.hibernate.sqm.query.expression.PositionalParameterExpression;
import org.hibernate.sqm.query.expression.SubQueryExpression;
import org.hibernate.sqm.query.expression.SumFunction;
import org.hibernate.sqm.query.expression.UnaryOperationExpression;
import org.hibernate.sqm.query.from.FromClause;
import org.hibernate.sqm.query.from.FromElement;
import org.hibernate.sqm.query.from.FromElementSpace;
import org.hibernate.sqm.query.from.QualifiedAttributeJoinFromElement;
import org.hibernate.sqm.query.from.RootEntityFromElement;
import org.hibernate.sqm.query.predicate.AndPredicate;
import org.hibernate.sqm.query.predicate.BetweenPredicate;
import org.hibernate.sqm.query.predicate.EmptinessPredicate;
import org.hibernate.sqm.query.predicate.InSubQueryPredicate;
import org.hibernate.sqm.query.predicate.InTupleListPredicate;
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
 * Handles processing the {@link QuerySpec} concept as modeled
 * by {@link AbstractQuery }in the JPA criteria API
 *
 * @author Steve Ebersole
 */
public class QuerySpecProcessor implements CriteriaVisitor {
	public static QuerySpecProcessor buildRootQuerySpecProcessor(ParsingContext parsingContext) {
		return new QuerySpecProcessor( parsingContext );
	}

	private final ParsingContext parsingContext;
	private final FromClauseIndex fromClauseIndex;
	private final FromElementBuilder fromElementBuilder;


	private QuerySpecProcessor(ParsingContext parsingContext) {
		this.parsingContext = parsingContext;
		this.fromClauseIndex = new FromClauseIndex();
		this.fromElementBuilder = new FromElementBuilder( parsingContext, fromClauseIndex );
	}

	public FromClauseIndex getFromClauseIndex() {
		return fromClauseIndex;
	}

	public QuerySpec visitQuerySpec(AbstractQuery jpaCriteria) {
		return new QuerySpec(
				visitFromClause( jpaCriteria ),
				visitSelectClause( jpaCriteria ),
				visitWhereClause( jpaCriteria )
		);
	}

	private FromClause visitFromClause(AbstractQuery<?> jpaCriteria) {
		final FromClause fromClause = new FromClause();
		for ( Root<?> root : jpaCriteria.getRoots() ) {
			final FromElementSpace space = fromClause.makeFromElementSpace();
			final RootEntityFromElement sqmRoot = fromElementBuilder.makeRootEntityFromElement(
					space,
					parsingContext.getConsumerContext().resolveEntityReference( root.getModel().getJavaType().getName() ),
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
			final QualifiedAttributeJoinFromElement sqmJoin = fromElementBuilder.buildAttributeJoin(
					space,
					sqmLhs,
					sqmLhs.getTypeDescriptor().getAttributeDescriptor( join.getAttribute().getName() ),
					join.getAlias(),
					convert( join.getJoinType() ),
					false
			);
			space.addJoin( sqmJoin );
			bindJoins( join, sqmJoin, space );
		}
	}

	private void bindFetches(FetchParent<?, ?> lhs, FromElement sqmLhs, FromElementSpace space) {
		for ( Fetch<?, ?> fetch : lhs.getFetches() ) {
			final QualifiedAttributeJoinFromElement sqmFetch = fromElementBuilder.buildAttributeJoin(
					space,
					sqmLhs,
					sqmLhs.getTypeDescriptor().getAttributeDescriptor( fetch.getAttribute().getName() ),
					parsingContext.getImplicitAliasGenerator().buildUniqueImplicitAlias(),
					convert( fetch.getJoinType() ),
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

	@Override
	public SelectClause visitSelectClause(AbstractQuery jpaCriteria) {
		final SelectClause sqmSelectClause = new SelectClause( jpaCriteria.isDistinct() );

		applySelection( jpaCriteria.getSelection(), sqmSelectClause );

		return sqmSelectClause;
	}

	private void applySelection(Selection<?> selection, AliasedExpressionContainer container) {
		if ( selection.isCompoundSelection() ) {
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
		else if ( selection instanceof Expression ) {
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
								"Unexpected JPA Criteria query Selection type [%s] encountered; " +
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

	protected Expression visitExpression(javax.persistence.criteria.Expression<?> expression) {
		return ( (ExpressionImplementor<?>) expression ).accept( this );
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> LiteralExpression<T> visitLiteral(T value) {
		if ( value == null ) {
			return (LiteralExpression<T>) new LiteralNullExpression();
		}

		return visitLiteral(
				value,
				StandardBasicTypeDescriptors.INSTANCE.standardDescriptorForType( value.getClass() )
		);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> LiteralExpression<T> visitLiteral(T value, BasicTypeDescriptor typeDescriptor) {
		assert typeDescriptor != null : "BasicTypeDescriptor passed cannot be null";

		if ( value == null ) {
			return (LiteralExpression<T>) new LiteralNullExpression();
		}

		if ( Boolean.class.isAssignableFrom( typeDescriptor.getCorrespondingJavaType() ) ) {
			if ( (Boolean) value ) {
				return (LiteralExpression<T>) new LiteralTrueExpression( typeDescriptor );
			}
			else {
				return (LiteralExpression<T>) new LiteralFalseExpression( typeDescriptor );
			}
		}
		else if ( Integer.class.isAssignableFrom( typeDescriptor.getCorrespondingJavaType() ) ) {
			return (LiteralExpression<T>) new LiteralIntegerExpression( (Integer) value, typeDescriptor );
		}
		else if ( Long.class.isAssignableFrom( typeDescriptor.getCorrespondingJavaType() ) ) {
			return (LiteralExpression<T>) new LiteralLongExpression( (Long) value, typeDescriptor );
		}
		else if ( Float.class.isAssignableFrom( typeDescriptor.getCorrespondingJavaType() ) ) {
			return (LiteralExpression<T>) new LiteralFloatExpression( (Float) value, typeDescriptor );
		}
		else if ( Double.class.isAssignableFrom( typeDescriptor.getCorrespondingJavaType() ) ) {
			return (LiteralExpression<T>) new LiteralDoubleExpression( (Double) value, typeDescriptor );
		}
		else if ( BigInteger.class.isAssignableFrom( typeDescriptor.getCorrespondingJavaType() ) ) {
			return (LiteralExpression<T>) new LiteralBigIntegerExpression( (BigInteger) value, typeDescriptor );
		}
		else if ( BigDecimal.class.isAssignableFrom( typeDescriptor.getCorrespondingJavaType() ) ) {
			return (LiteralExpression<T>) new LiteralBigDecimalExpression( (BigDecimal) value, typeDescriptor );
		}
		else if ( Character.class.isAssignableFrom( typeDescriptor.getCorrespondingJavaType() ) ) {
			return (LiteralExpression<T>) new LiteralCharacterExpression( (Character) value, typeDescriptor );
		}
		else if ( String.class.isAssignableFrom( typeDescriptor.getCorrespondingJavaType() ) ) {
			return (LiteralExpression<T>) new LiteralStringExpression( (String) value, typeDescriptor );
		}

		throw new QueryException(
				"Unexpected literal expression [value=" + value +
						", javaType=" + typeDescriptor.getCorrespondingJavaType().getName() +
						"]; expecting boolean, int, long, float, double, BigInteger, BigDecimal, char, or String"
		);
	}

	@Override
	public <T extends Enum> ConstantEnumExpression<T> visitEnumConstant(T value) {
		return new ConstantEnumExpression<T>( value );
	}

	@Override
	public <T> ConstantFieldExpression<T> visitConstant(T value) {
		return new ConstantFieldExpression<T>( value );
	}

	@Override
	public <T> ConstantFieldExpression<T> visitConstant(T value, TypeDescriptor typeDescriptor) {
		return new ConstantFieldExpression<T>( value, typeDescriptor );
	}

	@Override
	public ParameterExpression visitParameter(javax.persistence.criteria.ParameterExpression param) {
		TypeDescriptor typeDescriptor = null;
		if ( param.getJavaType() != null ) {
			typeDescriptor = StandardBasicTypeDescriptors.INSTANCE.standardDescriptorForType( param.getJavaType() );
		}

		return visitParameter( param, typeDescriptor );
	}

	@Override
	public ParameterExpression visitParameter(
			javax.persistence.criteria.ParameterExpression param,
			TypeDescriptor typeDescriptor) {
		if ( isNotEmpty( param.getName() ) ) {
			return new NamedParameterExpression( param.getName(), typeDescriptor );
		}
		else if ( param.getPosition() != null ) {
			return new PositionalParameterExpression( param.getPosition(), typeDescriptor );
		}

		throw new QueryException( "ParameterExpression did not define name nor position" );
	}

	@Override
	public UnaryOperationExpression visitUnaryOperation(
			javax.persistence.criteria.Expression expression,
			UnaryOperationExpression.Operation operation) {
		return new UnaryOperationExpression( operation, visitExpression( expression ) );
	}

	@Override
	public BinaryArithmeticExpression visitArithmetic(
			javax.persistence.criteria.Expression expression1,
			BinaryArithmeticExpression.Operation operation,
			javax.persistence.criteria.Expression expression2) {
		return new BinaryArithmeticExpression(
				operation,
				visitExpression( expression1 ),
				visitExpression( expression2 )
		);
	}

	@Override
	public FromElementReferenceExpression visitIdentificationVariableReference(From reference) {
		return new FromElementReferenceExpression( fromClauseIndex.findFromElementByAlias( reference.getAlias() ) );
	}

	@Override
	public AttributeReferenceExpression visitAttributeReference(From attributeSource, String attributeName) {
		final FromElement source = fromClauseIndex.findFromElementByAlias( attributeSource.getAlias() );
		final AttributeDescriptor attributeDescriptor = source.getTypeDescriptor().getAttributeDescriptor( attributeName );
		return new AttributeReferenceExpression( source, attributeDescriptor );
	}

	@Override
	public FunctionExpression visitFunction(
			String name,
			TypeDescriptor resultTypeDescriptor,
			List<javax.persistence.criteria.Expression> expressions) {
		final List<Expression> sqmExpressions = new ArrayList<Expression>();
		for ( javax.persistence.criteria.Expression expression : expressions ) {
			sqmExpressions.add( visitExpression( expression ) );
		}

		return new FunctionExpression( name, sqmExpressions, resultTypeDescriptor );
	}

	@Override
	public AvgFunction visitAvgFunction(javax.persistence.criteria.Expression expression, boolean distinct) {
		return new AvgFunction( visitExpression( expression ), distinct );
	}

	@Override
	public CountFunction visitCountFunction(javax.persistence.criteria.Expression expression, boolean distinct) {
		return new CountFunction( visitExpression( expression ), distinct );
	}

	@Override
	public CountStarFunction visitCountStarFunction(boolean distinct) {
		return new CountStarFunction( distinct );
	}

	@Override
	public MaxFunction visitMaxFunction(javax.persistence.criteria.Expression expression, boolean distinct) {
		return new MaxFunction( visitExpression( expression ), distinct );
	}

	@Override
	public MinFunction visitMinFunction(javax.persistence.criteria.Expression expression, boolean distinct) {
		return new MinFunction( visitExpression( expression ), distinct );
	}

	@Override
	public SumFunction visitSumFunction(javax.persistence.criteria.Expression expression, boolean distinct) {
		return new SumFunction( visitExpression( expression ), distinct );
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
	public EntityTypeExpression visitEntityType(String identificationVariable) {
		final FromElement fromElement = fromClauseIndex.findFromElementByAlias( identificationVariable );
		return new EntityTypeExpression( (EntityTypeDescriptor) fromElement.getTypeDescriptor() );
	}

	@Override
	public EntityTypeExpression visitEntityType(String identificationVariable, String attributeName) {
		final FromElement fromElement = fromClauseIndex.findFromElementByAlias( identificationVariable );
		return new EntityTypeExpression( (EntityTypeDescriptor) fromElement.getTypeDescriptor().getAttributeDescriptor(
				attributeName
		) );
	}

	@Override
	public SubQueryExpression visitSubQuery(Subquery subquery) {
		return new SubQueryExpression( visitQuerySpec( subquery ) );
	}

	private WhereClause visitWhereClause(AbstractQuery<?> jpaCriteria) {
		final WhereClause whereClause = new WhereClause();
		whereClause.setPredicate( visitPredicate( jpaCriteria.getRestriction() ) );
		return whereClause;
	}

	protected Predicate visitPredicate(javax.persistence.criteria.Predicate predicate) {
		return ( (PredicateImplementor) predicate ).accept( this );
	}

	protected Predicate visitPredicate(javax.persistence.criteria.Expression<Boolean> predicate) {
		return ( (PredicateImplementor) predicate ).accept( this );
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
	public NegatedPredicate visitPredicateNegation(javax.persistence.criteria.Expression<Boolean> expression) {
		return new NegatedPredicate( visitPredicate( expression ) );
	}

	@Override
	public NullnessPredicate visitNullnessPredicate(javax.persistence.criteria.Expression expression, boolean negated) {
		return new NullnessPredicate( visitExpression( expression ), negated );
	}

	@Override
	public EmptinessPredicate visitEmptinessPredicate(From attributeSource, String attributeName, boolean negated) {
		final AttributeReferenceExpression attributeReference = visitAttributeReference( attributeSource, attributeName );
		return new EmptinessPredicate( attributeReference, negated );
	}

	@Override
	public MemberOfPredicate visitMemberOfPredicate(From attributeSource, String attributeName, boolean negated) {
		return null;
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
	public RelationalPredicate visitRelationalPredicate(
			javax.persistence.criteria.Expression expression1,
			RelationalPredicate.Type type,
			javax.persistence.criteria.Expression expression2) {
		return new RelationalPredicate(
				type,
				visitExpression( expression1 ),
				visitExpression( expression2 )
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
	public InTupleListPredicate visitInTupleListPredicate(
			javax.persistence.criteria.Expression testExpression,
			List<javax.persistence.criteria.Expression> expressionsList,
			boolean negated) {
		final List<Expression> expressions = new ArrayList<Expression>();
		for ( javax.persistence.criteria.Expression expression : expressionsList ) {
			expressions.add( visitExpression( expression ) );
		}

		return new InTupleListPredicate(
				visitExpression( testExpression ),
				expressions,
				negated
		);
	}
}
