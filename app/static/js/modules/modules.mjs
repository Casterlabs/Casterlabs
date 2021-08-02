import ModuleInstance from "./module-instance.mjs";
import { moduleStore } from "../caffeinated.mjs";

let registeredDynamicModules = {};
let dynamicModules = {};

let staticModules = {};

function createModule(baseUrl, isStatic, moduleDeclaration) {
    const { namespace, id, name, location, settings } = moduleDeclaration;
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

        const instance = new ModuleInstance(namespace, id, name, `${baseUrl}/${location}`, storedSettings, defaultSettings);

        if (isStatic) {
            staticModules[fullId] = instance;
        } else {
            dynamicModules[fullId] = instance;
        }
    }
}

async function registerRepo(baseUrl) {
    const modulesManifest = await (await fetch(`${baseUrl}/modules.json`)).json();

    for (const staticModule of modulesManifest.static) {
        createModule(baseUrl, true, staticModule);
    }

    // TODO DYNAMIC
}

function getRegisteredDynamicModules() {
    return registeredDynamicModules;
}

function getDynamicModules() {
    return dynamicModules;
}

function getStaticModules() {
    return staticModules;
}

export {
    registerRepo,
    getRegisteredDynamicModules,
    getDynamicModules,
    getStaticModules
};