* type function - SQM-17
* strict jpql compliance -> nested (non-embedded) join paths
* strict jpql compliance -> map key() function using path rather than alias
* strict jpql compliance -> TREAT context (iirc spec allows in FROM and WHERE only)


from orm-sqm poc
----------------

* Possibly we should maintain a map from Expression -> "select alias" for substitution in other clauses.  For example,
	given `select a.b + a.c as s from Anything a order by a.b + a.c` the more efficient query (SQL-wise) is a substitution to
	`select a.b + a.c as s from Anything a order by s`.
* Proper handling for GroupedPredicate alternatives (explicit grouping parenthesis) - ATM I simply
	created a GroupedPredicate class; maybe that is enough
* Proper identification of left and right hand side of joins, at least for joins with ON or WITH clauses.  See 
	`org.hibernate.query.parser.internal.hql.antlr.SemanticQueryBuilder#visitQualifiedJoinPredicate` for details.  Note that I keep
	joins in a flat structure because its easier during the initial phase (frm clause processing); and in fact it might
	be impossible to properly identify the left hand side of an "ad hoc" entity join.
* TREAT should be journaled into the respective FromElement along with some concept of where it came from (because ultimately that
  	affects its rendering into SQL).  For TREAT in SELECT we may still need a wrapper (see next point too)
* Make sure that FromElements are NEVER used directly in other parts of the query.  All references to a FromElement in
	another part of the query should always be "wrapped" in another type (FromElementReferenceExpression, e.g.).  Part
	of this is that I do not think its a good idea for all FromElement types (via org.hibernate.sqm.path.AttributePathPart) 
	to be Expressions; that change has some bearing on the AttributePathResolver
	code.

Downcasting (TREAT)
-----------------------

Have FromElement (in SQM) maintain a List of down-cast targets.  Ultimately we need to know whether to render these
as INNER or OUTER joins.  JPA only allows TREAT in FROM and WHERE, so SQM should consider uses in other context a 
"strict JPQL violation".  

An alternative to the INNER/OUTER decision is to always render an outer join here (to the subtype tables) and generate a 
predicate where ever the TREAT occurs.   In the FROM clause it would become part of the join predicate.  In there WHERE 
clause we'd generate a grouped predicate.  In SELECT (?) I guess just render a predicate into the WHERE



Subclass attribute references
-----------------------------

Another piece to determine whether we need to include subclass tables is whether the query referenced any of the 
subclass attributes.  JPQL disallows this (strict JPQL violation), but HQL allows it.

One option would be to simply handle this via the mechanism for treat.  When a subclass attribute is referenced, implicitly
add a TREAT reference to the FromElement.

Another option is to just keep a list of the referenced attributes for each FromElement.  On the "back end" we can 
work out the subclass table inclusion based on that info.