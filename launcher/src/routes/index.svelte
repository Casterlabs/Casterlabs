<script>
	import LoadingSpinner from "../components/loading-spinner.svelte";
	import RandomStreamer from "../components/random-streamer.svelte";
</script>

<style>
	.casterlabs-banner {
		position: absolute;
		top: 7px;
		left: 5px;
	}

	.welcome {
		position: absolute;
		top: 100px;
		left: 12px;
	}

	.welcome h1 {
		font-size: 1.5em;
		margin-bottom: 0;
	}

	.loading {
		position: absolute;
		bottom: 12px;
		left: 12px;
		width: 275px;
		height: 100px;
	}

	.loading-spinner {
		position: absolute;
		bottom: 3px;
		left: 0;
		width: 35px;
		height: 35px;
	}

	.loading-status {
		color: #dbdbdb;
		font-size: .9em;
		position: absolute;
		bottom: 0;
		left: 40px;
		height: 40px;
	}

	.random-streamer {
		position: absolute;
		top: 0;
		bottom: 0;
		right: 0;
		width: 250px;
	}
</style>

<div class="random-streamer">
	<RandomStreamer />
</div>

<div>
	<img class="casterlabs-banner" alt="Casterlabs" src="banner-white.png" />
</div>

<div class="loading">
	<div class="loading-spinner">
		<LoadingSpinner />
	</div>
	<div class="loading-status">
		<span id="status-line-1">
			Downloading updated app file
		</span>
		<br />
		<span id="status-line-2">
			5% (2.1MB / 32.6MB)
		</span>
	</div>
</div>

<div class="welcome">
	<h1>Welcome to Casterlabs</h1>
	<span>
		The free stream widgets service.
	</span>
</div>