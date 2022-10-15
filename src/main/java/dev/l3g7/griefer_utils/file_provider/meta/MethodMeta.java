/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 *
 * Copyright 2020-2022 L3g7
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.l3g7.griefer_utils.file_provider.meta;

import org.objectweb.asm.Type;
import org.objectweb.asm.tree.MethodNode;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

import static dev.l3g7.griefer_utils.util.ArrayUtil.flatmap;
import static dev.l3g7.griefer_utils.util.ArrayUtil.map;
import static dev.l3g7.griefer_utils.util.Util.elevate;

/**
 * Meta information of a method.
 */
public class MethodMeta implements IMeta {

	public final ClassMeta owner;
	public final String name;
	public final String desc;
	public final int modifiers;
	public final List<AnnotationMeta> annotations;
	private Method loadedMethod = null;

	/**
	 * Load the information from ASM's {@link MethodNode}.
	 */
	public MethodMeta(ClassMeta owner, MethodNode data) {
		this.owner = owner;
		this.name = data.name;
		this.desc = data.desc;
		this.modifiers = data.access;
		this.annotations = data.visibleAnnotations == null ? Collections.emptyList() : map(data.visibleAnnotations, AnnotationMeta::new);
	}

	/**
	 * Load the information from Reflection's {@link Method}.
	 */
	public MethodMeta(ClassMeta owner, Method method) {
		this.owner = owner;
		this.name = method.getName();
		this.desc = Type.getMethodDescriptor(method);
		this.modifiers = method.getModifiers();
		this.annotations = map(method.getAnnotations(), AnnotationMeta::new);
		this.loadedMethod = method;
	}


	/**
	 * Loads the method.
	 */
	public Method load() {
		if (loadedMethod != null)
			return loadedMethod;

		Class<?> ownerClass = owner.load();

		// Search for method in ownerClass
		for (Method method : flatmap(Method.class, ownerClass.getDeclaredMethods(), ownerClass.getMethods()))
			if (name.equals(method.getName()) && desc.equals(Type.getMethodDescriptor(method)))
				return loadedMethod = method;

		throw elevate(new NoSuchMethodException(), "Could not find method %s %s in %s!", name, desc, owner.name);
	}

	@Override
	public List<AnnotationMeta> getAnnotations() {
		return annotations;
	}

	@Override
	public int getModifiers() {
		return modifiers;
	}

	@Override
	public String toString() {
		return owner + "." + name + desc;
	}

}
