<script>
    export let dropdownName;
</script>

<div class="dropdown">
    <div class="dropdown-trigger">
        <button class="button">
            <span>Click me</span>
            <span class="icon is-small">
                <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="feather feather-chevron-down">
                    <polyline points="6 9 12 15 18 9"></polyline>
                </svg>
            </span>
        </button>
    </div>
    <div class="dropdown-menu" role="menu">
        <div class="dropdown-content">
            <slot></slot>
        </div>
    </div>
</div>