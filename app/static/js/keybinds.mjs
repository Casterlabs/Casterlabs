
function isNavigateBackwardsAllowed() {
    return getPageAttribute("allow-navigate-backwards");
}

document.addEventListener("keyup", (e) => {
    if (e.ctrlKey) {
        switch (e.key) {

            case "Backspace": {
                if (isNavigateBackwardsAllowed()) {
                    history.back();
                }
                break;
            }

        }
    }
});
