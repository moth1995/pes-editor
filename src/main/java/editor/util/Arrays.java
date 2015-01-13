package editor.util;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Map;

public final class Arrays {
	private Arrays() {
	}

	public static int indexOfIgnoreCase(String[] array, String value) {
		if (null == array) throw new NullPointerException("array");
		for (int i = 0; i < array.length; i++) {
			if (Strings.equalsIgnoreCase(array[i], value))
				return i;
		}
		return -1;
	}

	public static int indexOf(Object[] array, Object value) {
		if (null == array) throw new NullPointerException("array");
		for (int i = 0; i < array.length; i++) {
			if ((null == array[i] && null == value)
					|| (null != array[i] && null != value && array[i].equals(value)))
				return i;
		}
		return -1;
	}

	public static class EntryValueComparator<K, V extends Comparable<? super V>>
			implements Comparator<Map.Entry<K, V>>, Serializable {
		private static final long serialVersionUID = 1L;

		public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
			return o2.getValue().compareTo(o1.getValue());
		}
	}

}
