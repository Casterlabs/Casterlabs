import * as Modules from "./js/modules/modules.mjs";

// Init the default widgets
Modules.registerRepo(
    isDev ?
        `${process.cwd()}/../default-modules` :
        `./default-modules`
);
