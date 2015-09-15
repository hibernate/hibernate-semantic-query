/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.query.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.sqm.BaseSemanticQueryWalker;
import org.hibernate.sqm.domain.EntityTypeDescriptor;
import org.hibernate.sqm.domain.PolymorphicEntityTypeDescriptor;
import org.hibernate.sqm.query.DeleteStatement;
import org.hibernate.sqm.query.QuerySpec;
import org.hibernate.sqm.query.SelectStatement;
import org.hibernate.sqm.query.Statement;
import org.hibernate.sqm.query.UpdateStatement;
import org.hibernate.sqm.query.expression.AttributeReferenceExpression;
import org.hibernate.sqm.query.expression.AvgFunction;
import org.hibernate.sqm.query.expression.BinaryArithmeticExpression;
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
import org.hibernate.sqm.query.expression.LiteralFalseExpression;
import org.hibernate.sqm.query.expression.LiteralFloatExpression;
import org.hibernate.sqm.query.expression.LiteralIntegerExpression;
import org.hibernate.sqm.query.expression.LiteralLongExpression;
import org.hibernate.sqm.query.expression.LiteralNullExpression;
import org.hibernate.sqm.query.expression.LiteralStringExpression;
import org.hibernate.sqm.query.expression.LiteralTrueExpression;
import org.hibernate.sqm.query.expression.MaxFunction;
import org.hibernate.sqm.query.expression.MinFunction;
import org.hibernate.sqm.query.expression.NamedParameterExpression;
import org.hibernate.sqm.query.expression.PositionalParameterExpression;
import org.hibernate.sqm.query.expression.SubQueryExpression;
import org.hibernate.sqm.query.expression.SumFunction;
import org.hibernate.sqm.query.expression.UnaryOperationExpression;
import org.hibernate.sqm.query.from.CrossJoinedFromElement;
import org.hibernate.sqm.query.from.FromClause;
import org.hibernate.sqm.query.from.FromElement;
import org.hibernate.sqm.query.from.FromElementSpace;
import org.hibernate.sqm.query.from.JoinedFromElement;
import org.hibernate.sqm.query.from.QualifiedAttributeJoinFromElement;
import org.hibernate.sqm.query.from.QualifiedEntityJoinFromElement;
import org.hibernate.sqm.query.from.RootEntityFromElement;
import org.hibernate.sqm.query.from.TreatedJoinedFromElement;
import org.hibernate.sqm.query.order.OrderByClause;
import org.hibernate.sqm.query.order.SortSpecification;
import org.hibernate.sqm.query.predicate.AndPredicate;
import org.hibernate.sqm.query.predicate.BetweenPredicate;
import org.hibernate.sqm.query.predicate.GroupedPredicate;
import org.hibernate.sqm.query.predicate.InSubQueryPredicate;
import org.hibernate.sqm.query.predicate.InTupleListPredicate;
import org.hibernate.sqm.query.predicate.IsEmptyPredicate;
import org.hibernate.sqm.query.predicate.IsNullPredicate;
import org.hibernate.sqm.query.predicate.LikePredicate;
import org.hibernate.sqm.query.predicate.MemberOfPredicate;
import org.hibernate.sqm.query.predicate.NegatedPredicate;
import org.hibernate.sqm.query.predicate.OrPredicate;
import org.hibernate.sqm.query.predicate.Predicate;
import org.hibernate.sqm.query.predicate.RelationalPredicate;
import org.hibernate.sqm.query.predicate.WhereClause;
import org.hibernate.sqm.query.select.DynamicInstantiation;
import org.hibernate.sqm.query.select.DynamicInstantiationArgument;
import org.hibernate.sqm.query.select.SelectClause;
import org.hibernate.sqm.query.select.Selection;
import org.hibernate.sqm.query.set.Assignment;
import org.hibernate.sqm.query.set.SetClause;

/**
 * Handles splitting queries containing unmapped polymorphic references.
 *
 * @author Steve Ebersole
 */
public class QuerySplitter {
	public static SelectStatement[] split(SelectStatement statement) {
		// We only allow unmapped polymorphism in a very restricted way.  Specifically,
		// the unmapped polymorphic reference can only be a root and can be the only
		// root.  Use that restriction to locate the unmapped polymorphic reference
		RootEntityFromElement unmappedPolymorphicReference = null;
		for ( FromElementSpace fromElementSpace : statement.getQuerySpec().getFromClause().getFromElementSpaces() ) {
			if ( PolymorphicEntityTypeDescriptor.class.isInstance( fromElementSpace.getRoot().getTypeDescriptor() ) ) {
				unmappedPolymorphicReference = fromElementSpace.getRoot();
			}
		}

		if ( unmappedPolymorphicReference == null ) {
			return new SelectStatement[] { statement };
		}

		final PolymorphicEntityTypeDescriptor unmappedPolymorphicDescriptor = (PolymorphicEntityTypeDescriptor) unmappedPolymorphicReference.getTypeDescriptor();
		final SelectStatement[] expanded = new SelectStatement[ unmappedPolymorphicDescriptor.getImplementors().size() ];

		int i = -1;
		for ( EntityTypeDescriptor mappedDescriptor : unmappedPolymorphicDescriptor.getImplementors() ) {
			i++;
			final UnmappedPolymorphismReplacer replacer = new UnmappedPolymorphismReplacer(
					statement,
					unmappedPolymorphicReference,
					mappedDescriptor
			);
			expanded[i] = replacer.visitSelectStatement( statement );
		}

		return expanded;
	}

	private static class UnmappedPolymorphismReplacer extends BaseSemanticQueryWalker {
		private final RootEntityFromElement unmappedPolymorphicFromElement;
		private final EntityTypeDescriptor mappedDescriptor;

		private Map<FromElement,FromElement> fromElementCopyMap = new HashMap<FromElement, FromElement>();

		private UnmappedPolymorphismReplacer(
				SelectStatement selectStatement,
				RootEntityFromElement unmappedPolymorphicFromElement,
				EntityTypeDescriptor mappedDescriptor) {
			this.unmappedPolymorphicFromElement = unmappedPolymorphicFromElement;
			this.mappedDescriptor = mappedDescriptor;
		}

		@Override
		public Statement visitStatement(Statement statement) {
			throw new UnsupportedOperationException( "Not valid" );
		}

		@Override
		public UpdateStatement visitUpdateStatement(UpdateStatement statement) {
			throw new UnsupportedOperationException( "Not valid" );
		}

		@Override
		public SetClause visitSetClause(SetClause setClause) {
			throw new UnsupportedOperationException( "Not valid" );
		}

		@Override
		public Object visitAssignment(Assignment assignment) {
			throw new UnsupportedOperationException( "Not valid" );
		}

		@Override
		public DeleteStatement visitDeleteStatement(DeleteStatement statement) {
			throw new UnsupportedOperationException( "Not valid" );
		}

		@Override
		public SelectStatement visitSelectStatement(SelectStatement statement) {
			SelectStatement copy = new SelectStatement();
			copy.applyQuerySpec( visitQuerySpec( statement.getQuerySpec() ) );
			copy.applyOrderByClause( visitOrderByClause( statement.getOrderByClause() ) );
			return copy;
		}

		@Override
		public QuerySpec visitQuerySpec(QuerySpec querySpec) {
			// NOTE : it is important that we visit the FromClause first so that the
			// 		fromElementCopyMap gets built before other parts of the queryspec
			// 		are visited
			return new QuerySpec(
					visitFromClause( querySpec.getFromClause() ),
					visitSelectClause( querySpec.getSelectClause() ),
					visitWhereClause( querySpec.getWhereClause() )
			);
		}

		private FromClause currentFromClauseCopy = null;

		@Override
		public FromClause visitFromClause(FromClause fromClause) {
			final FromClause previousCurrent = currentFromClauseCopy;

			try {
				FromClause copy = new FromClause();
				currentFromClauseCopy = copy;
				super.visitFromClause( fromClause );
//				for ( FromElementSpace space : fromClause.getFromElementSpaces() ) {
//					visitFromElementSpace( space );
//				}
				return copy;
			}
			finally {
				currentFromClauseCopy = previousCurrent;
			}
		}

		private FromElementSpace currentFromElementSpaceCopy;

		@Override
		public FromElementSpace visitFromElementSpace(FromElementSpace fromElementSpace) {
			if ( currentFromClauseCopy == null ) {
				throw new AssertionError( "Current FromClause copy was null" );
			}

			final FromElementSpace previousCurrent = currentFromElementSpaceCopy;
			try {
				FromElementSpace copy = currentFromClauseCopy.makeFromElementSpace();
				currentFromElementSpaceCopy = copy;
				super.visitFromElementSpace( fromElementSpace );
				return copy;
			}
			finally {
				currentFromElementSpaceCopy = previousCurrent;
			}
		}

		@Override
		public RootEntityFromElement visitRootEntityFromElement(RootEntityFromElement rootEntityFromElement) {
			if ( currentFromElementSpaceCopy == null ) {
				throw new AssertionError( "Current FromElementSpace copy was null" );
			}
			if ( currentFromElementSpaceCopy.getRoot() != null ) {
				throw new AssertionError( "FromElementSpace copy already contains root." );
			}

			final RootEntityFromElement copy;
			if ( rootEntityFromElement == unmappedPolymorphicFromElement ) {
				copy = new RootEntityFromElement(
						currentFromElementSpaceCopy,
						rootEntityFromElement.getAlias(),
						mappedDescriptor
				);
			}
			else {
				copy = new RootEntityFromElement(
						currentFromElementSpaceCopy,
						rootEntityFromElement.getAlias(),
						rootEntityFromElement.getTypeDescriptor()
				);
			}
			fromElementCopyMap.put( rootEntityFromElement, copy );
			return copy;
		}

		@Override
		public Object visitCrossJoinedFromElement(CrossJoinedFromElement joinedFromElement) {
			CrossJoinedFromElement copy = new CrossJoinedFromElement(
					currentFromElementSpaceCopy,
					joinedFromElement.getAlias(),
					joinedFromElement.getTypeDescriptor()
			);
			fromElementCopyMap.put( joinedFromElement, copy );
			return copy;
		}

		@Override
		public Object visitTreatedJoinFromElement(TreatedJoinedFromElement joinedFromElement) {
			JoinedFromElement wrappedCopy = (JoinedFromElement) joinedFromElement.getWrapped().accept( this );

			TreatedJoinedFromElement copy = new TreatedJoinedFromElement(
					wrappedCopy,
					joinedFromElement.getTypeDescriptor()
			);
			fromElementCopyMap.put( joinedFromElement, copy );
			return copy;
		}

		@Override
		public Object visitQualifiedEntityJoinFromElement(QualifiedEntityJoinFromElement joinedFromElement) {
			if ( currentFromElementSpaceCopy == null ) {
				throw new AssertionError( "Current FromElementSpace copy was null" );
			}

			QualifiedEntityJoinFromElement copy = new QualifiedEntityJoinFromElement(
					currentFromElementSpaceCopy,
					joinedFromElement.getAlias(),
					joinedFromElement.getTypeDescriptor(),
					joinedFromElement.getJoinType()
			);
			fromElementCopyMap.put( joinedFromElement, copy );
			return copy;
		}

		@Override
		public Object visitQualifiedAttributeJoinFromElement(QualifiedAttributeJoinFromElement joinedFromElement) {
			if ( currentFromElementSpaceCopy == null ) {
				throw new AssertionError( "Current FromElementSpace copy was null" );
			}

			QualifiedAttributeJoinFromElement copy = new QualifiedAttributeJoinFromElement(
					currentFromElementSpaceCopy,
					joinedFromElement.getAlias(),
					joinedFromElement.getLhsAlias(),
					joinedFromElement.getJoinedAttributeDescriptor(),
					joinedFromElement.getJoinType(),
					joinedFromElement.isFetched()
			);
			fromElementCopyMap.put( joinedFromElement, copy );
			return copy;
		}

		@Override
		public SelectClause visitSelectClause(SelectClause selectClause) {
			SelectClause copy = new SelectClause( selectClause.isDistinct() );
			for ( Selection selection : selectClause.getSelections() ) {
				copy.addSelection( visitSelection( selection ) );
			}
			return copy;
		}

		@Override
		public Selection visitSelection(Selection selection) {
			return new Selection(
					(Expression) selection.getExpression().accept( this ),
					selection.getAlias()
			);
		}

		@Override
		public DynamicInstantiation visitDynamicInstantiation(DynamicInstantiation dynamicInstantiation) {
			DynamicInstantiation copy = new DynamicInstantiation( dynamicInstantiation.getInstantiationTarget() );
			for ( DynamicInstantiationArgument aliasedArgument : dynamicInstantiation.getArguments() ) {
				copy.addArgument(
						new DynamicInstantiationArgument(
								(Expression) aliasedArgument.getExpression().accept( this ),
								aliasedArgument.getAlias()
						)
				);
			}
			return copy;
		}

		@Override
		public WhereClause visitWhereClause(WhereClause whereClause) {
			if ( whereClause == null ) {
				return null;
			}
			return new WhereClause( (Predicate) whereClause.getPredicate().accept( this ) );
		}

		@Override
		public GroupedPredicate visitGroupedPredicate(GroupedPredicate predicate) {
			return new GroupedPredicate( (Predicate) predicate.accept( this ) );
		}

		@Override
		public AndPredicate visitAndPredicate(AndPredicate predicate) {
			return new AndPredicate(
					(Predicate) predicate.getLeftHandPredicate().accept( this ),
					(Predicate) predicate.getRightHandPredicate().accept( this )
			);
		}

		@Override
		public OrPredicate visitOrPredicate(OrPredicate predicate) {
			return new OrPredicate(
					(Predicate) predicate.getLeftHandPredicate().accept( this ),
					(Predicate) predicate.getRightHandPredicate().accept( this )
			);
		}

		@Override
		public RelationalPredicate visitRelationalPredicate(RelationalPredicate predicate) {
			return new RelationalPredicate(
					predicate.getType(),
					(Expression) predicate.getLeftHandExpression().accept( this ),
					(Expression) predicate.getRightHandExpression().accept( this )
			);
		}

		@Override
		public IsEmptyPredicate visitIsEmptyPredicate(IsEmptyPredicate predicate) {
			return new IsEmptyPredicate(
					(Expression) predicate.getExpression().accept( this ),
					predicate.isNegated()
			);
		}

		@Override
		public IsNullPredicate visitIsNullPredicate(IsNullPredicate predicate) {
			return new IsNullPredicate(
					(Expression) predicate.getExpression().accept( this ),
					predicate.isNegated()
			);
		}

		@Override
		public BetweenPredicate visitBetweenPredicate(BetweenPredicate predicate) {
			return new BetweenPredicate(
					(Expression) predicate.getExpression().accept( this ),
					(Expression) predicate.getLowerBound().accept( this ),
					(Expression) predicate.getUpperBound().accept( this )
			);
		}

		@Override
		public LikePredicate visitLikePredicate(LikePredicate predicate) {
			return new LikePredicate(
					(Expression) predicate.getMatchExpression().accept( this ),
					(Expression) predicate.getPattern().accept( this ),
					(Expression) predicate.getEscapeCharacter().accept( this )
			);
		}

		@Override
		public MemberOfPredicate visitMemberOfPredicate(MemberOfPredicate predicate) {
			return new MemberOfPredicate(
					visitAttributeReferenceExpression( predicate.getAttributeReferenceExpression() )
			);
		}

		@Override
		public NegatedPredicate visitNegatedPredicate(NegatedPredicate predicate) {
			return new NegatedPredicate(
					(Predicate) predicate.getWrappedPredicate().accept( this )
			);
		}

		@Override
		public InTupleListPredicate visitInTupleListPredicate(InTupleListPredicate predicate) {
			InTupleListPredicate copy = new InTupleListPredicate(
					(Expression) predicate.getTestExpression().accept( this )
			);
			for ( Expression expression : predicate.getTupleListExpressions() ) {
				copy.addExpression( (Expression) expression.accept( this ) );
			}
			return copy;
		}

		@Override
		public InSubQueryPredicate visitInSubQueryPredicate(InSubQueryPredicate predicate) {
			return new InSubQueryPredicate(
					(Expression) predicate.getTestExpression().accept( this ),
					visitSubQueryExpression( predicate.getSubQueryExpression() )
			);
		}

		@Override
		public OrderByClause visitOrderByClause(OrderByClause orderByClause) {
			if ( orderByClause == null ) {
				return null;
			}

			OrderByClause copy = new OrderByClause();
			for ( SortSpecification sortSpecification : orderByClause.getSortSpecifications() ) {
				copy.addSortSpecification( visitSortSpecification( sortSpecification ) );
			}
			return copy;
		}

		@Override
		public SortSpecification visitSortSpecification(SortSpecification sortSpecification) {
			return new SortSpecification(
					(Expression) sortSpecification.getSortExpression().accept( this ),
					sortSpecification.getCollation(),
					sortSpecification.getSortOrder()
			);
		}

		@Override
		public PositionalParameterExpression visitPositionalParameterExpression(PositionalParameterExpression expression) {
			return new PositionalParameterExpression( expression.getPosition() );
		}

		@Override
		public NamedParameterExpression visitNamedParameterExpression(NamedParameterExpression expression) {
			return new NamedParameterExpression( expression.getName() );
		}

		@Override
		public EntityTypeExpression visitEntityTypeExpression(EntityTypeExpression expression) {
			return new EntityTypeExpression( expression.getTypeDescriptor() );
		}

		@Override
		public UnaryOperationExpression visitUnaryOperationExpression(UnaryOperationExpression expression) {
			return new UnaryOperationExpression(
					expression.getOperation(),
					(Expression) expression.getOperand().accept( this )
			);
		}

		@Override
		public AttributeReferenceExpression visitAttributeReferenceExpression(AttributeReferenceExpression expression) {
			// find the FromElement copy
			final FromElement sourceCopy = fromElementCopyMap.get( expression.getSource() );
			if ( sourceCopy == null ) {
				throw new AssertionError( "FromElement not found in copy map" );
			}
			return new AttributeReferenceExpression( sourceCopy, expression.getAttributeDescriptor() );
		}

		@Override
		public FromElementReferenceExpression visitFromElementReferenceExpression(FromElementReferenceExpression expression) {
			// find the FromElement copy
			final FromElement fromElementCopy = fromElementCopyMap.get( expression.getFromElement() );
			if ( fromElementCopy == null ) {
				throw new AssertionError( "FromElement not found in copy map" );
			}
			return new FromElementReferenceExpression( fromElementCopy );
		}

		@Override
		public FunctionExpression visitFunctionExpression(FunctionExpression expression) {
			List<Expression> argumentsCopy = new ArrayList<Expression>();
			for ( Expression argument : expression.getArguments() ) {
				argumentsCopy.add( (Expression) argument.accept( this ) );
			}
			return new FunctionExpression(
					expression.getFunctionName(),
					argumentsCopy,
					expression.getTypeDescriptor()
			);
		}

		@Override
		public AvgFunction visitAvgFunction(AvgFunction expression) {
			return new AvgFunction(
					(Expression) expression.getArgument().accept( this ),
					expression.isDistinct()
			);
		}

		@Override
		public CountStarFunction visitCountStarFunction(CountStarFunction expression) {
			return new CountStarFunction( expression.isDistinct() );
		}

		@Override
		public CountFunction visitCountFunction(CountFunction expression) {
			return new CountFunction(
					(Expression) expression.getArgument().accept( this ),
					expression.isDistinct()
			);
		}

		@Override
		public MaxFunction visitMaxFunction(MaxFunction expression) {
			return new MaxFunction(
					(Expression) expression.getArgument().accept( this ),
					expression.isDistinct()
			);
		}

		@Override
		public MinFunction visitMinFunction(MinFunction expression) {
			return new MinFunction(
					(Expression) expression.getArgument().accept( this ),
					expression.isDistinct()
			);
		}

		@Override
		public SumFunction visitSumFunction(SumFunction expression) {
			return new SumFunction(
					(Expression) expression.getArgument().accept( this ),
					expression.isDistinct()
			);
		}

		@Override
		public LiteralStringExpression visitLiteralStringExpression(LiteralStringExpression expression) {
			return new LiteralStringExpression( expression.getLiteralValue() );
		}

		@Override
		public LiteralCharacterExpression visitLiteralCharacterExpression(LiteralCharacterExpression expression) {
			return new LiteralCharacterExpression( expression.getLiteralValue() );
		}

		@Override
		public LiteralDoubleExpression visitLiteralDoubleExpression(LiteralDoubleExpression expression) {
			return new LiteralDoubleExpression( expression.getLiteralValue() );
		}

		@Override
		public LiteralIntegerExpression visitLiteralIntegerExpression(LiteralIntegerExpression expression) {
			return new LiteralIntegerExpression( expression.getLiteralValue() );
		}

		@Override
		public LiteralBigIntegerExpression visitLiteralBigIntegerExpression(LiteralBigIntegerExpression expression) {
			return new LiteralBigIntegerExpression( expression.getLiteralValue() );
		}

		@Override
		public LiteralBigDecimalExpression visitLiteralBigDecimalExpression(LiteralBigDecimalExpression expression) {
			return new LiteralBigDecimalExpression( expression.getLiteralValue() );
		}

		@Override
		public LiteralFloatExpression visitLiteralFloatExpression(LiteralFloatExpression expression) {
			return new LiteralFloatExpression( expression.getLiteralValue() );
		}

		@Override
		public LiteralLongExpression visitLiteralLongExpression(LiteralLongExpression expression) {
			return new LiteralLongExpression( expression.getLiteralValue() );
		}

		@Override
		public LiteralTrueExpression visitLiteralTrueExpression(LiteralTrueExpression expression) {
			return new LiteralTrueExpression();
		}

		@Override
		public LiteralFalseExpression visitLiteralFalseExpression(LiteralFalseExpression expression) {
			return new LiteralFalseExpression();
		}

		@Override
		public LiteralNullExpression visitLiteralNullExpression(LiteralNullExpression expression) {
			return new LiteralNullExpression();
		}

		@Override
		public ConcatExpression visitConcatExpression(ConcatExpression expression) {
			return new ConcatExpression(
					(Expression) expression.getLeftHandOperand().accept( this ),
					(Expression) expression.getRightHandOperand().accept( this )
			);
		}

		@Override
		@SuppressWarnings("unchecked")
		public ConstantEnumExpression visitConstantEnumExpression(ConstantEnumExpression expression) {
			return new ConstantEnumExpression( expression.getValue() );
		}

		@Override
		@SuppressWarnings("unchecked")
		public ConstantFieldExpression visitConstantFieldExpression(ConstantFieldExpression expression) {
			return new ConstantFieldExpression( expression.getValue() );
		}

		@Override
		public BinaryArithmeticExpression visitBinaryArithmeticExpression(BinaryArithmeticExpression expression) {
			return new BinaryArithmeticExpression(
					expression.getOperation(),
					(Expression) expression.getLeftHandOperand().accept( this ),
					(Expression) expression.getRightHandOperand().accept( this )
			);
		}

		@Override
		public SubQueryExpression visitSubQueryExpression(SubQueryExpression expression) {
			return new SubQueryExpression( visitQuerySpec( expression.getQuerySpec() ) );
		}
	}

}
