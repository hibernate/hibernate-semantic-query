* type function - SQM-17
* strict jpql compliance -> nested (non-embedded) join paths
* strict jpql compliance -> map key() function using path rather than alias
* strict jpql compliance -> TREAT context (iirc spec allows in FROM and WHERE only)
* from-element hoisting - this comes into play when we have an implicit join path used in both a subquery and the query.
    how that is render in current Hibernate depends on which is seen first.  The proper solution is to always render these 
    into the outer query, but if we saw the subquery reference first we need to "hoist"/"promote" the generated from-element(s).
    See `org.hibernate.hql.internal.ast.tree.FromClause#promoteJoin` in existing Antlr2-based parser
* group-by entity support.  e.g. `select p, count(*) from Person p group by p`
* ? - should we automatically transform any `count(*)` to `count(1)` ?
 	performance