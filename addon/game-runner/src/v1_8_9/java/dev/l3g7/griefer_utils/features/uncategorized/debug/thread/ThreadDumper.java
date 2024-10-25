/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.uncategorized.debug.thread;

import dev.l3g7.griefer_utils.core.settings.types.NumberSetting;
import dev.l3g7.griefer_utils.core.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.features.uncategorized.debug.DebugSettings;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

public class ThreadDumper {

	private static Thread thread = null;
	private static final List<String> dumps = new ArrayList<>();
	private static final File FILE = new File("GrieferUtils/threaddumps.txt");

	private static final SwitchSetting dumpAll = SwitchSetting.create()
		.name("Nur Client Thread dumpen")
		.description("Ob alle Threads, oder nur der Client / Mainthread gedumpt werden sollen.")
		.defaultValue(true)
		.icon(Items.nether_star);

	private static final NumberSetting interval = NumberSetting.create()
		.name("Intervall")
		.description("Wie viel Zeit zwischen Dumps vergehen soll (in Millisekunden).")
		.icon(Items.clock)
		.min(1)
		.defaultValue(1000);

	private static final NumberSetting maxDumps = NumberSetting.create()
		.name("Maximale Dumps")
		.description("Wie viel Dumps gespeichert werden sollen.", "Vorherige Dumps werden gelöscht.")
		.icon(Blocks.hopper)
		.min(1)
		.defaultValue(60)
		.callback(dumps::clear);

	public static final SwitchSetting enabled = SwitchSetting.create()
		.name("Thread-Dumper")
		.description("Dumpt in regelmäßigen Abständen die Stacktraces laufender Threads.", "Die Dumps werden in GrieferUtils/threaddumps.txt geschrieben.")
		.icon(Items.string)
		.subSettings(interval, maxDumps, dumpAll)
		.callback(enabled -> {
			if (DebugSettings.enabled.get())
				tryStartThread();
		});

	public static void tryStartThread() throws IOException {
		if (enabled.get() && thread != null)
			return;

		FILE.createNewFile();
		thread = new Thread(() -> {
			while (enabled.get()) {
				dumps.add(ThreadDumpGenerator.generateThreadDumps(dumpAll.get()));
				if (dumps.size() > maxDumps.get())
					dumps.remove(0);

				try {
					try (PrintStream ps = new PrintStream(FILE)) {
						for (String dump : dumps) {
							ps.println(dump);
							ps.println("\n");
						}
					}
				} catch (IOException e) {
					throw new RuntimeException(e);
				}

				try {
					Thread.sleep(interval.get());
				} catch (InterruptedException e) {
					throw new RuntimeException(e);
				}
			}
			thread = null;
		}, "GrieferUtils Thread Dumper");
		thread.start();
	}

}
