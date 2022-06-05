<script>
    import { onMount } from "svelte";
    import Aspect16by9 from "$lib/components/aspect-ratio/Aspect16by9.svelte";

    let addVideoInput = "";

    let animatePlaybackProgress = true;
    let anim_playbackProgress = -1;

    let blockClick = true;

    let player;
    let volume = 0.5;
    let shouldAutoplay = false;

    let videoQueue = [];

    let playing = false;
    let currentPlaybackId = -1;
    let playbackProgress = -1;

    function pollInformation() {
        if (playing) {
            playbackProgress = player.getCurrentTime() / player.getDuration();
        } else {
            playbackProgress = -1;
        }

        if (animatePlaybackProgress) {
            anim_playbackProgress = playbackProgress;
        }

        requestAnimationFrame(pollInformation);
    }

    onMount(() => {
        requestAnimationFrame(pollInformation);

        window.onYouTubeIframeAPIReady = () => {
            player = new YT.Player("video", {
                width: "100%",
                height: "100%",
                playerVars: {
                    controls: 0,
                    disablekb: 1,
                    fs: 0
                },
                events: {
                    onReady: () => {
                        checkMute();
                        player.setVolume(volume * 100);
                    },
                    onStateChange: (event) => {
                        console.log("State: " + event.data);
                        if (event.data == YT.PlayerState.PAUSED) {
                            Widget.emit("play-pause", true);
                        } else if (event.data == YT.PlayerState.PLAYING) {
                            // Widget.emit("seek", player.getCurrentTime());
                            Widget.emit("play-pause", false);
                        } else if (event.data == YT.PlayerState.UNSTARTED) {
                            player.playVideo();
                            blockClick = false;
                            playing = true;
                            Widget.emit("play-start", currentPlaybackId);
                        } else if (event.data == YT.PlayerState.ENDED) {
                            blockClick = true;
                            playing = false;
                            Widget.emit("play-end", currentPlaybackId);
                        }
                    },
                    onError: (event) => {
                        console.log("Error: " + event.data);
                        playing = false;
                        Widget.emit("play-end", currentPlaybackId);
                    }
                }
            });

            window.player = player;
        };
    });

    function checkMute() {
        // if (Widget.getSetting("shouldMute")) {
        //     player.mute();
        // } else {
        player.unMute();
        // }
    }

    function isPaused() {
        const state = player.getPlayerState();
        return state == YT.PlayerState.PAUSED;
    }

    function playPause() {
        if (playing && player.getVideoUrl() != "https://www.youtube.com/watch") {
            if (isPaused()) {
                Widget.emit("play-pause", false);
            } else {
                Widget.emit("seek", player.getCurrentTime());
                Widget.emit("play-pause", true);
            }
        } else if (videoQueue.length > 0) {
            Widget.emit("play", 0);
        }
    }

    Widget.on("seek", (t) => {
        player.seekTo(t);
        animatePlaybackProgress = true;
    });

    Widget.on("play-pause", (shouldPause) => {
        if (shouldPause) {
            if (player.getPlayerState() != YT.PlayerState.BUFFERING) {
                player.pauseVideo();
            }
        } else {
            player.playVideo();
        }
    });

    Widget.on("play", ({ video, currentPlaybackId: _currentPlaybackId }) => {
        currentPlaybackId = _currentPlaybackId;
        console.debug(video);

        const dummy = document.createElement("html");
        dummy.innerHTML = video.html;
        const link = dummy.querySelector("iframe").src;

        player.cueVideoByUrl(link);
    });

    Widget.on("update", (settings) => {
        console.log(settings);

        videoQueue = settings.queue;
        shouldAutoplay = settings.autoplay;
        volume = settings.volume || 0.5;

        if (player) {
            checkMute();
            player.setVolume(volume * 100);
        }
    });

    Widget.on("init", () => {
        Widget.broadcast("update", Widget.widgetData.settings);
    });

    if (Widget.widgetData) {
        Widget.broadcast("update", Widget.widgetData.settings);
    }
</script>

<svelte:head>
    <script src="https://www.youtube.com/iframe_api"></script>
</svelte:head>

<!-- svelte-ignore a11y-missing-attribute -->
<!-- svelte-ignore missing-declaration -->
<div id="page">
    <div class="video-container">
        <Aspect16by9>
            <div id="video" />
            <!-- svelte-ignore a11y-missing-content -->
            {#if blockClick}
                <a id="video-blocker" on:click={playPause}>
                    <!-- {#if showGestureConsent && canShowGestureConsent}
                    <div id="playback-blocker">Click to unmute</div>
                {/if} -->
                </a>
            {/if}
        </Aspect16by9>
        <div style="margin: 10px;">
            <input
                type="range"
                class="range"
                title="Progress"
                disabled={!playing}
                bind:value={anim_playbackProgress}
                on:click={() => (animatePlaybackProgress = false)}
                on:change={(e) => {
                    Widget.emit("seek", e.target.value * player.getDuration());
                    animatePlaybackProgress = true;
                }}
                step="0.01"
                min="0"
                max="1"
                style="width: calc(100% - 140px);"
            />
            <input
                type="range"
                class="range"
                title="Volume"
                bind:value={volume}
                on:change={() => {
                    Widget.emit("volume-update", volume);
                }}
                step="0.01"
                min="0"
                max="1"
                style="margin-left: 35px; width: 100px;"
            />
            <br />
            <br />
            <label class="checkbox">
                <input
                    type="checkbox"
                    bind:checked={shouldAutoplay}
                    on:change={() => {
                        Widget.emit("autoplay", shouldAutoplay);
                    }}
                />
                Autoplay
            </label>
        </div>
    </div>
    <div class="queue-list-container">
        <div class="field has-addons">
            <div class="control">
                <input class="input" type="text" placeholder="https://youtube.com/watch?v=..." bind:value={addVideoInput} />
            </div>
            <div class="control">
                <a
                    class="button is-info"
                    on:click={() => {
                        Widget.emit("queue", addVideoInput);
                        addVideoInput = "";
                    }}
                >
                    Add
                </a>
            </div>
        </div>
        <ul id="queue-list">
            {#each videoQueue as video, idx}
                <li class="box">
                    <a class="clicker" title="Video Title" on:click={() => Widget.emit("play", idx)}>
                        <span> {escapeHtml(video.title)} </span>
                    </a>
                    <a class="remove" title="Remove" on:click={() => Widget.emit("remove", idx)}>
                        <svg
                            xmlns="http://www.w3.org/2000/svg"
                            width="18"
                            height="18"
                            viewBox="0 0 24 24"
                            fill="none"
                            stroke="currentColor"
                            stroke-width="2"
                            stroke-linecap="round"
                            stroke-linejoin="round"
                            class="feather feather-x"
                        >
                            <line x1="18" y1="6" x2="6" y2="18" />
                            <line x1="6" y1="6" x2="18" y2="18" />
                        </svg>
                    </a>
                </li>
            {/each}
        </ul>
    </div>
</div>

<style>
    #page {
        display: flex;
        flex-direction: row;
    }

    .video-container {
        flex-grow: 1;
        align-self: stretch;
        position: relative;
    }

    #video-blocker {
        position: absolute;
        top: 0;
        bottom: 0;
        left: 0;
        right: 0;
    }

    /* #playback-blocker {
        display: flex;
        align-items: center;
        justify-content: center;
        background-color: rgba(0, 0, 0, 0.85);
        width: 100%;
        height: 100%;
        color: white;
    } */

    .queue-list-container {
        align-self: stretch;
        height: calc(100vh - 8px);
        width: 275px;
    }

    #queue-list li {
        margin: 3px;
        padding: 8px;
    }

    #queue-list li .clicker {
        color: inherit;
        width: 200px;
        display: inline-block;
        line-height: 1em;
        text-overflow: ellipsis;
    }

    #queue-list li .remove {
        color: inherit;
        line-height: 18px;
        vertical-align: middle;
    }

    .range {
        -webkit-appearance: none;
        height: 4px !important;
        transform: translateY(-3px);
        background-color: #adadad;
        --thumb-background: #e8e8e8;
        --thumb-color: #121212;
    }

    :global(.app-is-dark) .range {
        background-color: #5c5c5c;
        --thumb-background: #121212;
        --thumb-color: #e8e8e8;
    }

    .range::-webkit-slider-runnable-track {
        width: 300px;
        height: 2px;
        border: none;
        border-radius: 3px;
    }

    .range::-webkit-slider-thumb {
        -webkit-appearance: none;
        border: none;
        height: 12px;
        width: 12px;
        border-radius: 500%;
        background: var(--background-color);
        border: 2px solid var(--title-bar-text-color);
        margin-top: -5px;
        cursor: pointer;
    }
</style>
