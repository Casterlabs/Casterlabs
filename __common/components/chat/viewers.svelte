<script>
    import { createEventDispatcher, onMount } from "svelte";

    const dispatch = createEventDispatcher();

    let platforms = {};
    let viewersList = [];

    let draggable;
    let rootElement;

    class Draggable {
        constructor(element, options = {}) {
            this.element = null;
            this.parent = null;
            this.eventListeners = {};
            this.posX = {};
            this.posY = {};
            this.enabled = true;

            const {
                limit = true,

                posX = 0,
                posY = 0,

                width = 1,
                height = 1,

                minWidth = 150,
                minHeight = 300,

                maxWidth = 150,
                maxHeight = 300,
            } = options;

            this.posX = posX;
            this.posY = posY;

            let dragging = false;
            let startX = 0;
            let startY = 0;
            let mouseX;
            let mouseY;

            this.element = element;
            this.parent = this.element.parentElement;

            // Set the styles
            {
                this.element.style.width = `${width}px`;
                this.element.style.height = `${height}px`;

                this.element.style.minWidth = `${minWidth}px`;
                this.element.style.minHeight = `${minHeight}px`;

                this.element.style.maxWidth = `${maxWidth}px`;
                this.element.style.maxHeight = `${maxHeight}px`;

                this.element.style.cursor = "move";
            }

            // new ResizeObserver((e) => {
            //     this.update();
            //     this.broadcast("resize");
            // }).observe(this.element);

            this.element.addEventListener("mousedown", (e) => {
                if (this.enabled && !e.shiftKey) {
                    e.preventDefault();

                    dragging = true;

                    startX = e.clientX;
                    startY = e.clientY;
                }
            });

            document.addEventListener(
                "mousemove",
                (e) => {
                    if (dragging) {
                        const parentWidth = this.parent.offsetWidth;
                        const parentHeight = this.parent.offsetHeight;

                        if (!mouseX) {
                            mouseX = this.posX;
                            mouseY = this.posY;
                        }

                        console.log(mouseX, mouseY);

                        mouseX -= startX - e.clientX;
                        mouseY -= startY - e.clientY;

                        console.log(mouseX, mouseY);

                        if (limit) {
                            const maxX = parentWidth;
                            const maxY = parentHeight;

                            if (mouseX > maxX) {
                                this.posX = maxX;
                            } else if (mouseX < 0) {
                                this.posX = 0;
                            } else {
                                this.posX = mouseX;
                            }

                            if (mouseY > maxY) {
                                this.posY = maxY;
                            } else if (mouseY < 0) {
                                this.posY = 0;
                            } else {
                                this.posY = mouseY;
                            }
                        } else {
                            this.posX = mouseX;
                            this.posY = mouseY;
                        }

                        startX = e.clientX;
                        startY = e.clientY;

                        this.update();
                        this.broadcast("move");
                    }
                },
                false
            );

            document.addEventListener("mouseup", () => {
                dragging = false;
            });

            this.update();
        }

        on(type, listener) {
            const arr = this.eventListeners[type.toLowerCase()] ?? [];

            arr.push(listener);

            this.eventListeners[type.toLowerCase()] = arr;
        }

        broadcast(type) {
            const listeners = this.eventListeners[type.toLowerCase()];

            if (listeners) {
                listeners.forEach((listener) => {
                    try {
                        listener(this);
                    } catch (e) {
                        console.error(
                            "An event listener produced an exception: "
                        );
                        console.error(e);
                    }
                });
            }
        }

        update() {
            // These may seem flipped, but it's intentional.
            {
                this.element.style.top = this.posY + "px";
                this.element.style.left = this.posX + "px";
            }

            // Do this so it will always match the container dimensions properly.
            {
                const [width, height] = this.getSize();

                this.element.style.width = `${width * 100}%`;
                this.element.style.height = `${height * 100}%`;
            }

            this.broadcast("update");
        }

        getElement() {
            return this.element;
        }

        getPosition() {
            return [this.posX, this.posY];
        }

        setPosition(posX, posY) {
            this.posX = posX;
            this.posY = posY;

            this.update();
        }

        getSize() {
            let width = this.element.style.width;

            if (width.endsWith("%")) {
                width = parseFloat(width.substring(0, width.length - 1)) / 100;
            } else {
                const parentWidth = this.parent.offsetWidth;

                width = parseFloat(width.substring(0, width.length - 2)); // px

                width = width / parentWidth;
            }

            let height = this.element.style.height;

            if (height.endsWith("%")) {
                height =
                    parseFloat(height.substring(0, height.length - 1)) / 100;
            } else {
                const parentHeight = this.parent.offsetHeight;

                height = parseFloat(height.substring(0, height.length - 2)); // px

                height = height / parentHeight;
            }

            return [width, height];
        }

        setSize(width, height) {
            this.element.style.width = `${width * 100}%`;
            this.element.style.height = `${height * 100}%`;

            this.update();
        }
    }

    onMount(() => {
        /*document.addEventListener("keydown", (e) => {
            if (e.key == "Shift") {
                Array.from(document.querySelectorAll(".draggable")).forEach(
                    (elem) => {
                        elem.style.cursor = "auto";
                        elem.style.resize = "both";
                    }
                );
            }
        });

        document.addEventListener("keyup", (e) => {
            if (e.key == "Shift") {
                Array.from(document.querySelectorAll(".draggable")).forEach(
                    (elem) => {
                        elem.style.cursor = "move";
                        elem.style.resize = "none";
                    }
                );
            }
        });

        window.parent?.addEventListener("keydown", (e) => {
            if (e.key == "Shift") {
                Array.from(document.querySelectorAll(".draggable")).forEach(
                    (elem) => {
                        elem.style.cursor = "auto";
                        elem.style.resize = "both";
                    }
                );
            }
        });

        window.parent?.addEventListener("keyup", (e) => {
            if (e.key == "Shift") {
                Array.from(document.querySelectorAll(".draggable")).forEach(
                    (elem) => {
                        elem.style.cursor = "move";
                        elem.style.resize = "none";
                    }
                );
            }
        });*/

        draggable = new Draggable(rootElement, {});

        draggable.on("update", () => {
            const [x, y] = draggable.getPosition();

            dispatch("move", {
                x: x,
                y: y,
            });
        });
    });

    export function setPosition(x, y) {
        draggable.setPosition(x, y);
    }

    export function getPosition() {
        return draggable.getPosition();
    }

    export function onViewersList(e) {
        console.log(e);
        platforms[e.streamer.platform] = e.viewers;

        updateViewersList();
    }

    export function onAuthUpdate(signedInPlatforms) {
        for (const platform of Object.keys(platforms)) {
            if (!signedInPlatforms.includes(platform)) {
                delete platforms[platform];
            }
        }

        updateViewersList();
    }

    function updateViewersList() {
        let list = [];

        for (const viewers of Object.values(platforms)) {
            list.push(...viewers);
        }

        viewersList = list;
    }
</script>

<div class="viewers-list-container">
    <div id="viewers-list" bind:this={rootElement} class="draggable">
        <div class="container box">
            <span id="total-count">
                <svg
                    xmlns="http://www.w3.org/2000/svg"
                    width="14"
                    height="14"
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    stroke-width="2"
                    stroke-linecap="round"
                    stroke-linejoin="round"
                    class="feather feather-eye"
                    style="transform: translateY(1.5px);"
                >
                    <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z" />
                    <circle cx="12" cy="12" r="3" />
                </svg>
                {viewersList.length}
            </span>

            <ul class="allow-select">
                {#each viewersList as viewer}
                    <li>
                        {viewer.displayname}
                    </li>
                {/each}
            </ul>
        </div>
    </div>
</div>

<style>
    .viewers-list-container {
        position: fixed;
        pointer-events: none;
        top: 0;
        bottom: 0;
        left: 0;
        right: 0;
        z-index: 0;
    }

    #viewers-list {
        position: absolute;
        pointer-events: all;
    }

    .container {
        height: 100%;
        margin: 5px;
        position: relative;
        overflow-y: auto;
        overflow-x: hidden;
        opacity: 0.65;
    }

    #total-count {
        position: absolute;
        top: 5px;
        right: 9px;
        text-align: right;
    }
</style>
