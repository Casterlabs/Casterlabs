<script>
    export let title = "";

</script>

<div class="hide">
    <!-- Page attributes -->
    <span data-name="page-title">{title}</span>
    <displayMode />

    <script type="module">
        const currentPage = document.querySelector(".title-bar .current-page");

        // Grab the passed page title.
        const pageTitle = document.querySelector("[data-name='page-title']").innerText;
        if (pageTitle.length > 0) {
            currentPage.innerText = `Caffeinated - ${pageTitle}`;
            document.title = `Casterlabs Caffeinated - ${pageTitle}`;
        } else {
            currentPage.innerText = "Caffeinated";
            document.title = "Casterlabs Caffeinated";
        }

    </script>
</div>