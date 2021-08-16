import * as Modules from "./js/modules/modules.mjs";
import { moduleStore } from "./js/caffeinated.mjs";
import { getRandomItemInArray } from "./js/util/misc.mjs";

// Init the default widgets
Modules.registerRepo(
    isDev ?
        `${process.cwd()}/../default-modules` :
        `./default-modules`
).then(() => {
    for (const [namespace, modules] of Object.entries(moduleStore.store)) {
        const moduleEntries = Object.entries(modules);

        // Delete empty entries
        if (moduleEntries.length == 0) {
            moduleStore.delete(namespace);
            continue;
        }

        for (const [moduleId, moduleData] of moduleEntries) {
            // Static modules are already initialized.
            if (Modules.getStaticModules()[namespace]) {
                continue;
            }

            const holder = Modules.getDynamicModuleHolders()[namespace];
            const moduleName = moduleData.name;

            if (holder) {
                holder
                    .create(moduleData.name, moduleId)
                    .then(() => {
                        // I'm a comedy master. - Lcyx
                        console.debug("[postInit.js]", getRandomItemInArray([
                            `Successfully brought ${moduleName} back from cryosleep, welcome back commander. o7`,
                            `Successfully raised the dead (${moduleName})`,
                            `\n\tPavel Chekov > Captain on the bridge.\n\tCpt. ${moduleName} > Mr. Scott, how are we looking?\n\tMontgomery Scott > Purring like a kitten captain, she's ready for a long journey.`,
                            `\n\t${moduleName} > Bring me my coffee.\n\tpostInit.js > Sir, Yes Sir (￣^￣ )ゞ`
                        ]));
                    });
            } else {
                console.debug("[postInit.js]", moduleName, "no longer exists, removing it.");
                moduleStore.delete(`${namespace}.${moduleId}`);
            }
        }
    }
});