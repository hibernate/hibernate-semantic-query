/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.query.parser.internal.hql;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.hibernate.sqm.domain.BasicType;

import org.hibernate.query.parser.LiteralNumberFormatException;
import org.hibernate.query.parser.ParsingException;
import org.hibernate.query.parser.SemanticException;
import org.hibernate.query.parser.StrictJpaComplianceViolation;
import org.hibernate.query.parser.internal.ExpressionTypeHelper;
import org.hibernate.query.parser.internal.FromClauseIndex;
import org.hibernate.query.parser.internal.FromElementBuilder;
import org.hibernate.query.parser.internal.ParsingContext;
import org.hibernate.query.parser.internal.hql.antlr.HqlParser;
import org.hibernate.query.parser.internal.hql.antlr.HqlParser.GroupByClauseContext;
import org.hibernate.query.parser.internal.hql.antlr.HqlParser.HavingClauseContext;
import org.hibernate.query.parser.internal.hql.antlr.HqlParserBaseVisitor;
import org.hibernate.query.parser.internal.hql.path.AttributePathResolver;
import org.hibernate.query.parser.internal.hql.path.AttributePathResolverStack;
import org.hibernate.query.parser.internal.hql.path.IndexedAttributeRootPathResolver;
import org.hibernate.query.parser.internal.hql.phase1.FromClauseStackNode;
import org.hibernate.sqm.domain.EntityType;
import org.hibernate.sqm.domain.PluralAttribute;
import org.hibernate.sqm.domain.Type;
import org.hibernate.sqm.path.AttributePathPart;
import org.hibernate.sqm.query.QuerySpec;
import org.hibernate.sqm.query.SelectStatement;
import org.hibernate.sqm.query.expression.AggregateFunction;
import org.hibernate.sqm.query.expression.AttributeReferenceExpression;
import org.hibernate.sqm.query.expression.AvgFunction;
import org.hibernate.sqm.query.expression.BinaryArithmeticExpression;
import org.hibernate.sqm.query.expression.CollectionIndexFunction;
import org.hibernate.sqm.query.expression.CollectionSizeFunction;
import org.hibernate.sqm.query.expression.CollectionValueFunction;
import org.hibernate.sqm.query.expression.ConcatExpression;
import org.hibernate.sqm.query.expression.ConstantEnumExpression;
import org.hibernate.sqm.query.expression.ConstantExpression;
import org.hibernate.sqm.query.expression.ConstantFieldExpression;
import org.hibernate.sqm.query.expression.CountFunction;
import org.hibernate.sqm.query.expression.CountStarFunction;
import org.hibernate.sqm.query.expression.EntityTypeExpression;
import org.hibernate.sqm.query.expression.Expression;
import org.hibernate.sqm.query.expression.FromElementReferenceExpression;
import org.hibernate.sqm.query.expression.FunctionExpression;
import org.hibernate.sqm.query.expression.ImpliedTypeExpression;
import org.hibernate.sqm.query.expression.IndexedAttributePathPart;
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
import org.hibernate.sqm.query.expression.MaxElementFunction;
import org.hibernate.sqm.query.expression.MaxFunction;
import org.hibernate.sqm.query.expression.MaxIndexFunction;
import org.hibernate.sqm.query.expression.MinElementFunction;
import org.hibernate.sqm.query.expression.MinFunction;
import org.hibernate.sqm.query.expression.MinIndexFunction;
import org.hibernate.sqm.query.expression.NamedParameterExpression;
import org.hibernate.sqm.query.expression.PositionalParameterExpression;
import org.hibernate.sqm.query.expression.SubQueryExpression;
import org.hibernate.sqm.query.expression.SumFunction;
import org.hibernate.sqm.query.expression.UnaryOperationExpression;
import org.hibernate.sqm.query.from.FromClause;
import org.hibernate.sqm.query.from.FromElement;
import org.hibernate.sqm.query.from.JoinedFromElement;
import org.hibernate.sqm.query.from.TreatedFromElement;
import org.hibernate.sqm.query.from.TreatedJoinedFromElement;
import org.hibernate.sqm.query.order.OrderByClause;
import org.hibernate.sqm.query.order.SortOrder;
import org.hibernate.sqm.query.order.SortSpecification;
import org.hibernate.sqm.query.predicate.AndPredicate;
import org.hibernate.sqm.query.predicate.BetweenPredicate;
import org.hibernate.sqm.query.predicate.EmptinessPredicate;
import org.hibernate.sqm.query.predicate.GroupedPredicate;
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
import org.hibernate.sqm.query.select.DynamicInstantiation;
import org.hibernate.sqm.query.select.DynamicInstantiationArgument;
import org.hibernate.sqm.query.select.SelectClause;
import org.hibernate.sqm.query.select.Selection;

import org.jboss.logging.Logger;

import org.antlr.v4.runtime.tree.TerminalNode;

import static org.hibernate.query.parser.StrictJpaComplianceViolation.Type.HQL_COLLECTION_FUNCTION;

/**
 * @author Steve Ebersole
 */
public abstract class AbstractHqlParseTreeVisitor extends HqlParserBaseVisitor {
	private static final Logger log = Logger.getLogger( AbstractHqlParseTreeVisitor.class );

	private final ParsingContext parsingContext;
	private final FromClauseIndex fromClauseIndex;

	/**
	 * Whether the currently processed clause is WHERE or not
	 */
	private boolean inWhereClause;

	protected final AttributePathResolverStack attributePathResolverStack = new AttributePathResolverStack();

	public AbstractHqlParseTreeVisitor(
			ParsingContext parsingContext,
			FromClauseIndex fromClauseIndex) {
		this.parsingContext = parsingContext;
		this.fromClauseIndex = fromClauseIndex;
	}

	public abstract FromClause getCurrentFromClause();

	public abstract FromElementBuilder getFromElementBuilder();

	public abstract FromClauseStackNode getCurrentFromClauseNode();

	public ParsingContext getParsingContext() {
		return parsingContext;
	}

	public AttributePathResolver getCurrentAttributePathResolver() {
		return attributePathResolverStack.getCurrent();
	}

	@Override
	public SelectStatement visitSelectStatement(HqlParser.SelectStatementContext ctx) {
		final SelectStatement selectStatement = new SelectStatement();
		selectStatement.applyQuerySpec( visitQuerySpec( ctx.querySpec() ) );
		if ( ctx.orderByClause() != null ) {
			selectStatement.applyOrderByClause( visitOrderByClause( ctx.orderByClause() ) );
		}

		return selectStatement;
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

	@Override
	public QuerySpec visitQuerySpec(HqlParser.QuerySpecContext ctx) {
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

	protected SelectClause buildInferredSelectClause(FromClause fromClause) {
		// for now, this is slightly different than the legacy behavior where
		// the root and each non-fetched-join was selected.  For now, here, we simply
		// select the root
		final SelectClause selectClause = new SelectClause( true );
		final FromElement root = fromClause.getFromElementSpaces().get( 0 ).getRoot();
		selectClause.addSelection(
				new Selection(
						new FromElementReferenceExpression(
								root,
								root.getBindableModelDescriptor().getBoundType()
						)
				)
		);
		return selectClause;
	}

	@Override
	public SelectClause visitSelectClause(HqlParser.SelectClauseContext ctx) {
		final SelectClause selectClause = new SelectClause( ctx.distinctKeyword() != null );
		for ( HqlParser.SelectionContext selectionContext : ctx.selectionList().selection() ) {
			selectClause.addSelection( visitSelection( selectionContext ) );
		}
		return selectClause;
	}

	@Override
	public Selection visitSelection(HqlParser.SelectionContext ctx) {
		final Selection selection = new Selection(
				visitSelectExpression( ctx.selectExpression() ),
				interpretAlias( ctx.IDENTIFIER() )
		);
		getFromElementBuilder().getAliasRegistry().registerAlias( selection );
		return selection;
	}

	private String interpretAlias(TerminalNode aliasNode) {
		if ( aliasNode == null ) {
			return null;
		}
		final String aliasText = aliasNode.getText();
		return aliasText;
	}

	@Override
	public Expression visitSelectExpression(HqlParser.SelectExpressionContext ctx) {
		if ( ctx.dynamicInstantiation() != null ) {
			return visitDynamicInstantiation( ctx.dynamicInstantiation() );
		}
		else if ( ctx.jpaSelectObjectSyntax() != null ) {
			return visitJpaSelectObjectSyntax( ctx.jpaSelectObjectSyntax() );
		}
		else if ( ctx.expression() != null ) {
			return (Expression) ctx.expression().accept( this );
		}

		throw new ParsingException( "Unexpected selection rule type : " + ctx.getText() );
	}

	@Override
	public DynamicInstantiation visitDynamicInstantiation(HqlParser.DynamicInstantiationContext ctx) {
		final DynamicInstantiation dynamicInstantiation;

		if ( ctx.dynamicInstantiationTarget().mapKeyword() != null ) {
			final BasicType<Map> mapType = parsingContext.getConsumerContext().getDomainMetamodel().getBasicType( Map.class );
			dynamicInstantiation = DynamicInstantiation.forMapInstantiation( mapType );
		}
		else if ( ctx.dynamicInstantiationTarget().listKeyword() != null ) {
			final BasicType<List> listType = parsingContext.getConsumerContext().getDomainMetamodel().getBasicType( List.class );
			dynamicInstantiation = DynamicInstantiation.forListInstantiation( listType );
		}
		else {
			final String className = ctx.dynamicInstantiationTarget().dotIdentifierSequence().getText();
			try {
				final BasicType instantiationType = parsingContext.getConsumerContext().getDomainMetamodel().getBasicType(
						parsingContext.getConsumerContext().classByName( className )
				);

				dynamicInstantiation = DynamicInstantiation.forClassInstantiation( instantiationType );
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
	public DynamicInstantiationArgument visitDynamicInstantiationArg(HqlParser.DynamicInstantiationArgContext ctx) {
		return new DynamicInstantiationArgument(
				visitDynamicInstantiationArgExpression( ctx.dynamicInstantiationArgExpression() ),
				ctx.IDENTIFIER() == null ? null : ctx.IDENTIFIER().getText()
		);
	}

	@Override
	public Expression visitDynamicInstantiationArgExpression(HqlParser.DynamicInstantiationArgExpressionContext ctx) {
		if ( ctx.dynamicInstantiation() != null ) {
			return visitDynamicInstantiation( ctx.dynamicInstantiation() );
		}
		else if ( ctx.expression() != null ) {
			return (Expression) ctx.expression().accept( this );
		}

		throw new ParsingException( "Unexpected dynamic-instantiation-argument rule type : " + ctx.getText() );
	}

	@Override
	public FromElementReferenceExpression visitJpaSelectObjectSyntax(HqlParser.JpaSelectObjectSyntaxContext ctx) {
		final String alias = ctx.IDENTIFIER().getText();
		final FromElement fromElement = getFromElementBuilder().getAliasRegistry().findFromElementByAlias( alias );
		if ( fromElement == null ) {
			throw new SemanticException( "Unable to resolve alias [" +  alias + "] in selection [" + ctx.getText() + "]" );
		}
		return new FromElementReferenceExpression( fromElement, fromElement.getBindableModelDescriptor().getBoundType() );
	}

	@Override
	public Predicate visitQualifiedJoinPredicate(HqlParser.QualifiedJoinPredicateContext ctx) {
		return (Predicate) ctx.predicate().accept( this );
	}

	@Override
	public WhereClause visitWhereClause(HqlParser.WhereClauseContext ctx) {
		inWhereClause = true;

		try {
			return new WhereClause( (Predicate) ctx.predicate().accept( this ) );
		}
		finally {
			inWhereClause = false;
		}
	}

	@Override
	public Object visitGroupByClause(GroupByClauseContext ctx) {
		return super.visitGroupByClause( ctx );
	}

	@Override
	public Object visitHavingClause(HavingClauseContext ctx) {
		return super.visitHavingClause( ctx );
	}

	@Override
	public GroupedPredicate visitGroupedPredicate(HqlParser.GroupedPredicateContext ctx) {
		return new GroupedPredicate( (Predicate) ctx.predicate().accept( this ) );
	}

	@Override
	public AndPredicate visitAndPredicate(HqlParser.AndPredicateContext ctx) {
		return new AndPredicate(
				(Predicate) ctx.predicate( 0 ).accept( this ),
				(Predicate) ctx.predicate( 1 ).accept( this )
		);
	}

	@Override
	public OrPredicate visitOrPredicate(HqlParser.OrPredicateContext ctx) {
		return new OrPredicate(
				(Predicate) ctx.predicate( 0 ).accept( this ),
				(Predicate) ctx.predicate( 1 ).accept( this )
		);
	}

	@Override
	public NegatedPredicate visitNegatedPredicate(HqlParser.NegatedPredicateContext ctx) {
		return new NegatedPredicate( (Predicate) ctx.predicate().accept( this ) );
	}

	@Override
	public NullnessPredicate visitIsNullPredicate(HqlParser.IsNullPredicateContext ctx) {
		return new NullnessPredicate( (Expression) ctx.expression().accept( this ) );
	}

	@Override
	public EmptinessPredicate visitIsEmptyPredicate(HqlParser.IsEmptyPredicateContext ctx) {
		return new EmptinessPredicate( (Expression) ctx.expression().accept( this ) );
	}

	@Override
	public Object visitEqualityPredicate(HqlParser.EqualityPredicateContext ctx) {
		return new RelationalPredicate(
				RelationalPredicate.Type.EQUAL,
				(Expression) ctx.expression().get( 0 ).accept( this ),
				(Expression) ctx.expression().get( 1 ).accept( this )
		);
	}

	@Override
	public Object visitInequalityPredicate(HqlParser.InequalityPredicateContext ctx) {
		return new RelationalPredicate(
				RelationalPredicate.Type.NOT_EQUAL,
				(Expression) ctx.expression().get( 0 ).accept( this ),
				(Expression) ctx.expression().get( 1 ).accept( this )
		);
	}

	@Override
	public Object visitGreaterThanPredicate(HqlParser.GreaterThanPredicateContext ctx) {
		return new RelationalPredicate(
				RelationalPredicate.Type.GT,
				(Expression) ctx.expression().get( 0 ).accept( this ),
				(Expression) ctx.expression().get( 1 ).accept( this )
		);
	}

	@Override
	public Object visitGreaterThanOrEqualPredicate(HqlParser.GreaterThanOrEqualPredicateContext ctx) {
		return new RelationalPredicate(
				RelationalPredicate.Type.GE,
				(Expression) ctx.expression().get( 0 ).accept( this ),
				(Expression) ctx.expression().get( 1 ).accept( this )
		);
	}

	@Override
	public Object visitLessThanPredicate(HqlParser.LessThanPredicateContext ctx) {
		return new RelationalPredicate(
				RelationalPredicate.Type.LT,
				(Expression) ctx.expression().get( 0 ).accept( this ),
				(Expression) ctx.expression().get( 1 ).accept( this )
		);
	}

	@Override
	public Object visitLessThanOrEqualPredicate(HqlParser.LessThanOrEqualPredicateContext ctx) {
		return new RelationalPredicate(
				RelationalPredicate.Type.LE,
				(Expression) ctx.expression().get( 0 ).accept( this ),
				(Expression) ctx.expression().get( 1 ).accept( this )
		);
	}

	@Override
	public Object visitBetweenPredicate(HqlParser.BetweenPredicateContext ctx) {
		final Expression expression = (Expression) ctx.expression().get( 0 ).accept( this );
		final Expression lowerBound = (Expression) ctx.expression().get( 1 ).accept( this );
		final Expression upperBound = (Expression) ctx.expression().get( 2 ).accept( this );

		if ( expression.getInferableType() != null ) {
			if ( lowerBound instanceof ImpliedTypeExpression ) {
				( (ImpliedTypeExpression) lowerBound ).impliedType( expression.getInferableType() );
			}
			if ( upperBound instanceof ImpliedTypeExpression ) {
				( (ImpliedTypeExpression) upperBound ).impliedType( expression.getInferableType() );
			}
		}
		else if ( lowerBound.getInferableType() != null ) {
			if ( expression instanceof ImpliedTypeExpression ) {
				( (ImpliedTypeExpression) expression ).impliedType( lowerBound.getInferableType() );
			}
			if ( upperBound instanceof ImpliedTypeExpression ) {
				( (ImpliedTypeExpression) upperBound ).impliedType( lowerBound.getInferableType() );
			}
		}
		else if ( upperBound.getInferableType() != null ) {
			if ( expression instanceof ImpliedTypeExpression ) {
				( (ImpliedTypeExpression) expression ).impliedType( upperBound.getInferableType() );
			}
			if ( lowerBound instanceof ImpliedTypeExpression ) {
				( (ImpliedTypeExpression) lowerBound ).impliedType( upperBound.getInferableType() );
			}
		}

		return new BetweenPredicate(
				expression,
				lowerBound,
				upperBound,
				false
		);
	}

	@Override
	public Object visitLikePredicate(HqlParser.LikePredicateContext ctx) {
		if ( ctx.likeEscape() != null ) {
			return new LikePredicate(
					(Expression) ctx.expression().get( 0 ).accept( this ),
					(Expression) ctx.expression().get( 1 ).accept( this ),
					(Expression) ctx.likeEscape().expression().accept( this )
			);
		}
		else {
			return new LikePredicate(
					(Expression) ctx.expression().get( 0 ).accept( this ),
					(Expression) ctx.expression().get( 1 ).accept( this )
			);
		}
	}

	@Override
	public Object visitMemberOfPredicate(HqlParser.MemberOfPredicateContext ctx) {
		final Object pathResolution = ctx.path().accept( this );
		if ( !AttributeReferenceExpression.class.isInstance( pathResolution ) ) {
			throw new SemanticException( "Could not resolve path [" + ctx.path().getText() + "] as an attribute reference" );
		}
		final AttributeReferenceExpression attributeReference = (AttributeReferenceExpression) pathResolution;
		if ( !PluralAttribute.class.isInstance( attributeReference.getAttributeDescriptor() ) ) {
			throw new SemanticException( "Path argument to MEMBER OF must be a collection" );
		}
		return new MemberOfPredicate( attributeReference );
	}

	@Override
	public Object visitInPredicate(HqlParser.InPredicateContext ctx) {
		if ( HqlParser.ExplicitTupleInListContext.class.isInstance( ctx.inList() ) ) {
			final HqlParser.ExplicitTupleInListContext tupleExpressionListContext = (HqlParser.ExplicitTupleInListContext) ctx.inList();
			final List<Expression> tupleExpressions = new ArrayList<Expression>( tupleExpressionListContext.expression().size() );
			for ( HqlParser.ExpressionContext expressionContext : tupleExpressionListContext.expression() ) {
				tupleExpressions.add( (Expression) expressionContext.accept( this ) );
			}

			return new InTupleListPredicate(
					(Expression) ctx.expression().accept( this ),
					tupleExpressions
			);
		}
		else if ( HqlParser.SubQueryInListContext.class.isInstance( ctx.inList() ) ) {
			final HqlParser.SubQueryInListContext subQueryContext = (HqlParser.SubQueryInListContext) ctx.inList();
			final Expression subQueryExpression = (Expression) subQueryContext.expression().accept( this );

			if ( !SubQueryExpression.class.isInstance( subQueryExpression ) ) {
				throw new ParsingException(
						"Was expecting a SubQueryExpression, but found " + subQueryExpression.getClass().getSimpleName()
								+ " : " + subQueryContext.expression().toString()
				);
			}

			return new InSubQueryPredicate(
					(Expression) ctx.expression().accept( this ),
					(SubQueryExpression) subQueryExpression
			);
		}

		// todo : handle PersistentCollectionReferenceInList labeled branch

		throw new ParsingException( "Unexpected IN predicate type [" + ctx.getClass().getSimpleName() + "] : " + ctx.getText() );
	}

	@Override
	public Object visitSimplePath(HqlParser.SimplePathContext ctx) {
		final AttributePathPart attributePathPart = getCurrentAttributePathResolver().resolvePath( ctx.dotIdentifierSequence() );
		if ( attributePathPart != null ) {
			return attributePathPart;
		}

		final String pathText = ctx.getText();

		try {
			final EntityType entityType = parsingContext.getConsumerContext().getDomainMetamodel().resolveEntityType( pathText );
			if ( entityType != null ) {
				return new EntityTypeExpression( entityType );
			}
		}
		catch (IllegalArgumentException ignore) {
		}

		// 5th level precedence : constant reference
		try {
			return resolveConstantExpression( pathText );
		}
		catch (SemanticException e) {
			log.debug( e.getMessage() );
		}

		// if we get here we had a problem interpreting the dot-ident sequence
		throw new SemanticException( "Could not interpret token : " + pathText );
	}

	@SuppressWarnings("unchecked")
	protected ConstantExpression resolveConstantExpression(String reference) {
		// todo : hook in "import" resolution using the ParsingContext
		final int dotPosition = reference.lastIndexOf( '.' );
		final String className = reference.substring( 0, dotPosition - 1 );
		final String fieldName = reference.substring( dotPosition+1, reference.length() );

		try {
			final Class clazz = parsingContext.getConsumerContext().classByName( className );
			if ( clazz.isEnum() ) {
				try {
					return new ConstantEnumExpression(
							Enum.valueOf( clazz, fieldName ),
							parsingContext.getConsumerContext().getDomainMetamodel().getBasicType( clazz )
					);
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
					return new ConstantFieldExpression(
							field.get( null ),
							parsingContext.getConsumerContext().getDomainMetamodel().getBasicType( field.getType() )
					);
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
			throw new SemanticException( "Cannot resolve class for query constant [" + reference + "]" );
		}
	}

	@Override
	public AttributePathPart visitTreatedPath(HqlParser.TreatedPathContext ctx) {
		final FromElement fromElement = (FromElement) getCurrentAttributePathResolver().resolvePath( ctx.dotIdentifierSequence().get( 0 ) );
		if ( fromElement == null ) {
			throw new SemanticException( "Could not resolve path [" + ctx.dotIdentifierSequence().get( 0 ).getText() + "] as base for TREAT-AS expression" );
		}

		final String treatAsName = ctx.dotIdentifierSequence().get( 1 ).getText();
		final EntityType treatAsTypeDescriptor = parsingContext.getConsumerContext()
				.getDomainMetamodel()
				.resolveEntityType( treatAsName );
		if ( treatAsTypeDescriptor == null ) {
			throw new SemanticException( "TREAT-AS target type [" + treatAsName + "] did not reference an entity" );
		}

		fromElement.addTreatedAs( treatAsTypeDescriptor );

		if ( fromElement instanceof JoinedFromElement ) {
			return new TreatedJoinedFromElement( (JoinedFromElement) fromElement, treatAsTypeDescriptor );
		}
		else {
			return new TreatedFromElement( fromElement, treatAsTypeDescriptor );
		}
	}

	@Override
	public AttributePathPart visitIndexedPath(HqlParser.IndexedPathContext ctx) {
		if ( ctx.path().size() > 2 ) {
			throw new ParsingException( "Encountered unexpected number of path expressions in indexed path reference : " + ctx.getText() );
		}

		final AttributePathPart indexSource = (AttributePathPart) ctx.path( 0 ).accept( this );
		final Expression indexExpression = (Expression) ctx.expression().accept( this );

		// the source TypeDescriptor needs to be an indexed collection for this to be valid...
		if ( !PluralAttribute.class.isInstance( indexSource.getBindableModelDescriptor() ) ) {
			throw new SemanticException( "Index operator only valid for indexed collections (maps, lists, arrays) : " + indexSource );
		}

		final PluralAttribute pluralAttribute = (PluralAttribute) indexSource.getBindableModelDescriptor();
		// todo : would be nice to validate the index's type against the Collection-index's type
		// 		but that requires "compatible type checking" rather than TypeDescriptor sameness (long versus int, e.g)

		final IndexedAttributePathPart indexedReference = new IndexedAttributePathPart(
				indexSource,
				indexExpression,
				// Ultimately the Type for this part is the same as the elements of the collection...
				pluralAttribute.getElementType()
		);

		if ( ctx.path( 1 ) == null ) {
			return indexedReference;
		}


		// we have a de-reference of the indexed reference.  push a new path resolver
		// that handles the indexed reference as the root to the path
		attributePathResolverStack.push(
				new IndexedAttributeRootPathResolver(
						getFromElementBuilder(),
						parsingContext,
						indexedReference
				)
		);
		try {
			return (AttributePathPart) ctx.path( 1 ).accept( this );
		}
		finally {
			attributePathResolverStack.pop();
		}
	}

	@Override
	public ConcatExpression visitConcatenationExpression(HqlParser.ConcatenationExpressionContext ctx) {
		if ( ctx.expression().size() != 2 ) {
			throw new ParsingException( "Expecting 2 operands to the concat operator" );
		}
		return new ConcatExpression(
				(Expression) ctx.expression( 0 ).accept( this ),
				(Expression) ctx.expression( 1 ).accept( this )
		);
	}

	@Override
	public Object visitAdditionExpression(HqlParser.AdditionExpressionContext ctx) {
		if ( ctx.expression().size() != 2 ) {
			throw new ParsingException( "Expecting 2 operands to the + operator" );
		}

		final Expression firstOperand = (Expression) ctx.expression( 0 ).accept( this );
		final Expression secondOperand = (Expression) ctx.expression( 1 ).accept( this );
		return new BinaryArithmeticExpression(
				BinaryArithmeticExpression.Operation.ADD,
				firstOperand,
				secondOperand,
				ExpressionTypeHelper.resolveArithmeticType(
						(BasicType) firstOperand.getExpressionType(),
						(BasicType) secondOperand.getExpressionType(),
						parsingContext.getConsumerContext(),
						false
				)
		);
	}

	@Override
	public Object visitSubtractionExpression(HqlParser.SubtractionExpressionContext ctx) {
		if ( ctx.expression().size() != 2 ) {
			throw new ParsingException( "Expecting 2 operands to the - operator" );
		}

		final Expression firstOperand = (Expression) ctx.expression( 0 ).accept( this );
		final Expression secondOperand = (Expression) ctx.expression( 1 ).accept( this );
		return new BinaryArithmeticExpression(
				BinaryArithmeticExpression.Operation.SUBTRACT,
				firstOperand,
				secondOperand,
				ExpressionTypeHelper.resolveArithmeticType(
						(BasicType) firstOperand.getExpressionType(),
						(BasicType) secondOperand.getExpressionType(),
						parsingContext.getConsumerContext(),
						false
				)
		);
	}

	@Override
	public Object visitMultiplicationExpression(HqlParser.MultiplicationExpressionContext ctx) {
		if ( ctx.expression().size() != 2 ) {
			throw new ParsingException( "Expecting 2 operands to the * operator" );
		}

		final Expression firstOperand = (Expression) ctx.expression( 0 ).accept( this );
		final Expression secondOperand = (Expression) ctx.expression( 1 ).accept( this );
		return new BinaryArithmeticExpression(
				BinaryArithmeticExpression.Operation.MULTIPLY,
				firstOperand,
				secondOperand,
				ExpressionTypeHelper.resolveArithmeticType(
						(BasicType) firstOperand.getExpressionType(),
						(BasicType) secondOperand.getExpressionType(),
						parsingContext.getConsumerContext(),
						false
				)
		);
	}

	@Override
	public Object visitDivisionExpression(HqlParser.DivisionExpressionContext ctx) {
		if ( ctx.expression().size() != 2 ) {
			throw new ParsingException( "Expecting 2 operands to the / operator" );
		}

		final Expression firstOperand = (Expression) ctx.expression( 0 ).accept( this );
		final Expression secondOperand = (Expression) ctx.expression( 1 ).accept( this );
		return new BinaryArithmeticExpression(
				BinaryArithmeticExpression.Operation.DIVIDE,
				firstOperand,
				secondOperand,
				ExpressionTypeHelper.resolveArithmeticType(
						(BasicType) firstOperand.getExpressionType(),
						(BasicType) secondOperand.getExpressionType(),
						parsingContext.getConsumerContext(),
						true
				)
		);
	}

	@Override
	public Object visitModuloExpression(HqlParser.ModuloExpressionContext ctx) {
		if ( ctx.expression().size() != 2 ) {
			throw new ParsingException( "Expecting 2 operands to the % operator" );
		}

		final Expression firstOperand = (Expression) ctx.expression( 0 ).accept( this );
		final Expression secondOperand = (Expression) ctx.expression( 1 ).accept( this );
		return new BinaryArithmeticExpression(
				BinaryArithmeticExpression.Operation.MODULO,
				firstOperand,
				secondOperand,
				ExpressionTypeHelper.resolveArithmeticType(
						(BasicType) firstOperand.getExpressionType(),
						(BasicType) secondOperand.getExpressionType(),
						parsingContext.getConsumerContext(),
						false
				)
		);
	}

	@Override
	public Object visitUnaryPlusExpression(HqlParser.UnaryPlusExpressionContext ctx) {
		return new UnaryOperationExpression(
				UnaryOperationExpression.Operation.PLUS,
				(Expression) ctx.expression().accept( this )
		);
	}

	@Override
	public Object visitUnaryMinusExpression(HqlParser.UnaryMinusExpressionContext ctx) {
		return new UnaryOperationExpression(
				UnaryOperationExpression.Operation.MINUS,
				(Expression) ctx.expression().accept( this )
		);
	}

	@Override
	@SuppressWarnings("UnnecessaryBoxing")
	public LiteralExpression visitLiteralExpression(HqlParser.LiteralExpressionContext ctx) {
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
			return new LiteralNullExpression();
		}

		// otherwise we have a problem
		throw new ParsingException( "Unexpected literal expression type [" + ctx.getText() + "]" );
	}

	private LiteralExpression<Boolean> booleanLiteral(boolean value) {
		final BasicType<Boolean> type = parsingContext.getConsumerContext().getDomainMetamodel().getBasicType( Boolean.class );

		return value
				? new LiteralTrueExpression( type )
				: new LiteralFalseExpression( type );
	}

	private LiteralCharacterExpression characterLiteral(String text) {
		if ( text.length() > 1 ) {
			// todo : or just treat it as a String literal?
			throw new ParsingException( "Value for CHARACTER_LITERAL token was more than 1 character" );
		}
		return new LiteralCharacterExpression(
				text.charAt( 0 ),
				parsingContext.getConsumerContext().getDomainMetamodel().getBasicType( Character.class )
		);
	}

	private LiteralExpression stringLiteral(String text) {
		return new LiteralStringExpression(
				text,
				parsingContext.getConsumerContext().getDomainMetamodel().getBasicType( String.class )
		);
	}

	protected LiteralIntegerExpression integerLiteral(String text) {
		try {
			final Integer value = Integer.valueOf( text );
			return new LiteralIntegerExpression(
					value,
					parsingContext.getConsumerContext().getDomainMetamodel().getBasicType( Integer.class )
			);
		}
		catch (NumberFormatException e) {
			throw new LiteralNumberFormatException(
					"Unable to convert query literal [" + text + "] to Integer",
					e
			);
		}
	}

	protected LiteralLongExpression longLiteral(String text) {
		final String originalText = text;
		try {
			if ( text.endsWith( "l" ) || text.endsWith( "L" ) ) {
				text = text.substring( 0, text.length() - 1 );
			}
			final Long value = Long.valueOf( text );
			return new LiteralLongExpression(
					value,
					parsingContext.getConsumerContext().getDomainMetamodel().getBasicType( Long.class )
			);
		}
		catch (NumberFormatException e) {
			throw new LiteralNumberFormatException(
					"Unable to convert query literal [" + originalText + "] to Long",
					e
			);
		}
	}

	protected LiteralBigIntegerExpression bigIntegerLiteral(String text) {
		final String originalText = text;
		try {
			if ( text.endsWith( "bi" ) || text.endsWith( "BI" ) ) {
				text = text.substring( 0, text.length() - 2 );
			}
			return new LiteralBigIntegerExpression(
					new BigInteger( text ),
					parsingContext.getConsumerContext().getDomainMetamodel().getBasicType( BigInteger.class )
			);
		}
		catch (NumberFormatException e) {
			throw new LiteralNumberFormatException(
					"Unable to convert query literal [" + originalText + "] to BigInteger",
					e
			);
		}
	}

	protected LiteralFloatExpression floatLiteral(String text) {
		try {
			return new LiteralFloatExpression(
					Float.valueOf( text ),
					parsingContext.getConsumerContext().getDomainMetamodel().getBasicType( Float.class )
			);
		}
		catch (NumberFormatException e) {
			throw new LiteralNumberFormatException(
					"Unable to convert query literal [" + text + "] to Float",
					e
			);
		}
	}

	protected LiteralDoubleExpression doubleLiteral(String text) {
		try {
			return new LiteralDoubleExpression(
					Double.valueOf( text ),
					parsingContext.getConsumerContext().getDomainMetamodel().getBasicType( Double.class )
			);
		}
		catch (NumberFormatException e) {
			throw new LiteralNumberFormatException(
					"Unable to convert query literal [" + text + "] to Double",
					e
			);
		}
	}

	protected LiteralBigDecimalExpression bigDecimalLiteral(String text) {
		final String originalText = text;
		try {
			if ( text.endsWith( "bd" ) || text.endsWith( "BD" ) ) {
				text = text.substring( 0, text.length() - 2 );
			}
			return new LiteralBigDecimalExpression(
					new BigDecimal( text ),
					parsingContext.getConsumerContext().getDomainMetamodel().getBasicType( BigDecimal.class )
			);
		}
		catch (NumberFormatException e) {
			throw new LiteralNumberFormatException(
					"Unable to convert query literal [" + originalText + "] to BigDecimal",
					e
			);
		}
	}

	@Override
	public Object visitParameterExpression(HqlParser.ParameterExpressionContext ctx) {
		return ctx.parameter().accept( this );
	}

	@Override
	public NamedParameterExpression visitNamedParameter(HqlParser.NamedParameterContext ctx) {
		return new NamedParameterExpression( ctx.IDENTIFIER().getText() );
	}

	@Override
	public PositionalParameterExpression visitPositionalParameter(HqlParser.PositionalParameterContext ctx) {
		return new PositionalParameterExpression( Integer.valueOf( ctx.INTEGER_LITERAL().getText() ) );
	}

	@Override
	public FunctionExpression visitNonStandardFunction(HqlParser.NonStandardFunctionContext ctx) {
		if ( getParsingContext().getConsumerContext().useStrictJpaCompliance() ) {
			throw new StrictJpaComplianceViolation(
					"Encountered non-compliant non-standard function call [" +
							ctx.nonStandardFunctionName() + "], but strict JPQL compliance was requested",
					StrictJpaComplianceViolation.Type.FUNCTION_CALL
			);
		}

		final String functionName = ctx.nonStandardFunctionName().getText();
		final List<Expression> functionArguments = visitNonStandardFunctionArguments( ctx.nonStandardFunctionArguments() );

		// todo : integrate some form of SqlFunction look-up using the ParsingContext so we can resolve the "type"
		return new FunctionExpression( functionName, null, functionArguments );
	}

	@Override
	public List<Expression> visitNonStandardFunctionArguments(HqlParser.NonStandardFunctionArgumentsContext ctx) {
		final List<Expression> arguments = new ArrayList<Expression>();

		for ( HqlParser.ExpressionContext expressionContext : ctx.expression() ) {
			arguments.add( (Expression) expressionContext.accept( this ) );
		}

		return arguments;
	}

	@Override
	public AggregateFunction visitAggregateFunction(HqlParser.AggregateFunctionContext ctx) {
		return (AggregateFunction) super.visitAggregateFunction( ctx );
	}

	@Override
	public AvgFunction visitAvgFunction(HqlParser.AvgFunctionContext ctx) {
		final Expression expr = (Expression) ctx.expression().accept( this );
		return new AvgFunction(
				expr,
				ctx.distinctKeyword() != null,
				(BasicType) expr.getExpressionType()
		);
	}

	@Override
	public AggregateFunction visitCountFunction(HqlParser.CountFunctionContext ctx) {
		final BasicType longType = parsingContext.getConsumerContext().getDomainMetamodel().getBasicType( Long.class );
		if ( ctx.ASTERISK() != null ) {
			return new CountStarFunction( ctx.distinctKeyword() != null, longType );
		}
		else {
			return new CountFunction(
					(Expression) ctx.expression().accept( this ),
					ctx.distinctKeyword() != null,
					longType
			);
		}
	}

	@Override
	public MaxFunction visitMaxFunction(HqlParser.MaxFunctionContext ctx) {
		final Expression expr = (Expression) ctx.expression().accept( this );
		return new MaxFunction(
				expr,
				ctx.distinctKeyword() != null,
				(BasicType) expr.getExpressionType()
		);
	}

	@Override
	public MinFunction visitMinFunction(HqlParser.MinFunctionContext ctx) {
		final Expression expr = (Expression) ctx.expression().accept( this );
		return new MinFunction(
				expr,
				ctx.distinctKeyword() != null,
				(BasicType) expr.getExpressionType()
		);
	}

	@Override
	public SumFunction visitSumFunction(HqlParser.SumFunctionContext ctx) {
		final Expression expr = (Expression) ctx.expression().accept( this );
		return new SumFunction(
				expr,
				ctx.distinctKeyword() != null,
				ExpressionTypeHelper.resolveSingleNumericType(
						(BasicType) expr.getExpressionType(),
						parsingContext.getConsumerContext()
				)
		);
	}

	@Override
	public CollectionSizeFunction visitCollectionSizeFunction(HqlParser.CollectionSizeFunctionContext ctx) {
		final AttributePathPart pathResolution = (AttributePathPart) ctx.path().accept( this );

		if ( !AttributeReferenceExpression.class.isInstance( pathResolution ) ) {
			throw new SemanticException(
					"size() function can only be applied to path expressions which resolve to an attribute; specified " +
							"path [" + ctx.path().getText() + "] resolved to " + pathResolution.getClass().getName()
			);
		}

		if ( !PluralAttribute.class.isInstance( pathResolution.getBindableModelDescriptor() ) ) {
			throw new SemanticException(
					"size() function can only be applied to path expressions which resolve to a collection; specified " +
							"path [" + ctx.path().getText() + "] resolved to " + pathResolution
			);
		}

		// TODO avoid down-cast
		return new CollectionSizeFunction(
				pathResolution.getUnderlyingFromElement(),
				( (AttributeReferenceExpression) pathResolution ).getAttributeDescriptor(),
				parsingContext.getConsumerContext().getDomainMetamodel().getBasicType( Long.class )
		);
	}

	@Override
	public CollectionValueFunction visitCollectionValueFunction(HqlParser.CollectionValueFunctionContext ctx) {
		final AttributePathPart pathResolution = (AttributePathPart) ctx.path().accept( this );

		if ( !PluralAttribute.class.isInstance( pathResolution.getBindableModelDescriptor() ) ) {
			throw new SemanticException(
					"value() function can only be applied to path expressions which resolve to a collection; specified " +
							"path [" + ctx.path().getText() + "] resolved to " + pathResolution.getBindableModelDescriptor()
			);
		}

		final PluralAttribute collectionReference = (PluralAttribute) pathResolution.getBindableModelDescriptor();

		if ( getParsingContext().getConsumerContext().useStrictJpaCompliance() ) {
			if ( collectionReference.getCollectionClassification() != PluralAttribute.CollectionClassification.MAP ) {
				throw new StrictJpaComplianceViolation(
						"Encountered application of value() function to path expression which does not " +
								"resolve to a persistent Map, but strict JPQL compliance was requested. specified "
								+ "path [" + ctx.path().getText() + "] resolved to " + pathResolution.getBindableModelDescriptor(),
						StrictJpaComplianceViolation.Type.VALUE_FUNCTION_ON_NON_MAP
				);
			}
		}

		return new CollectionValueFunction( pathResolution.getUnderlyingFromElement(), collectionReference.getElementType() );
	}

	@Override
	public CollectionIndexFunction visitCollectionIndexFunction(HqlParser.CollectionIndexFunctionContext ctx) {
		final String alias = ctx.IDENTIFIER().getText();
		final FromElement fromElement = getFromElementBuilder().getAliasRegistry().findFromElementByAlias( alias );

		if ( !PluralAttribute.class.isInstance( fromElement.getBindableModelDescriptor() ) ) {
			throw new SemanticException(
					"index() function can only be applied to identification variables which resolve to a collection; specified " +
							"identification variable [" + alias + "] resolved to " + fromElement.getBindableModelDescriptor()
			);
		}

		final PluralAttribute collectionDescriptor = (PluralAttribute) fromElement.getBindableModelDescriptor();
		if ( collectionDescriptor.getCollectionClassification() != PluralAttribute.CollectionClassification.MAP
				&& collectionDescriptor.getCollectionClassification() != PluralAttribute.CollectionClassification.LIST ) {
			throw new SemanticException(
					"index() function can only be applied to identification variables which resolve to an " +
							"indexed collection (map,list); specified identification variable [" + alias +
							"] resolved to " + collectionDescriptor
			);
		}

		return new CollectionIndexFunction( fromElement, collectionDescriptor.getIndexType() );
	}

	@Override
	public MapKeyFunction visitMapKeyFunction(HqlParser.MapKeyFunctionContext ctx) {
		final AttributePathPart pathResolution = (AttributePathPart) ctx.path().accept( this );

		final PluralAttribute pluralAttribute = (PluralAttribute) pathResolution.getBindableModelDescriptor();
		if ( pluralAttribute.getCollectionClassification() != PluralAttribute.CollectionClassification.MAP ) {
			throw new SemanticException(
					"key() function can only be applied to path expressions which resolve to a persistent Map; " +
							"specified path [" + ctx.path().getText() + "] resolved to " + pathResolution.getBindableModelDescriptor()
			);
		}

		return new MapKeyFunction( pathResolution.getUnderlyingFromElement(), pluralAttribute.getIndexType() );
	}


	@Override
	public MapEntryFunction visitMapEntryFunction(HqlParser.MapEntryFunctionContext ctx) {
		final AttributePathPart pathResolution = (AttributePathPart) ctx.path().accept( this );

		if ( inWhereClause ) {
			throw new SemanticException(
					"entry() function may only be used in SELECT clauses; specified "
							+ "path [" + ctx.path().getText() + "] is used in WHERE clause" );
		}

		if ( PluralAttribute.class.isInstance( pathResolution.getBindableModelDescriptor() ) ) {
			final PluralAttribute pluralAttribute = (PluralAttribute) pathResolution.getBindableModelDescriptor();
			if ( pluralAttribute.getCollectionClassification() == PluralAttribute.CollectionClassification.MAP ) {
				return new MapEntryFunction(
						pathResolution.getUnderlyingFromElement(),
						pluralAttribute.getIndexType(),
						pluralAttribute.getElementType()
				);
			}
		}

		throw new SemanticException(
				"entry() function can only be applied to path expressions which resolve to a persistent Map; specified "
						+ "path [" + ctx.path().getText() + "] resolved to " + pathResolution.getBindableModelDescriptor()
		);
	}

	@Override
	public MaxElementFunction visitMaxElementFunction(HqlParser.MaxElementFunctionContext ctx) {
		if ( getParsingContext().getConsumerContext().useStrictJpaCompliance() ) {
			throw new StrictJpaComplianceViolation( HQL_COLLECTION_FUNCTION );
		}

		final AttributePathPart pathResolution = (AttributePathPart) ctx.path().accept( this );

		if ( !PluralAttribute.class.isInstance( pathResolution.getBindableModelDescriptor() ) ) {
			throw new SemanticException(
					"maxelement() function can only be applied to path expressions which resolve to a " +
							"collection; specified path [" + ctx.path().getText() +
							"] resolved to " + pathResolution.getBindableModelDescriptor()
			);
		}

		final PluralAttribute pluralAttribute = (PluralAttribute) pathResolution.getBindableModelDescriptor();
		return new MaxElementFunction( pathResolution.getUnderlyingFromElement(), pluralAttribute.getElementType() );
	}

	@Override
	public MinElementFunction visitMinElementFunction(HqlParser.MinElementFunctionContext ctx) {
		if ( getParsingContext().getConsumerContext().useStrictJpaCompliance() ) {
			throw new StrictJpaComplianceViolation( HQL_COLLECTION_FUNCTION );
		}

		final AttributePathPart pathResolution = (AttributePathPart) ctx.path().accept( this );
		if ( !PluralAttribute.class.isInstance( pathResolution.getBindableModelDescriptor() ) ) {
			throw new SemanticException(
					"minelement() function can only be applied to path expressions which resolve to a " +
							"collection; specified path [" + ctx.path().getText() + "] resolved to "
							+ pathResolution.getBindableModelDescriptor()
			);
		}

		final PluralAttribute pluralAttribute = (PluralAttribute) pathResolution.getBindableModelDescriptor();
		return new MinElementFunction( pathResolution.getUnderlyingFromElement(), pluralAttribute.getElementType() );
	}

	@Override
	public MaxIndexFunction visitMaxIndexFunction(HqlParser.MaxIndexFunctionContext ctx) {
		if ( getParsingContext().getConsumerContext().useStrictJpaCompliance() ) {
			throw new StrictJpaComplianceViolation( HQL_COLLECTION_FUNCTION );
		}

		final AttributePathPart pathResolution = (AttributePathPart) ctx.path().accept( this );
		if ( PluralAttribute.class.isInstance( pathResolution.getBindableModelDescriptor() ) ) {
			final PluralAttribute pluralAttribute = (PluralAttribute) pathResolution.getBindableModelDescriptor();
			if ( pluralAttribute.getCollectionClassification() == PluralAttribute.CollectionClassification.LIST ) {
				return new MaxIndexFunction(
						pathResolution.getUnderlyingFromElement(),
						// todo : again, best we can do given the JPA model
						parsingContext.getConsumerContext().getDomainMetamodel().getBasicType( Integer.class )
				);
			}
			else if ( pluralAttribute.getCollectionClassification() == PluralAttribute.CollectionClassification.MAP ) {
				return new MaxIndexFunction(
						pathResolution.getUnderlyingFromElement(),
						pluralAttribute.getIndexType()
				);
			}
		}

		throw new SemanticException(
				"maxindex() function can only be applied to path expressions which resolve to an " +
						"indexed collection (list,map); specified path [" + ctx.path().getText() +
						"] resolved to " + pathResolution.getBindableModelDescriptor()
		);
	}

	@Override
	public MinIndexFunction visitMinIndexFunction(HqlParser.MinIndexFunctionContext ctx) {
		if ( getParsingContext().getConsumerContext().useStrictJpaCompliance() ) {
			throw new StrictJpaComplianceViolation( HQL_COLLECTION_FUNCTION );
		}

		final AttributePathPart pathResolution = (AttributePathPart) ctx.path().accept( this );
		if ( PluralAttribute.class.isInstance( pathResolution.getBindableModelDescriptor() ) ) {
			final PluralAttribute pluralAttribute = (PluralAttribute) pathResolution.getBindableModelDescriptor();
			if ( pluralAttribute.getCollectionClassification() == PluralAttribute.CollectionClassification.LIST ) {
				return new MinIndexFunction(
						pathResolution.getUnderlyingFromElement(),
						// todo : again, best we can do given the JPA model
						parsingContext.getConsumerContext().getDomainMetamodel().getBasicType( Integer.class )
				);
			}
			else if ( pluralAttribute.getCollectionClassification() == PluralAttribute.CollectionClassification.MAP ) {
				return new MinIndexFunction(
						pathResolution.getUnderlyingFromElement(),
						pluralAttribute.getIndexType()
				);
			}
		}

		throw new SemanticException(
				"minindex() function can only be applied to path expressions which resolve to an " +
						"indexed collection (list,map); specified path [" + ctx.path().getText() +
						"] resolved to " + pathResolution.getBindableModelDescriptor()
		);
	}

	@Override
	public SubQueryExpression visitSubQueryExpression(HqlParser.SubQueryExpressionContext ctx) {
		final QuerySpec querySpec = visitQuerySpec( ctx.querySpec() );
		return new SubQueryExpression( querySpec, determineTypeDescriptor( querySpec.getSelectClause() ) );
	}

	private static Type determineTypeDescriptor(SelectClause selectClause) {
		if ( selectClause.getSelections().size() != 0 ) {
			return null;
		}

		final Selection selection = selectClause.getSelections().get( 0 );
		return selection.getExpression().getExpressionType();
	}
}
