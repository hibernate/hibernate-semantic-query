/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
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
package org.hibernate.sqm.parser.internal.hql.phase1;
