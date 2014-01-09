package com.useful.ucars;

public class Lang {
	public static String get(String key) {
		String val = getRaw(key);
		val = ucars.colorise(val);
		return val;
	}

	public static String getRaw(String key) {
		if (!ucars.lang.contains(key)) {
			return key;
		}
		return ucars.lang.getString(key);
	}
}
