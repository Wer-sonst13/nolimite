const { app, BrowserWindow, ipcMain, shell } = require("electron");
const fs = require("fs"), path = require("path"), os = require("os");
const { Client } = require("minecraft-launcher-core");
const { Auth } = require("msmc");
const { autoUpdater } = require("electron-updater");

const ROOT = path.join(os.homedir(), ".nolimite");          // geteilt: versions/libraries/assets
const INST = path.join(ROOT, "instances");                   // pro Instanz: mods/saves/...
const DB = path.join(ROOT, "instances.json");
const UA = { "User-Agent": "NoLimite/0.1" };
let win, account = null;

// NoLimite-Mod (HUD-Editor) automatisch in passende Profile kopieren
const modJar = () => { const j = app.isPackaged ? path.join(process.resourcesPath, "nolimite-mod.jar") : path.join(__dirname, "resources", "nolimite-mod.jar"); return fs.existsSync(j) ? j : null; };
const syncMod = (inst) => { const j = modJar(); if (!j || inst.mc !== "1.21.4") return; const d = path.join(INST, inst.id, "mods"); fs.mkdirSync(d, { recursive: true }); fs.copyFileSync(j, path.join(d, "nolimite.jar")); };
const jget = async (u) => (await fetch(u, { headers: UA })).json();
const read = () => (fs.existsSync(DB) ? JSON.parse(fs.readFileSync(DB, "utf8")) : []);
const write = (d) => { fs.mkdirSync(ROOT, { recursive: true }); fs.writeFileSync(DB, JSON.stringify(d, null, 2)); };
const log = (m) => win.webContents.send("log", m);

app.whenReady().then(() => {
  win = new BrowserWindow({ width: 1100, height: 720, minWidth: 900, minHeight: 600, frame: true, backgroundColor: "#050505",
    autoHideMenuBar: true, webPreferences: { nodeIntegration: true, contextIsolation: false } });
  win.loadFile("index.html");
  if (app.isPackaged) {                       // Auto-Update nur in der installierten Version
    autoUpdater.on("update-available", () => log("Update gefunden, wird geladen ..."));
    autoUpdater.on("update-downloaded", () => autoUpdater.quitAndInstall());
    autoUpdater.checkForUpdates().catch(() => {});
  }
});

ipcMain.handle("versions", async () => (await jget("https://meta.fabricmc.net/v2/versions/game")).filter(v => v.stable).map(v => v.version).slice(0, 40));
ipcMain.handle("instances", () => read());

ipcMain.handle("create", async (_, { name, mc, mods }) => {
  const loader = (await jget(`https://meta.fabricmc.net/v2/versions/loader/${mc}`))[0].loader.version;
  const prof = await jget(`https://meta.fabricmc.net/v2/versions/loader/${mc}/${loader}/profile/json`);
  const vdir = path.join(ROOT, "versions", prof.id);
  fs.mkdirSync(vdir, { recursive: true });
  fs.writeFileSync(path.join(vdir, prof.id + ".json"), JSON.stringify(prof));
  const id = "nl-" + Date.now(), dir = path.join(INST, id, "mods");
  fs.mkdirSync(dir, { recursive: true });
  await installMods(mc, dir, mods);
  const list = read(); list.unshift({ id, name, mc, loader, versionId: prof.id, created: Date.now(), installed: mods }); write(list); syncMod(list[0]);
  return list;
});

ipcMain.handle("delete", (_, id) => { fs.rmSync(path.join(INST, id), { recursive: true, force: true }); const l = read().filter(i => i.id !== id); write(l); return l; });

ipcMain.handle("login", async () => {
  const xbox = await new Auth("select_account").launch("electron");
  const mc = await xbox.getMinecraft();
  account = mc;
  return { name: mc.profile.name, uuid: mc.profile.id };
});

ipcMain.handle("launch", async (_, { id, ram }) => {
  if (!account) throw new Error("Bitte zuerst mit Microsoft anmelden.");
  const inst = read().find(i => i.id === id);
  syncMod(inst);
  const l = new Client();
  l.on("debug", log); l.on("data", log);
  l.on("progress", p => win.webContents.send("progress", Math.round((p.task / p.total) * 100)));
  await l.launch({
    authorization: account.mclc(), root: ROOT,
    version: { number: inst.mc, type: "release", custom: inst.versionId },
    memory: { max: (ram || 4) + "G", min: "1G" },
    overrides: { gameDirectory: path.join(INST, inst.id) },
  });
  return true;
});

async function installMods(mc, dir, mods) {
  fs.mkdirSync(dir, { recursive: true });
  const done = new Set();
  const get = async (p) => {
    if (done.has(p)) return; done.add(p);
    try {
      const q = `game_versions=${encodeURIComponent(JSON.stringify([mc]))}&loaders=${encodeURIComponent('["fabric"]')}`;
      const vs = await jget(`https://api.modrinth.com/v2/project/${p}/version?${q}`);
      if (!vs.length) return log(`- ${p}: nicht verfügbar für ${mc}`);
      const f = vs[0].files.find(x => x.primary) || vs[0].files[0];
      fs.writeFileSync(path.join(dir, f.filename), Buffer.from(await (await fetch(f.url, { headers: UA })).arrayBuffer()));
      log("+ " + f.filename);
      for (const d of vs[0].dependencies) if (d.dependency_type === "required" && d.project_id) await get(d.project_id);
    } catch (e) { log(`- ${p}: ${e.message}`); }
  };
  for (const m of mods) await get(m);
}
ipcMain.handle("search", async (_, { q, mc, cat, sort, offset }) => {
  const facets = [["project_type:mod"], ["categories:fabric"], ["versions:" + mc]];
  if (cat) facets.push(["categories:" + cat]);
  const url = `https://api.modrinth.com/v2/search?query=${encodeURIComponent(q || "")}&facets=${encodeURIComponent(JSON.stringify(facets))}&index=${sort || "relevance"}&limit=20&offset=${offset || 0}`;
  const r = await jget(url);
  return { total: r.total_hits, hits: r.hits.map(h => ({ slug: h.slug, title: h.title, desc: h.description, icon: h.icon_url, dl: h.downloads, author: h.author, cats: h.display_categories })) };
});
ipcMain.handle("addmods", async (_, { id, projects }) => {
  const list = read(), inst = list.find(i => i.id === id);
  await installMods(inst.mc, path.join(INST, id, "mods"), projects);
  inst.installed = [...new Set([...(inst.installed || []), ...projects])]; write(list);
  return inst.installed;
});
ipcMain.handle("folder", (_, id) => shell.openPath(path.join(INST, id)));
