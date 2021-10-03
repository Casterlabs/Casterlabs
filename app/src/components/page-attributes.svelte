<script>
    export let title = "";
    export let allowNavigateBackwards = false;
</script>

<div class="hidden">
    <!-- Page attributes -->
    <span data-page-attr="title">{title}</span>
    <span data-page-attr="allow-navigate-backwards" data-page-attr-type="boolean">{allowNavigateBackwards}</span>

    <script type="module">
        const currentPage = document.querySelector(".title-bar .current-page");

        window.getPageAttribute = function (attributeName) {
            const attrElement = document.querySelector(`[data-page-attr=${JSON.stringify(attributeName)}]`);

            const value = attrElement?.innerText ?? null;
            const type = attrElement?.getAttribute("data-page-attr-type") ?? "string";

            if (value) {
                switch (type) {
                    case "boolean": {
                        return value == "true";
                    }

                    case "number": {
                        return parseFloat(value);
                    }

                    default: {
                        return value;
                    }
                }
            } else {
                return null;
            }
        };

        // Grab the passed page title.
        const pageTitle = getPageAttribute("title");
        if (pageTitle && pageTitle.length > 0) {
            currentPage.innerText = `Caffeinated - ${pageTitle}`;
            document.title = `Casterlabs Caffeinated - ${pageTitle}`;
        } else {
            currentPage.innerText = "Caffeinated";
            document.title = "Casterlabs Caffeinated";
        }
    </script>
</div>
