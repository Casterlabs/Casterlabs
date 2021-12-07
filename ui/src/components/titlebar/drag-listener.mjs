
class DragListener {

    constructor(element) {
        this.element = null;
        this.parent = null;
        this.eventListeners = {};

        let dragging = false;
        let startX = 0;
        let startY = 0;

        this.element = element;
        this.parent = this.element.parentElement;

        this.element.addEventListener("mousedown", (e) => {
            e.preventDefault();

            dragging = true;

            startX = e.clientX;
            startY = e.clientY;

            this.broadcast("start");
        });

        document.addEventListener("mousemove", (e) => {
            if (dragging) {
                let moveX = (startX - e.clientX);
                let moveY = (startY - e.clientY);

                startX = e.clientX + moveX;
                startY = e.clientY + moveY;

                this.broadcast("move", { moveX, moveY });
            }
        }, false);

        document.addEventListener("mouseup", () => {
            dragging = false;
            this.broadcast("stop");
        });
    }

    on(type, listener) {
        const arr = this.eventListeners[type.toLowerCase()] || [];

        arr.push(listener);

        this.eventListeners[type.toLowerCase()] = arr;
    }

    broadcast(type, data = {}) {
        const listeners = this.eventListeners[type.toLowerCase()];

        if (listeners) {
            listeners.forEach((listener) => {
                try {
                    listener(data);
                } catch (e) {
                    console.error("An event listener produced an exception: ");
                    console.error(e);
                }
            });
        }
    }

    getElement() {
        return this.element;
    }

}

export default DragListener;