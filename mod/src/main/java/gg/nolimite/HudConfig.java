package gg.nolimite;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.fabricmc.loader.api.FabricLoader;

import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

/** Speichert Position, Größe und An/Aus aller HUD-Elemente in config/nolimite.json */
public class HudConfig {
    public static class Entry {
        public double x, y;
        public float scale = 1f;
        public boolean enabled = true;
        Entry(double x, double y) { this.x = x; this.y = y; }
    }

    public static final Map<String, Entry> ENTRIES = new LinkedHashMap<>();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static void defaults() {
        ENTRIES.put("fps", new Entry(10, 10));
        ENTRIES.put("coords", new Entry(10, 26));
        ENTRIES.put("scoreboard", new Entry(0, 0)); // Offset zur Standardposition
    }

    public static Entry get(String id) { return ENTRIES.get(id); }

    private static Path file() { return FabricLoader.getInstance().getConfigDir().resolve("nolimite.json"); }

    public static void load() {
        defaults();
        try {
            if (Files.exists(file())) {
                try (Reader r = Files.newBufferedReader(file())) {
                    Map<String, Entry> saved = GSON.fromJson(r, new TypeToken<Map<String, Entry>>() {}.getType());
                    if (saved != null) saved.forEach((k, v) -> { if (ENTRIES.containsKey(k) && v != null) ENTRIES.put(k, v); });
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    public static void save() {
        try (Writer w = Files.newBufferedWriter(file())) { GSON.toJson(ENTRIES, w); } catch (Exception e) { e.printStackTrace(); }
    }

    public static void reset() { ENTRIES.clear(); defaults(); }
}
