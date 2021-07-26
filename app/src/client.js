import * as sapper from "@sapper/app";
import SideBar from "./components/side-bar.svelte";

sapper.start({
	target: document.querySelector("#sapper")
}).then(() => {
	const sideBarContainer = document.querySelector("#side-bar");

	new SideBar({
		target: sideBarContainer
	});
});
