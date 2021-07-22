
function isDarkMode() {
    const htmlElement = document.querySelector("html");

    return htmlElement.classList.contains("bulma-dark-mode");
}

function setDarkMode(enabled = !isDarkMode()) {
    const htmlElement = document.querySelector("html");

    if (enabled && !htmlElement.classList.contains("bulma-dark-mode")) {
        htmlElement.classList.add("bulma-dark-mode");
    } else {
        htmlElement.classList.remove("bulma-dark-mode");
    }
}

export {
    isDarkMode,
    setDarkMode
};