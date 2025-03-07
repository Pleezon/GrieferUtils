/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.api.event_bus;

import dev.l3g7.griefer_utils.core.api.file_provider.FileProvider;
import dev.l3g7.griefer_utils.core.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.api.file_provider.meta.ClassMeta;
import dev.l3g7.griefer_utils.core.api.file_provider.meta.MethodMeta;
import dev.l3g7.griefer_utils.core.api.misc.functions.Supplier;
import dev.l3g7.griefer_utils.core.api.reflection.Reflection;
import org.objectweb.asm.Type;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

public class EventRegisterer {

	/**
	 * The pending registrations associated with each event.
	 */
	private static final Map<String, Collection<LazyRegistration>> lazyRegistrations = Collections.synchronizedMap(new ConcurrentHashMap<>());

	/**
	 * Registers the pending registrations for the given event class.<br>
	 * It is triggered at the start of {@link EventBus#fire(Event)}, to load the classes as late as possible.
	 */
	static void handleLazyRegistrations(String eventClass) {
		Collection<LazyRegistration> registrations = lazyRegistrations.remove(eventClass);
		if (registrations == null)
			return;

		for (LazyRegistration registration : registrations)
			EventBus.registerMethod(registration.owner.get(), registration.meta.load());
	}

	/**
	 * Lazy-registers all static event listeners, as well as listeners belonging to a {@link Singleton}.
	 */
	public static void init() {
		for (MethodMeta method : FileProvider.getAnnotatedMethods(EventListener.class)) {
			if (method.isStatic()) {
				registerLazyRegistration(method, () -> method.owner().load());
				continue;
			}

			boolean isSingleton = method.owner().hasAnnotation(Singleton.class);
			if (isSingleton) {
				registerLazyRegistration(method, () -> FileProvider.getSingleton(method.owner().load()));
			} else {
				for (ClassMeta classMeta : FileProvider.getClassesWithSuperClass(method.owner().name))
					if (classMeta.hasAnnotation(Singleton.class))
						registerLazyRegistration(method, () -> FileProvider.getSingleton(classMeta.load()));
			}
		}
	}

	/**
	 * Lazy-registers all non-static event listeners in the given object.
	 */
	public static void register(Object object) {
		if (object instanceof Class<?>)
			return;

		Class<?> clazz = object.getClass();

		for (Method method : Reflection.getAllMethods(clazz)) {
			if (Modifier.isStatic(method.getModifiers()))
				continue;

			if (!method.isAnnotationPresent(EventListener.class))
				continue;

			registerLazyRegistration(new MethodMeta(new ClassMeta(clazz), method), () -> object);
		}
	}

	/**
	 * Unregisters all event listeners and removes all pending registrations associated to the given object.
	 */
	public static void unregister(Object object) {
		synchronized (EventBus.events) {
			EventBus.events.values().removeIf(consumers -> {
				consumers.removeEventsOf(object);
				return consumers.isEmpty();
			});
		}

		boolean isClass = object instanceof Class<?>;
		ClassMeta classMeta = new ClassMeta(isClass ? (Class<?>) object : object.getClass());
		List<MethodMeta> methods = new ArrayList<>();
		for (MethodMeta method : classMeta.methods) {
			if (method.isStatic() != isClass)
				continue;

			if (!method.hasAnnotation(EventListener.class))
				continue;

			methods.add(method);
		}

		synchronized (lazyRegistrations) {
			lazyRegistrations.values().forEach(registrations -> registrations.removeIf(l -> methods.contains(l.meta)));
		}
	}

	/**
	 * Registers a lazy registration consisting of the given input.
	 */
	private static void registerLazyRegistration(MethodMeta method, Supplier<Object> ownerSupplier) {

		// Check count
		Type[] params = Type.getArgumentTypes(method.desc());
		if (params.length != 1)
			throw new IllegalArgumentException("Method " + method + " has @EventListener annotation, but requires " + params.length + " arguments");

		String eventClass = params[0].getClassName();
		Collection<LazyRegistration> registrations = lazyRegistrations.computeIfAbsent(eventClass, s -> new ConcurrentLinkedDeque<>());

		registrations.add(new LazyRegistration(method, ownerSupplier));
	}

	private static class LazyRegistration {

		private final MethodMeta meta;
		private final Supplier<Object> owner; // Supplier is to lazy-load singletons

		private LazyRegistration(MethodMeta meta, Supplier<Object> owner) {
			this.meta = meta;
			this.owner = owner;
		}

	}

}
