import { appStore } from "./caffeinated.mjs";

/* -------- */
/* Theme    */
/* -------- */

function getTheme() {
    return appStore.get("appearance.theme");
}

function setTheme(theme) {
    const htmlElement = document.querySelector("html");

    switch (theme) {
        case "light": {
            htmlElement.classList.remove("bulma-dark-mode");
            appStore.set("appearance.theme", "light");
            return;
        }

        case "dark":
        default: {
            htmlElement.classList.add("bulma-dark-mode");
            appStore.set("appearance.theme", "dark");
            return;
        }
    }
}

/* -------- */
/* Logo     */
/* -------- */

function getLogo() {
    return appStore.get("appearance.logo");
}

function setLogo(logo) {
    if (![
        "casterlabs",
        "pride",
        "moonlabs"
    ].includes(logo)) {
        logo = "casterlabs";
    }

    appStore.set("appearance.logo", logo);

    const appPath = app.getAppPath();

    currentWindow.setIcon(`${appPath}/__sapper__/export/logo/${logo}.png`);

    updateLogoImages();
}

// Updates all images with the class app-logo
function updateLogoImages() {
    const logo = getLogo();

    for (const img of document.querySelectorAll("img.app-logo")) {
        img.src = `/logo/${logo}.svg`;
    }
}


// Init
setTheme(getTheme());
setLogo(getLogo());


export {
    getTheme,
    setTheme,

    getLogo,
    setLogo,
    updateLogoImages,

};