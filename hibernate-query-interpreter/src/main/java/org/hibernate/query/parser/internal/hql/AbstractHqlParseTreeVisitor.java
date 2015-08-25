/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.query.parser.internal.hql;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.query.parser.internal.hql.antlr.HqlParser;
import org.hibernate.query.parser.internal.hql.antlr.HqlParserBaseVisitor;
import org.hibernate.query.parser.LiteralNumberFormatException;
import org.hibernate.query.parser.ParsingException;
import org.hibernate.query.parser.SemanticException;
import org.hibernate.query.parser.internal.FromClauseIndex;
import org.hibernate.query.parser.internal.FromElementBuilder;
import org.hibernate.query.parser.internal.ParsingContext;
import org.hibernate.query.parser.internal.hql.path.AttributePathResolver;
import org.hibernate.query.parser.internal.hql.path.AttributePathResolverStack;
import org.hibernate.query.parser.internal.hql.path.IndexedAttributeRootPathResolver;
import org.hibernate.query.parser.internal.hql.phase1.FromClauseStackNode;
import org.hibernate.sqm.domain.CollectionTypeDescriptor;
import org.hibernate.sqm.domain.EntityTypeDescriptor;
import org.hibernate.sqm.domain.TypeDescriptor;
import org.hibernate.sqm.path.AttributePathPart;
import org.hibernate.sqm.query.QuerySpec;
import org.hibernate.sqm.query.SelectStatement;
import org.hibernate.sqm.query.expression.AttributeReferenceExpression;
import org.hibernate.sqm.query.expression.BinaryArithmeticExpression;
import org.hibernate.sqm.query.expression.ConcatExpression;
import org.hibernate.sqm.query.expression.ConstantEnumExpression;
import org.hibernate.sqm.query.expression.ConstantExpression;
import org.hibernate.sqm.query.expression.ConstantFieldExpression;
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
import org.hibernate.sqm.query.expression.NamedParameterExpression;
import org.hibernate.sqm.query.expression.PositionalParameterExpression;
import org.hibernate.sqm.query.expression.SubQueryExpression;
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
import org.hibernate.sqm.query.predicate.GroupedPredicate;
import org.hibernate.sqm.query.predicate.InSubQueryPredicate;
import org.hibernate.sqm.query.predicate.InTupleListPredicate;
import org.hibernate.sqm.query.expression.IndexedAttributePathPart;
import org.hibernate.sqm.query.predicate.IsEmptyPredicate;
import org.hibernate.sqm.query.predicate.IsNullPredicate;
import org.hibernate.sqm.query.predicate.LikePredicate;
import org.hibernate.sqm.query.predicate.MemberOfPredicate;
import org.hibernate.sqm.query.predicate.NegatedPredicate;
import org.hibernate.sqm.query.predicate.OrPredicate;
import org.hibernate.sqm.query.predicate.Predicate;
import org.hibernate.sqm.query.predicate.RelationalPredicate;
import org.hibernate.sqm.query.predicate.WhereClause;
import org.hibernate.sqm.query.select.AliasedDynamicInstantiationArgument;
import org.hibernate.sqm.query.select.DynamicInstantiation;
import org.hibernate.sqm.query.select.SelectClause;
import org.hibernate.sqm.query.select.SelectList;
import org.hibernate.sqm.query.select.SelectListItem;
import org.hibernate.sqm.query.select.Selection;

import org.jboss.logging.Logger;

/**
 * @author Steve Ebersole
 */
public abstract class AbstractHqlParseTreeVisitor extends HqlParserBaseVisitor {
	private static final Logger log = Logger.getLogger( AbstractHqlParseTreeVisitor.class );

	private final ParsingContext parsingContext;
	private final FromElementBuilder fromElementBuilder;
	private final FromClauseIndex fromClauseIndex;
	protected final AttributePathResolverStack attributePathResolverStack = new AttributePathResolverStack();

	public AbstractHqlParseTreeVisitor(
			ParsingContext parsingContext,
			FromElementBuilder fromElementBuilder,
			FromClauseIndex fromClauseIndex) {
		this.parsingContext = parsingContext;
		this.fromElementBuilder = fromElementBuilder;
		this.fromClauseIndex = fromClauseIndex;
	}

	public abstract FromClause getCurrentFromClause();

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
			log.info( "Encountered implicit select clause which is a deprecated feature : " + ctx.toString() );
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
		return new SelectClause(
				new SelectList(
						new SelectListItem(
								new FromElementReferenceExpression(
										fromClause.getFromElementSpaces().get( 0 ).getRoot()
								)
						)
				)
		);
	}

	@Override
	public FromClause visitFromClause(HqlParser.FromClauseContext ctx) {
		return getCurrentFromClause();
	}

	@Override
	public SelectClause visitSelectClause(HqlParser.SelectClauseContext ctx) {
		return new SelectClause(
				visitSelection( ctx.selection() ),
				ctx.distinctKeyword() != null
		);
	}

	@Override
	public Selection visitSelection(HqlParser.SelectionContext ctx) {
		if ( ctx.dynamicInstantiation() != null ) {
			return visitDynamicInstantiation( ctx.dynamicInstantiation() );
		}
		else if ( ctx.jpaSelectObjectSyntax() != null ) {
			return visitJpaSelectObjectSyntax( ctx.jpaSelectObjectSyntax() );
		}
		else if ( ctx.selectItemList() != null ) {
			return visitSelectItemList( ctx.selectItemList() );
		}

		throw new ParsingException( "Unexpected selection rule type : " + ctx );
	}

	@Override
	public DynamicInstantiation visitDynamicInstantiation(HqlParser.DynamicInstantiationContext ctx) {
		final String className = ctx.dynamicInstantiationTarget().getText();
		final DynamicInstantiation dynamicInstantiation;
		try {
			dynamicInstantiation = new DynamicInstantiation(
					parsingContext.getConsumerContext().classByName( className )
			);
		}
		catch (ClassNotFoundException e) {
			throw new SemanticException( "Unable to resolve class named for dynamic instantiation : " + className );
		}

		for ( HqlParser.DynamicInstantiationArgContext arg : ctx.dynamicInstantiationArgs().dynamicInstantiationArg() ) {
			dynamicInstantiation.addArgument( visitDynamicInstantiationArg( arg ) );
		}

		return dynamicInstantiation;
	}

	@Override
	public AliasedDynamicInstantiationArgument visitDynamicInstantiationArg(HqlParser.DynamicInstantiationArgContext ctx) {
		return new AliasedDynamicInstantiationArgument(
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

		throw new ParsingException( "Unexpected dynamic-instantiation-argument rule type : " + ctx );
	}

	@Override
	public Selection visitJpaSelectObjectSyntax(HqlParser.JpaSelectObjectSyntaxContext ctx) {
		final String alias = ctx.IDENTIFIER().getText();
		final FromElement fromElement = fromClauseIndex.findFromElementByAlias( alias );
		if ( fromElement == null ) {
			throw new SemanticException( "Unable to resolve alias [" +  alias + "] in selection [" + ctx.getText() + "]" );
		}
		return new SelectList(
				new SelectListItem(
						new FromElementReferenceExpression( fromElement )
				)
		);
	}

	@Override
	public SelectList visitSelectItemList(HqlParser.SelectItemListContext ctx) {
		final SelectList selectList = new SelectList();
		for ( HqlParser.SelectItemContext selectItemContext : ctx.selectItem() ) {
			selectList.addSelectListItem( visitSelectItem( selectItemContext ) );
		}
		return selectList;
	}

	@Override
	public SelectListItem visitSelectItem(HqlParser.SelectItemContext ctx) {
		return new SelectListItem(
				(Expression) ctx.expression().accept( this ),
				ctx.IDENTIFIER() == null ? null : ctx.IDENTIFIER().getText()
		);
	}

	@Override
	public Predicate visitQualifiedJoinPredicate(HqlParser.QualifiedJoinPredicateContext ctx) {
		return (Predicate) ctx.predicate().accept( this );
	}

	@Override
	public WhereClause visitWhereClause(HqlParser.WhereClauseContext ctx) {
		return new WhereClause( (Predicate) ctx.predicate().accept( this ) );
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
	public IsNullPredicate visitIsNullPredicate(HqlParser.IsNullPredicateContext ctx) {
		return new IsNullPredicate( (Expression) ctx.expression().accept( this ) );
	}

	@Override
	public IsEmptyPredicate visitIsEmptyPredicate(HqlParser.IsEmptyPredicateContext ctx) {
		return new IsEmptyPredicate( (Expression) ctx.expression().accept( this ) );
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
		return new BetweenPredicate(
				(Expression) ctx.expression().get( 0 ).accept( this ),
				(Expression) ctx.expression().get( 1 ).accept( this ),
				(Expression) ctx.expression().get( 2 ).accept( this )
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
		if ( !CollectionTypeDescriptor.class.isInstance( attributeReference.getTypeDescriptor() ) ) {
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

		throw new ParsingException( "Unexpected IN predicate type [" + ctx.getClass().getSimpleName() + "] : " + ctx.toString() );
	}

	@Override
	public Object visitSimplePath(HqlParser.SimplePathContext ctx) {
		final AttributePathPart attributePathPart = getCurrentAttributePathResolver().resolvePath( ctx.dotIdentifierSequence() );
		if ( attributePathPart != null ) {
			return attributePathPart;
		}

		final String pathText = ctx.getText();

		final EntityTypeDescriptor entityType = parsingContext.getConsumerContext().resolveEntityReference( pathText );
		if ( entityType != null ) {
			return new EntityTypeExpression( entityType );
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
					return new ConstantEnumExpression( Enum.valueOf( clazz, fieldName ) );
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
					return new ConstantFieldExpression( field.get( null ) );
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

		final TypeDescriptor treatAsTypeDescriptor = parsingContext.getConsumerContext().resolveEntityReference(
				treatAsName
		);
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
		if ( !CollectionTypeDescriptor.class.isInstance( indexSource.getTypeDescriptor() ) ) {
			throw new SemanticException( "Index operator only valid for indexed collections (maps, lists, arrays) : " + indexSource );
		}

		final CollectionTypeDescriptor collectionTypeDescriptor = (CollectionTypeDescriptor) indexSource.getTypeDescriptor();
		if ( collectionTypeDescriptor.getIndexTypeDescriptor() == null ) {
			throw new SemanticException( "Index operator only valid for indexed collections (maps, lists, arrays) : " + indexSource );
		}

		// todo : would be nice to validate the index's type against the Collection-index's type
		// 		but that requires "compatible type checking" rather than TypeDescriptor sameness (long versus int, e.g)

		final IndexedAttributePathPart indexedReference = new IndexedAttributePathPart( indexSource, indexExpression );

		if ( ctx.path( 1 ) == null ) {
			return indexedReference;
		}


		// we have a de-reference of the indexed reference.  push a new path resolver
		// that handles the indexed reference as the root to the path
		attributePathResolverStack.push(
				new IndexedAttributeRootPathResolver(
						fromElementBuilder,
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
				(Expression) ctx.expression( 0 ).accept( this )
		);
	}

	@Override
	public Object visitAdditionExpression(HqlParser.AdditionExpressionContext ctx) {
		if ( ctx.expression().size() != 2 ) {
			throw new ParsingException( "Expecting 2 operands to the + operator" );
		}
		return new BinaryArithmeticExpression(
				BinaryArithmeticExpression.Operation.ADD,
				(Expression) ctx.expression( 0 ).accept( this ),
				(Expression) ctx.expression( 0 ).accept( this )
		);
	}

	@Override
	public Object visitSubtractionExpression(HqlParser.SubtractionExpressionContext ctx) {
		if ( ctx.expression().size() != 2 ) {
			throw new ParsingException( "Expecting 2 operands to the - operator" );
		}
		return new BinaryArithmeticExpression(
				BinaryArithmeticExpression.Operation.SUBTRACT,
				(Expression) ctx.expression( 0 ).accept( this ),
				(Expression) ctx.expression( 0 ).accept( this )
		);
	}

	@Override
	public Object visitMultiplicationExpression(HqlParser.MultiplicationExpressionContext ctx) {
		if ( ctx.expression().size() != 2 ) {
			throw new ParsingException( "Expecting 2 operands to the * operator" );
		}
		return new BinaryArithmeticExpression(
				BinaryArithmeticExpression.Operation.MULTIPLY,
				(Expression) ctx.expression( 0 ).accept( this ),
				(Expression) ctx.expression( 0 ).accept( this )
		);
	}

	@Override
	public Object visitDivisionExpression(HqlParser.DivisionExpressionContext ctx) {
		if ( ctx.expression().size() != 2 ) {
			throw new ParsingException( "Expecting 2 operands to the / operator" );
		}
		return new BinaryArithmeticExpression(
				BinaryArithmeticExpression.Operation.DIVIDE,
				(Expression) ctx.expression( 0 ).accept( this ),
				(Expression) ctx.expression( 0 ).accept( this )
		);
	}

	@Override
	public Object visitModuloExpression(HqlParser.ModuloExpressionContext ctx) {
		if ( ctx.expression().size() != 2 ) {
			throw new ParsingException( "Expecting 2 operands to the % operator" );
		}
		return new BinaryArithmeticExpression(
				BinaryArithmeticExpression.Operation.MODULO,
				(Expression) ctx.expression( 0 ).accept( this ),
				(Expression) ctx.expression( 0 ).accept( this )
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
			final String text = ctx.literal().CHARACTER_LITERAL().getText();
			if ( text.length() > 1 ) {
				// todo : or just treat it as a String literal?
				throw new ParsingException( "Value for CHARACTER_LITERAL token was more than 1 character" );
			}
			return new LiteralCharacterExpression( Character.valueOf( text.charAt( 0 ) ) );
		}
		else if ( ctx.literal().STRING_LITERAL() != null ) {
			return new LiteralStringExpression( ctx.literal().STRING_LITERAL().getText() );
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
			return new LiteralFalseExpression();
		}
		else if ( ctx.literal().TRUE() != null ) {
			return new LiteralTrueExpression();
		}
		else if ( ctx.literal().NULL() != null ) {
			return new LiteralNullExpression();
		}

		// otherwise we have a problem
		throw new ParsingException( "Unexpected literal expression type [" + ctx.getText() + "]" );
	}

	protected LiteralIntegerExpression integerLiteral(String text) {
		try {
			final Integer value = Integer.valueOf( text );
			return new LiteralIntegerExpression( value );
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
			return new LiteralLongExpression( value );
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
			return new LiteralBigIntegerExpression( new BigInteger( text ) );
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
			return new LiteralFloatExpression( Float.valueOf( text ) );
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
			return new LiteralDoubleExpression( Double.valueOf( text ) );
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
			return new LiteralBigDecimalExpression( new BigDecimal( text ) );
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
		final String functionName = ctx.nonStandardFunctionName().getText();
		final List<Expression> functionArguments = visitNonStandardFunctionArguments( ctx.nonStandardFunctionArguments() );

		// todo : integrate some form of SqlFunction look-up using the ParsingContext so we can resolve the "type"
		return new FunctionExpression( functionName, functionArguments, null );
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
	public SubQueryExpression visitSubQueryExpression(HqlParser.SubQueryExpressionContext ctx) {
		return new SubQueryExpression( visitQuerySpec( ctx.querySpec() ) );
	}
}
