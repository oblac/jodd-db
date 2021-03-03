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

package jodd.db.oom;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ScanResult;
import jodd.db.oom.meta.DbTable;
import jodd.util.StringPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Auto-magically scans classpath for domain objects annotated with DbOom annotations.
 */
public class AutomagicDbOomConfigurator {

	private static final Logger log = LoggerFactory.getLogger(AutomagicDbOomConfigurator.class);

	protected final boolean registerAsEntities;
	protected final DbEntityManager dbEntityManager;

	private String[] packages = StringPool.EMPTY_ARRAY;

	public AutomagicDbOomConfigurator setPackages(String... packages) {
		this.packages = packages;
		return this;
	}

	public AutomagicDbOomConfigurator(final DbEntityManager dbEntityManager, final boolean registerAsEntities) {
		this.dbEntityManager = dbEntityManager;
		this.registerAsEntities = registerAsEntities;
	}

	/**
	 * Configures {@link DbEntityManager} with specified class path.
	 */
	public void configure() {
		long elapsed = System.currentTimeMillis();

		String petiteBeanAnnotation = DbTable.class.getName();
		try (ScanResult scanResult =
				     new ClassGraph()
						     .acceptPackages(packages)
						     .enableAnnotationInfo()
						     .enableClassInfo()       // Scan classes
						     .scan()) {               // Start the scan
			for (ClassInfo routeClassInfo : scanResult.getClassesWithAnnotation(petiteBeanAnnotation)) {
				final Class<?> beanClass = routeClassInfo.loadClass();

				if (registerAsEntities) {
					dbEntityManager.registerEntity(beanClass);
				} else {
					dbEntityManager.registerType(beanClass);
				}
			}
		}


		log.info("DbEntityManager configured in " + elapsed + "ms. Total entities: " + dbEntityManager.getTotalNames());
	}

}
