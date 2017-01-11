/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.query.from;

import org.hibernate.sqm.domain.SqmExpressableTypeEntity;

/**
 * Models information about a downcast (TREAT AS).
 *
 * @author Steve Ebersole
 */
public class Downcast {
	private final SqmExpressableTypeEntity downcastTarget;
	private boolean intrinsic;

	public Downcast(SqmExpressableTypeEntity downcastTarget) {
		this( downcastTarget, false );
	}

	public Downcast(SqmExpressableTypeEntity downcastTarget, boolean intrinsic) {
		this.downcastTarget = downcastTarget;
		this.intrinsic = intrinsic;
	}

	public SqmExpressableTypeEntity getTargetType() {
		return downcastTarget;
	}

	public boolean isIntrinsic() {
		return intrinsic;
	}

	public void makeIntrinsic() {
		// one-way toggle
		intrinsic = true;
	}
}
