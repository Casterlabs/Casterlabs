import ModuleInstance from "./module-instance.mjs";
import { moduleStore } from "../caffeinated.mjs";
import { generateUUID } from "../util/misc.mjs";

let dynamicModuleHolders = {};
let dynamicModules = {};

let staticModules = {};

let loadedRepos = {};

async function createModule(baseUrl, isStatic, moduleDeclaration, name, id) {
    const { namespace, location } = moduleDeclaration;
    const widgetBaseUrl = `${baseUrl}/${location}`;
    const { scripts, settings } = await (await fetch(`${widgetBaseUrl}/module.json`)).json();
    const fullId = `${namespace}:${id}`;

    if (staticModules[fullId] || dynamicModules[fullId]) {
        throw "Module already registered.";
    } else {
        let storedSettings = moduleStore.get(`${namespace}.${id}.settings`) ?? {};
        let defaultSettings = {};

        if (settings) {
            for (const [key, meta] of Object.entries(settings)) {
                defaultSettings[key] = meta.defaultValue;

                if (!storedSettings.hasOwnProperty(key)) {
                    storedSettings[key] = meta.defaultValue;
                }
            }
        }

        let scriptLocations = [];

        for (const script of scripts) {
            if (script.startsWith("./")) {
                scriptLocations.push(`${widgetBaseUrl}/${script.substring(2)}`);
            } else {
                scriptLocations.push(script);
            }
        }

        const instance = new ModuleInstance(namespace, id, name, scriptLocations, storedSettings, defaultSettings);

        instance.destroyHandlers.push(() => {
            if (isStatic) {
                delete staticModules[fullId];
            } else {
                delete dynamicModules[fullId];
            }
        });

        if (isStatic) {
            staticModules[fullId] = instance;
        } else {
            dynamicModules[fullId] = instance;
        }

        return instance;
    }
}

async function registerRepo(baseUrl) {
    if (loadedRepos[baseUrl]) {
        throw "Repo is already loaded."
    } else {
        const modulesManifest = await (await fetch(`${baseUrl}/modules.json`)).json();

        let _staticModules = [];
        let _dynamicModuleHolders = [];

        for (const declaration of modulesManifest.dynamic) {
            if (dynamicModuleHolders[declaration.namespace]) {
                throw `Dynamic module holder of namespace "${declaration.namespace}" is already registered.`
            } else {
                const holder = new DynamicModuleHolder(declaration, baseUrl);

                _dynamicModuleHolders.push(holder);
                dynamicModuleHolders[holder.namespace] = holder;
            }
        }

        for (const declaration of modulesManifest.static) {
            const instance = await createModule(baseUrl, true, declaration, declaration.name, declaration.id);
            _staticModules.push(instance);
        }

        const repoInstance = new RepoInstance(baseUrl, _dynamicModuleHolders, _staticModules);

        loadedRepos[baseUrl] = repoInstance;

        return repoInstance;
    }
}

class DynamicModuleHolder {
    #declaration = null;
    #baseUrl = null;
    #runningModules = {};

    constructor(declaration, baseUrl) {
        this.#declaration = declaration;
        this.#baseUrl = baseUrl;
    }

    get namespace() {
        return this.#declaration.namespace;
    }

    get declaration() {
        return this.#declaration;
    }

    async create(name, id = generateUUID()) {
        const instance = await createModule(this.#baseUrl, false, this.#declaration, name, id);

        this.#runningModules[id] = instance;

        instance.destroyHandlers.push(() => {
            delete this.#runningModules[id];
        });
    }

    destroyAll() {
        for (const dynamic of Object.values(this.#runningModules)) {
            dynamic.destroy();
        }

        delete dynamicModuleHolders[this.namespace]
    }

}

class RepoInstance {
    #baseUrl = null;

    #dynamicModuleHolders = null;
    #staticModules = null;

    constructor(baseUrl, dynamicModuleHolders, staticModules) {
        this.#baseUrl = baseUrl;
        this.#dynamicModuleHolders = dynamicModuleHolders;
        this.#staticModules = staticModules;
    }

    get baseUrl() {
        return this.#baseUrl;
    }

    async destroy() {
        for (const module of this.#staticModules) {
            module.destroy();
        }

        for (const module of this.#dynamicModuleHolders) {
            module.destroyAll();
        }

        delete loadedRepos[this.baseUrl];
    }

}

function getDynamicModuleHolders() {
    return dynamicModuleHolders;
}

function getDynamicModules() {
    return dynamicModules;
}

function getStaticModules() {
    return staticModules;
}

function getLoadedRepos() {
    return loadedRepos;
}

export {
    registerRepo,
    getDynamicModuleHolders,
    getDynamicModules,
    getStaticModules,
    getLoadedRepos
};