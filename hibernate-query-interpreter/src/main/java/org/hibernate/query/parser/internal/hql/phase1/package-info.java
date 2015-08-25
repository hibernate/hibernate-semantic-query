/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */

/**
 * Phase 1 of HQL parsing is to process the explicit from clause expressions.  Was we walk the
 * from clauses we do 2 things:<ol>
 *     <li>
 *         First, we index all of the from clauses we find by alias, by path and by "context"
 *     </li>
 *     <li>
 *         We build a working (in-flight) representation of the from-clause.
 *     </li>
 * </ol>
 */
package org.hibernate.query.parser.internal.hql.phase1;
