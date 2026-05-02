/*******************************************************************************
 * Copyright (c) 2009, 2010 Cloudsmith Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Cloudsmith Inc. - initial API and implementation
 *******************************************************************************/

package org.eclipse.equinox.p2.tests.omniVersion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.eclipse.equinox.p2.metadata.Version;
import org.junit.jupiter.api.Test;

/**
 * Tests processing rules not tested elsewhere, and combinations of processing
 * rules.
 */
public class FormatProcessingTest {

	@Test
	public void testIgnore() {
		Version v = Version.parseVersion("format(n=!;.n.n):100.1.2");
		assertNotNull(v);
		assertEquals(Integer.valueOf(1), v.getSegment(0));
		assertEquals(Integer.valueOf(2), v.getSegment(1));
	}

	@Test
	public void testDefaultArrayWithPad() {
		Version v = Version.parseVersion("format(s.?<n.n>=<1.0pm>;=p10;?):alpha");
		assertNotNull(v);
		assertEquals(Version.parseVersion("raw:'alpha'.<1.0pm>"), v);

		assertNotNull(v = Version.parseVersion("format(s.?<n.n>=<1.0pm>;=p10;?):alpha.1.2"));
		assertEquals(Version.parseVersion("raw:'alpha'.<1.2p10>"), v);
	}

	@Test
	public void testDefaultValues() {
		Version v = Version.parseVersion("format(n.[n=1;].?[s='foo';].?[a='bar';].?[a=2;]):9.");
		assertNotNull(v);
		assertEquals(Version.parseVersion("raw:9.1.'foo'.'bar'.2"), v);
	}

	@Test
	public void testArrayDefaultValues() {
		Version v = null;
		assertNotNull(v = Version.parseVersion("format(n.<n.n>=<1.0>;?):9."));
		assertEquals(Version.parseVersion("raw:9.<1.0>"), v);

		// array parses, so individual defaults are used
		assertNotNull(v = Version.parseVersion("format(n.<n=3;?.?n=4;?>=<1.0>;?):9."));
		assertEquals("individual defaults should be used", Version.parseVersion("raw:9.<3.4>"), v);

		// array does not parse, individual values are not used
		assertNotNull(v = Version.parseVersion("format(n.<n=3;?.n=4;?>=<1.0>;?):9."));
		assertEquals("individual defaults should not be used", Version.parseVersion("raw:9.<1.0>"), v);
	}

	@Test
	public void testOtherTypeAsDefault() {
		Version v = null;
		assertNotNull(v = Version.parseVersion("format(s=123;?n):1"));
		assertEquals("#1.1", Version.parseVersion("raw:123.1"), v);

		assertNotNull(v = Version.parseVersion("format(s=M;?n):1"));
		assertEquals("#1.2", Version.parseVersion("raw:M.1"), v);

		assertNotNull(v = Version.parseVersion("format(s=-M;?n):1"));
		assertEquals("#1.3", Version.parseVersion("raw:-M.1"), v);

		assertNotNull(v = Version.parseVersion("format(s=<1.2>;?n):1"));
		assertEquals("#1.4", Version.parseVersion("raw:<1.2>.1"), v);

		assertNotNull(v = Version.parseVersion("format(n='abc';?s):a"));
		assertEquals("#2.1", Version.parseVersion("raw:'abc'.'a'"), v);

		assertNotNull(v = Version.parseVersion("format(n=M;?s):a"));
		assertEquals("#2.2", Version.parseVersion("raw:M.'a'"), v);

		assertNotNull(v = Version.parseVersion("format(n=-M;?s):a"));
		assertEquals("#2.3", Version.parseVersion("raw:-M.'a'"), v);

		assertNotNull(v = Version.parseVersion("format(n=<'a'.'b'>;?n):1"));
		assertEquals("#2.4", Version.parseVersion("raw:<'a'.'b'>.1"), v);

		assertNotNull(v = Version.parseVersion("format(<n>='abc';?s):a"));
		assertEquals("#3.1", Version.parseVersion("raw:'abc'.'a'"), v);

		assertNotNull(v = Version.parseVersion("format(<n>=M;?s):a"));
		assertEquals("#3.2", Version.parseVersion("raw:M.'a'"), v);

		assertNotNull(v = Version.parseVersion("format(<n>=-M;?s):a"));
		assertEquals("#3.3", Version.parseVersion("raw:-M.'a'"), v);

		assertNotNull(v = Version.parseVersion("format(<n>=123;?s):a"));
		assertEquals("#3.4", Version.parseVersion("raw:123.'a'"), v);

	}

	/**
	 * A processing rule can only be applied once to the preceding element. (These
	 * tests check if the same processing can be applied twice).
	 */
	@Test
	public void testSameMoreThanOnce() {
		assertThrows(IllegalArgumentException.class,
				() -> Version.parseVersion("format(n=!;=!;.n):1.2"), "error detected:2 x =!;");
		assertThrows(IllegalArgumentException.class,
				() -> Version.parseVersion("format(s=[abc];=[123];.n):abc123.2"), "error detected:2 x =[];");
		assertThrows(IllegalArgumentException.class,
				() -> Version.parseVersion("format(nd=[^:];=[^:];n):1.2"), "error detected:2x [^];");
		assertThrows(IllegalArgumentException.class,
				() -> Version.parseVersion("format(n={1,3};={1,3};.n):1.2"), "error detected:2x ={ };");
		assertThrows(IllegalArgumentException.class,
				() -> Version.parseVersion("format(n=0;=1;.n):1.2"), "error detected:2x =default value");
		assertThrows(IllegalArgumentException.class,
				() -> Version.parseVersion("format((n.n)=pm;=pm;):1.2"), "error detected:2x =pm;");

	}

	/**
	 * Tests that it is not allowed to have both =[]; and =[^] at the same time.
	 */
	@Test
	public void testSetNotSet() {
		assertThrows(IllegalArgumentException.class,
				() -> Version.parseVersion("format(nd=[a-z];=[^.:];n):1.2"), "error detected: =[];=[^];");
	}

	/**
	 * Pad can only be combined with default value.
	 */
	@Test
	public void testBadPadCombinations() {
		assertThrows(IllegalArgumentException.class,
				() -> Version.parseVersion("format((n.n)=pm;=[abc];):1.2"), "error detected: =p; =[];");
		assertThrows(IllegalArgumentException.class,
				() -> Version.parseVersion("format((n.n)=pm;=[^.:];):1.2"), "error detected: =p; =[];");
		assertThrows(IllegalArgumentException.class,
				() -> Version.parseVersion("format((n.n)=pm;={1,3};):1.2"), "error detected: =p; ={};");

		assertThrows(IllegalArgumentException.class,
				() -> Version.parseVersion("format((n.n)=pm;=!;):1.2"), "error detected: =p; =!;");
	}

	@Test
	public void testNonPaddable() {
		assertThrows(IllegalArgumentException.class,
				() ->Version.parseVersion("format(n=pm;):1"), "error detected: n=p;");
		assertThrows(IllegalArgumentException.class,
				() ->Version.parseVersion("format(N=pm;):1"), "error detected: n=p;");

		assertThrows(IllegalArgumentException.class,
				() -> Version.parseVersion("format(s=pm;):a"), "error detected: s=p;");

		assertThrows(IllegalArgumentException.class,
				() ->Version.parseVersion("format(S=pm;):a"), "error detected: S=p;");

		assertThrows(IllegalArgumentException.class,
				() ->Version.parseVersion("format(a=pm;):a"), "error detected: a=p;");

		assertThrows(IllegalArgumentException.class,
				() ->Version.parseVersion("format(d=pm;):a"), "error detected: d=p;");

		assertThrows(IllegalArgumentException.class,
				() ->Version.parseVersion("format(q=pm;):a"), "error detected: q=p;");

		assertThrows(IllegalArgumentException.class,
				() ->Version.parseVersion("format(r=pm;):a"), "error detected: q=p;");

		assertThrows(IllegalArgumentException.class,
				() ->Version.parseVersion("format('x'=pm;n):x1"), "error detected: 'x'=p;");

		assertThrows(IllegalArgumentException.class,
				() -> Version.parseVersion("format(.=pm;n):x1"), "error detected: .=p;");

		assertThrows(IllegalArgumentException.class,
				() -> Version.parseVersion("format(p=pm;n):x1"), "error detected: p=p;");
	}

}
