/*******************************************************************************
 *  Copyright (c) 2007, 2011 IBM Corporation and others.
 *
 *  This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-2.0/
 *
 *  SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.equinox.p2.tests.full;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * Performs all automated full end-to-end install/update/rollback tests.
 */
@Suite
@SelectClasses({
// TODO re-enable all tests after resolution of https://bugs.eclipse.org/366540
// RepoValidator.class,
// End2EndTest35.class,
// End2EndTest36.class,
// End2EndTest37.class,
// End2EndTestCurrent.class,
})
public class AllTests {
	// test suite
}
