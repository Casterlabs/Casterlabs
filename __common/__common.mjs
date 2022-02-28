
const THEME_DATA = {
    "co.casterlabs.light": {
        id: "co.casterlabs.light",
        name: "Light",
        isAuto: false,
        css: "/css/themes/co.casterlabs.light.css"
    },
    "co.casterlabs.dark": {
        id: "co.casterlabs.dark",
        name: "Dark",
        isAuto: false,
        css: "/css/themes/co.casterlabs.dark.css"
    }
};

let themeStylesheet;

export const themes = Object.values(THEME_DATA); // Yuck!

export function changeTheme(id) {
    if (id == "system") {
        applySystemTheme();
        return;
    }

    const theme = THEME_DATA[id];

    if (!theme) {
        throw "Invalid theme id: " + id;
    }

    applyTheme(theme);
}

export function getCurrentTheme() {
    if (themeStylesheet) {
        return THEME_DATA[themeStylesheet.getAttribute("data-theme-id")];
    }
}

function applyTheme(theme, id = theme.id) {
    themeStylesheet.href = theme.css;
    themeStylesheet.setAttribute("data-theme-id", id);
    themeStylesheet.setAttribute("data-theme-name", theme.name);
}

function applySystemTheme() {
    if (window.matchMedia("(prefers-color-scheme: dark)").matches) {
        applyTheme(THEME_DATA["co.casterlabs.dark"], "system");
    } else {
        applyTheme(THEME_DATA["co.casterlabs.light"], "system");
    }
}

// The listener code for system theme.
// We only add the system theme if matchMedia is available.
if (window.matchMedia) {
    THEME_DATA.system = {
        "id": "system",
        "name": "Follow System",
        isAuto: true
    };

    window
        .matchMedia("(prefers-color-scheme: dark)")
        .addEventListener("change", () => {
            if (getCurrentTheme().isAuto) {
                applySystemTheme();
            }
        });
}

// Try to get the existing element if it exists.
// Otherwise, create a new one.
if (!themeStylesheet) {
    themeStylesheet = document.querySelector("link[rel=stylesheet][data-what=theme-stylesheet]");

    if (!themeStylesheet) {
        themeStylesheet = document.createElement("link");
        themeStylesheet.setAttribute("data-what", "theme-stylesheet");
        themeStylesheet.rel = "stylesheet";
        document.head.appendChild(themeStylesheet);
    }
}

// Load the user"s preferred theme from local storage.
try {
    if (window.localStorage) {
        const storedTheme = localStorage.getItem("cl:theme");

        if (storedTheme) {
            changeTheme(storedTheme);
        }
    }
} catch (e) {
    console.error("An error occurred whilst loading selected theme:", e, "\nLoading default.");
}

// No theme was selected, so load the default.
if (!getCurrentTheme()) {
    changeTheme("co.casterlabs.light");
}