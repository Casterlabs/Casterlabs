<script>
	import PageAttributes from "../components/page-attributes.svelte";
	import HideSideBar from "../components/displaymodes/hide-sidebar.svelte";
	import LoadingSpinner from "../components/loading-spinner.svelte";

	import { onMount } from "svelte";

	let logo = "casterlabs";
	let color = "white";

	onMount(async () => {
		logo = UI.getLogo();

		if ([
			"dark"
		].includes(UI.getTheme())) {
			color = "white";
		} else {
			color = "black";
		}
	});
</script>

<PageAttributes />
<HideSideBar />

<style>
	.casterlabs-wordmark {
		padding-top: 35%;
		transform: translateY(-50%);
	}

	.casterlabs-wordmark img {
		width: 240px;
	}

	.loading-spinner {
		width: 50px;
		height: 50px;
		margin: auto;
	}
</style>

<div class="has-text-centered">
	<div class="casterlabs-wordmark">
		<img src="/img/wordmark/{logo}/{color}.svg" alt="Casterlabs Logo" />

		<div class="loading-spinner">
			<LoadingSpinner />
		</div>
	</div>
</div>