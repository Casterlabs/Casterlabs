
Module.on("init", ({ name, id, namespace }) => {
    console.debug("My name:", name);
    console.debug("Location:", import.meta.url)
    console.debug(Caffeinated)
    alert("Hello from the Chat Widget!")
});
