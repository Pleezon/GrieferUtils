/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.api.mapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static dev.l3g7.griefer_utils.core.api.mapping.Mapping.OBFUSCATED;

public class MappingEntries {

	public static class MappedClass extends MappedMember {

		public final MappedList<MappedField> fields = new MappedList<>();
		public final MethodList methods = new MethodList();

		MappedClass(String obf, String unobf) {
			super(obf, unobf);
			this.unobfName = unobf;
		}

	}

	public static class MappedField extends MappedMember {

		MappedField(String obf, String srg) {
			super(obf, srg);
		}

	}

	public static class MappedMethod extends MappedMember {

		/**
		 * The obfuscated descriptor of the method.
		 */
		String obfDesc;

		/**
		 * The unobfuscated descriptor of the method.
		 */
		String unobfDesc;

		MappedMethod(String obf, String srg) {
			super(obf, srg);
		}

		/**
		 * @return The method descriptor in the specified mapping.
		 */
		String getDesc(Mapping mapping) {
			if (mapping == OBFUSCATED)
				return obfDesc;

			return unobfDesc;
		}
	}

	public static class MappedMember {

		/**
		 * The obfuscated name of the member.
		 */
		final String obfName;

		/**
		 * The searge (intermediary) name of the member.
		 * This name stays consistent for every mapping version targeting the same minecraft version.
		 * @see Mapping#SEARGE
		 */
		final String srgName;

		/**
		 * The unobfuscated name of the member.
		 */
		String unobfName;

		MappedMember(String obfName, String srgName) {
			this.obfName = obfName;
			this.srgName = srgName;
		}

		/**
		 * @return The name in the specified mapping.
		 *         If the unobfuscated name is requested but does not exist,
		 *         the intermediary name will be returned instead.
		 */
		public String getName(Mapping mapping) {
			switch (mapping) {
				case OBFUSCATED:
					return obfName;
				case UNOBFUSCATED:
					if (unobfName != null)
						return unobfName;
					// fall-through
				default:
					return srgName;
			}
		}
	}

	/**
	 * An {@link ArrayList} with a cache for every mapping type to achieve faster lookup.
	 */
	public static class MappedList<M extends MappedMember> extends ArrayList<M> {

		/**
		 * A member storage where the key is the obfuscated name.
		 */
		public transient final Map<String, M> obfMap = new HashMap<>();

		/**
		 * A member storage where the key is the searge name.
		 */
		public transient final Map<String, M> srgMap = new HashMap<>();

		/**
		 * A member storage where the key is the unobfuscated name.
		 */
		public transient final Map<String, M> unobfMap = new HashMap<>();

		/**
		 * populates the cache maps.
		 */
		void create() {
			for (M member : this) {
				obfMap.put(member.obfName, member);
				srgMap.put(member.srgName, member);
				unobfMap.put(member.unobfName, member);

				if (member instanceof MappedClass) {
					((MappedClass) member).fields.create();
					((MappedClass) member).methods.create();
				}
			}
		}

		public M get(String key, Mapping mapping) {
			switch (mapping) {
				case OBFUSCATED:
					return obfMap.get(key);
				case SEARGE:
					return srgMap.get(key);
				default:
					return unobfMap.get(key);
			}
		}
	}

	/**
	 * A {@link MappedList} where the lookup keys include the corresponding method descriptors.
	 */
	public static class MethodList extends MappedList<MappedMethod> {

		/**
		 * populates the cache maps for faster mapping lookup.
		 */
		@Override
		void create() {
			for (MappedMethod method : this) {
				obfMap.put(method.obfName + method.obfDesc, method);
				srgMap.put(method.srgName + method.unobfDesc, method);
				unobfMap.put(method.unobfName + method.unobfDesc, method);
			}
		}

	}
}
