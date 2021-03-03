package jodd.db.fixtures;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

/**
 * fixture of {@link java.sql.Types JDBC Types} to catch all different {@link java.sql.Types}.
 */
public abstract class JDBCTypesFixture {

	private JDBCTypesFixture() {
		// prevents initialization
	}

	/**
	 * catch all public static final int - fields
	 *
	 * @return an array with JDBC types - never null
	 * @throws IllegalAccessException if an error occurs while accecssing fields
	 */
	public static Integer[] getJDBCTypes() throws IllegalAccessException {
		final List<Integer> jdbcTypes = new ArrayList<>(39);

		Field[] fields = Types.class.getDeclaredFields();
		for (Field field : fields) {
			final int modifiers = field.getModifiers();
			// catch all public static final int - fields
			if (Modifier.isPublic(modifiers)
					&& Modifier.isStatic(modifiers)
					&& Modifier.isFinal(modifiers)
					&& field.getType() == int.class) {
				jdbcTypes.add(field.getInt(field));
			}
		}

		return jdbcTypes.toArray(new Integer[0]);
	}
}
