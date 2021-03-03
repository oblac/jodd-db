// Copyright (c) 2003-present, Jodd Team (http://jodd.org)
// All rights reserved.
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions are met:
//
// 1. Redistributions of source code must retain the above copyright notice,
// this list of conditions and the following disclaimer.
//
// 2. Redistributions in binary form must reproduce the above copyright
// notice, this list of conditions and the following disclaimer in the
// documentation and/or other materials provided with the distribution.
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
// AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
// IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
// ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
// LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
// CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
// SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
// INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
// CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
// ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
// POSSIBILITY OF SUCH DAMAGE.

package jodd.db.querymap;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.Resource;
import io.github.classgraph.ScanResult;
import jodd.exception.UncheckedException;
import jodd.props.Props;
import jodd.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

/**
 * {@link jodd.db.querymap.QueryMap} implementation based on
 * {@link jodd.props.Props} properties files.
 * Scans for <code>"*.sql.props"</code> and <code>"*.oom.props"</code>
 * properties on class path.
 */
public class DbPropsQueryMap implements QueryMap {

	private static final Logger log = LoggerFactory.getLogger(DbPropsQueryMap.class);

	protected final Pattern pattern;
	protected Props props;

	public DbPropsQueryMap(final Pattern pattern) {
		this.pattern = pattern;
		reload();
	}

	public DbPropsQueryMap() {
		this(Pattern.compile(".*.(sql|oom).prop(s|erties)"));
	}

	/**
	 * Returns <code>Props</code>.
	 */
	public Props props() {
		return props;
	}

	@Override
	public void reload() {
		props = new Props();
		try {
			loadFromClasspath(props, pattern);
		} catch (IOException e) {
			log.error("Unable to laod", e);
		}
	}

	@Override
	public int size() {
		return props.countTotalProperties();
	}

	// ---------------------------------------------------------------- sql

	/**
	 * Returns query for given key.
	 * In debug mode, props are reloaded every time before the lookup.
	 */
	@Override
	public String getQuery(final String key) {
		return props.getValue(key);
	}

	private void loadFromClasspath(final Props props, final Pattern pattern) throws IOException {

		try (ScanResult scanResult = new ClassGraph().acceptPaths().scan()) {
			scanResult.getResourcesMatchingPattern(pattern)
					.forEachByteArrayThrowingIOException((Resource res, byte[] content) -> {

						String usedEncoding = "UTF-8";
						if (StringUtil.endsWithIgnoreCase(res.getPath(), ".properties")) {
							usedEncoding = StandardCharsets.ISO_8859_1.name();
						}

						final String encoding = usedEncoding;
						final String str = new String(content, encoding);

						UncheckedException.runAndWrapException(() -> props.load(str));
			});
		}
	}
}
