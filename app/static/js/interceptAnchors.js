
new MutationObserver(() => {
    for (const anchor of document.querySelectorAll("a")) {

        // Intercept all rel="external" anchors and add a click listener.
        if (
            (anchor.getAttribute("rel") == "external") &&
            !anchor.getAttribute("intercepted")
        ) {

            // On click we prevent the navigation and openLink with the href.
            anchor.setAttribute("intercepted", true);
            anchor.addEventListener("click", (e) => {
                e.preventDefault();
                openLink(anchor.href);
            });
        }
    }
}).observe(document.body, {
    subtree: true,
    childList: true
});
