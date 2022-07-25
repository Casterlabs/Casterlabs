<script>
    import { onDestroy, onMount } from "svelte";

    import createConsole from "$lib/console-helper.mjs";
    import getDeviceMeta from "$lib/device-meta.mjs";
    import { KinokoV1 } from "$lib/kinoko.mjs";

    import LoadingSpinner from "$lib/components/LoadingSpinner.svelte";
    import Aspect16by9 from "$lib/components/aspect-ratio/Aspect16by9.svelte";
    import ChatViewer from "$lib/components/chat/chat-viewer.svelte";

    const console = createConsole("Cam");

    let iceServers = [];

    let deviceMeta;
    let connectionId;

    let kinoko = new KinokoV1();
    let hasConnected = false;
    let connectionLost = false;
    let hasVideoError = false;
    let settingsDialogOpen = false;

    let display = "LOADING";
    let videoDevices = [];
    let audioDevices = [];

    let koiAuth = {};

    let viewerElement;

    let peer;
    let callerId;
    let currentCall;

    let videoPreviewElement;
    let videoStream;

    let resolution;
    let videoDeviceId;
    let audioDeviceId;

    async function reconnect() {
        try {
            // Setup the video stream.
            let videoConstraints = {
                // ...RESOLUTIONS[resolution]
            };
            let audioConstraints = {};

            // Use selected video device, or fallback to default.
            if (videoDeviceId) {
                if (videoDeviceId == "iOS Front-Facing Camera") {
                    videoConstraints.facingMode = "user";
                } else if (videoDeviceId == "iOS Rear-Facing Camera") {
                    videoConstraints.facingMode = "environment";
                } else {
                    videoConstraints.deviceId = videoDeviceId;
                }
            }

            if (audioDeviceId) {
                audioConstraints.deviceId = audioDeviceId;
            }

            videoStream = await navigator.mediaDevices.getUserMedia({
                video: videoConstraints,
                audio: audioConstraints
            });

            videoPreviewElement.srcObject = videoStream;
            videoPreviewElement.play();

            if (callerId) {
                if (currentCall) currentCall.close();

                currentCall = peer.call(callerId, videoStream);

                currentCall.on("close", () => {
                    currentCall = null;
                });
            }
        } catch (e) {
            console.error(e);
            hasVideoError = true;
        }
    }

    onMount(async () => {
        deviceMeta = getDeviceMeta();
        connectionId = deviceMeta.queryParameters.id;

        iceServers = (await (await fetch("https://cdn.casterlabs.co/api.json")).json()).ice_servers;

        if (!connectionId) {
            display = "ERROR_CANNOT_CONNECT";
            return;
        } else if (!isWebRTCSupported()) {
            display = "ERROR_WEBRTC_NOT_SUPPORTED";
            return;
        }

        loadSettings();

        // Request permission, required on iOS.
        await navigator.mediaDevices.getUserMedia({
            video: true,
            audio: true
        });

        videoDevices = await getDevicesByKind("video");
        audioDevices = await getDevicesByKind("audio");

        // console.debug(videoDevices);
        // console.debug(audioDevices);

        let hasReceivedHistory = false;

        kinoko.on("message", ({ message }) => {
            if (message.type == "KOI_AUTH") {
                koiAuth = {};

                for (const platform of message.platforms) {
                    koiAuth[platform] = {};
                }

                viewerElement.onAuthUpdate({ koiAuth });
            } else if (message.type == "CALLER_ID") {
                callerId = message.id;
                reconnect();
            } else if (message.type == "KOI_EVENT") {
                viewerElement.processEvent(message.event);
            } else if (message.type == "KOI_HISTORY" && !hasReceivedHistory) {
                hasReceivedHistory = true;
                message.history.forEach(viewerElement.processEvent);
            }
        });

        kinoko.on("orphaned", () => {
            connectionLost = true;
        });

        kinoko.on("adopted", () => {
            display = "STREAM";
            connectionLost = false;
            hasConnected = true;

            // Begin handshake.

            peer = new Peer({
                config: {
                    iceServers: iceServers,
                    sdpSemantics: "unified-plan"
                }
            });

            peer.on("open", (id) => {
                kinoko.send({
                    type: "INIT"
                });

                reconnect();
            });
        });

        kinoko.connect(`casterlabs_camshare:${connectionId}`);

        // Wait 2.5s for connection to be established.
        // If it doesn't, we'll assume the connection was lost.
        setTimeout(() => {
            if (!hasConnected) {
                display = "ERROR_CANNOT_CONNECT";
            }
        }, 2.5 * 1000);
    });

    onDestroy(() => {
        kinoko.disconnect();
    });

    function onChatSend({ detail: data }) {
        sendChat(data.message, data.platform, data.replyTarget, true);
    }

    function sendChat(message, platform, replyTarget = null, isUserGesture = true) {
        kinoko.send({ type: "CHAT", message: message, platform: platform, replyTarget: replyTarget, isUserGesture: isUserGesture });
    }

    function onModAction({ detail: modAction }) {
        const { type, event } = modAction;
        const platform = event.streamer.platform;

        console.log("[StreamChat]", `onModAction(${type}, ${platform})`);

        switch (type) {
            case "ban": {
                switch (platform) {
                    case "TWITCH": {
                        sendChat(`/ban ${event.sender.username}`, platform);
                        return;
                    }

                    case "TROVO": {
                        sendChat(`/ban ${event.sender.username}`, platform);
                        return;
                    }

                    default: {
                        return;
                    }
                }
            }

            case "timeout": {
                // We timeout for 10 minutes
                switch (platform) {
                    case "TWITCH": {
                        sendChat(`/timeout ${event.sender.username} 600`, platform);
                        return;
                    }

                    case "TROVO": {
                        sendChat(`/ban ${event.sender.username} 600`, platform);
                        return;
                    }

                    default: {
                        return;
                    }
                }
            }

            case "delete": {
                if (["TWITCH", "BRIME", "TROVO"].includes(platform)) {
                    kinoko.send({ type: "DELETE", messageId: event.id, platform });
                }
                return;
            }

            case "upvote": {
                if (platform == "CAFFEINE") {
                    kinoko.send({ type: "UPVOTE", messageId: event.id, platform });
                }
                return;
            }

            case "raid": {
                switch (platform) {
                    case "TWITCH": {
                        sendChat(`/raid ${event.sender.username}`, platform);
                        return;
                    }

                    case "CAFFEINE": {
                        sendChat(`/afterparty ${event.sender.username}`, platform);
                        return;
                    }

                    case "TROVO": {
                        sendChat(`/host ${event.sender.username}`, platform);
                        return;
                    }

                    default: {
                        return;
                    }
                }
            }
        }
    }

    function onSavePreferences({ detail: data }) {
        localStorage.setItem("cl_studio:chat_viewer:preferences", JSON.stringify(data));
    }

    const RESOLUTIONS = {
        "720p30": {
            width: {
                max: 1280
            },
            height: {
                max: 720
            },
            frameRate: {
                max: 30
            }
        },
        "720p60": {
            width: {
                max: 1280
            },
            height: {
                max: 720
            },
            frameRate: {
                max: 60
            }
        },
        "1080p30": {
            width: {
                max: 1920
            },
            height: {
                max: 1080
            },
            frameRate: {
                max: 30
            }
        },
        "1080p60": {
            width: {
                max: 1920
            },
            height: {
                max: 1080
            },
            frameRate: {
                max: 60
            }
        }
        // "4k30": {
        //     width: {
        //         max: 3840
        //     },
        //     height: {
        //         max: 2160
        //     },
        //     frameRate: {
        //         max: 30
        //     }
        // }
    };

    function loadSettings() {
        const stored = localStorage.getItem("cl_studio:cam:settings");
        const defaults = {
            resolution: "720p30",
            videoDeviceId: "Default",
            audioDeviceId: "Default"
        };

        let settings = defaults;

        if (stored) {
            settings = {
                ...defaults,
                ...JSON.parse(stored)
            };
        }

        resolution = settings.resolution;
        videoDeviceId = settings.videoDeviceId;
        audioDeviceId = settings.audioDeviceId;
    }

    async function getDevicesByKind(kind = "input") {
        let inputs = [];

        (await navigator.mediaDevices.enumerateDevices()).forEach((device) => {
            if (device.kind.includes(kind)) {
                inputs.push(device);
            }
        });

        return inputs;
    }

    function isWebRTCSupported() {
        return navigator.mediaDevices ? true : false; // Prevent the passing of the instance.
    }
</script>

{#if settingsDialogOpen}
    <div id="background-cover" on:click={() => (settingsDialogOpen = false)} />
{/if}

<!-- svelte-ignore a11y-label-has-associated-control -->
{#if settingsDialogOpen}
    <div id="settings" class="box">
        <a id="settings-close" on:click={() => (settingsDialogOpen = false)}>
            <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="feather feather-x">
                <line x1="18" y1="6" x2="6" y2="18" />
                <line x1="6" y1="6" x2="18" y2="18" />
            </svg>
        </a>
        <!-- <div class="field">
            <label class="label">Resolution</label>
            <div class="control">
                <div class="select">
                    <select bind:value={resolution} on:change={reconnect}>
                        {#each Object.keys(RESOLUTIONS) as resolution}
                            <option>{resolution}</option>
                        {/each}
                    </select>
                </div>
            </div>
        </div> -->
        <div class="field">
            <label class="label">Video Device</label>
            <div class="control">
                <div class="select">
                    <select bind:value={videoDeviceId} on:change={reconnect}>
                        <option>Default</option>
                        {#each videoDevices as device}
                            <option value={device.deviceId}>{device.label}</option>
                        {/each}
                    </select>
                </div>
            </div>
        </div>
        <div class="field">
            <label class="label">Audio Device</label>
            <div class="control">
                <div class="select">
                    <select bind:value={audioDeviceId} on:change={reconnect}>
                        <option>Default</option>
                        {#each audioDevices as device}
                            <option value={device.deviceId}>{device.label}</option>
                        {/each}
                    </select>
                </div>
            </div>
        </div>
    </div>
{/if}

<section class="cam-share has-text-centered">
    {#if display != "STREAM"}
        <br />
        <div class="casterlabs-wordmark">
            <img class="light-show" src="/img/wordmark/casterlabs/black.svg" alt="Casterlabs Studio Logo" />
        </div>

        <br />
        <br />
    {/if}

    {#if display == "LOADING"}
        <div class="loading-spinner">
            <LoadingSpinner />
        </div>
    {:else if display == "STREAM"}
        <div id="stream-contents">
            <div class="box video-preview-container">
                <Aspect16by9>
                    <video bind:this={videoPreviewElement} id="video-preview" playsinline muted />

                    <button id="settings-opener" class="button" on:click={() => (settingsDialogOpen = !settingsDialogOpen)}>
                        <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="feather feather-settings" style="display: block;">
                            <circle cx="12" cy="12" r="3" />
                            <path d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 0 1 0 2.83 2 2 0 0 1-2.83 0l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-2 2 2 2 0 0 1-2-2v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 0 1-2.83 0 2 2 0 0 1 0-2.83l.06-.06a1.65 1.65 0 0 0 .33-1.82 1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1-2-2 2 2 0 0 1 2-2h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 0 1 0-2.83 2 2 0 0 1 2.83 0l.06.06a1.65 1.65 0 0 0 1.82.33H9a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 2-2 2 2 0 0 1 2 2v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 0 1 2.83 0 2 2 0 0 1 0 2.83l-.06.06a1.65 1.65 0 0 0-.33 1.82V9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 2 2 2 2 0 0 1-2 2h-.09a1.65 1.65 0 0 0-1.51 1z" />
                        </svg>
                    </button>
                </Aspect16by9>
            </div>

            <div id="chat-container">
                <ChatViewer
                    bind:this={viewerElement}
                    on:chatsend={onChatSend}
                    on:modaction={onModAction}
                    on:savepreferences={onSavePreferences}
                    on:mount={() => {
                        const prefs = localStorage.getItem("cl_studio:chat_viewer:preferences");
                        if (prefs) {
                            viewerElement.loadConfig(JSON.parse(prefs));
                        }

                        viewerElement.onAuthUpdate({ koiAuth });
                    }}
                />
            </div>
        </div>
    {:else if display == "ERROR_CANNOT_CONNECT"}
        <h1 class="title is-4">Could not establish a connection with Caffeinated.</h1>
        <h2 class="subtitle is-6">Make sure you correctly scanned the QR code in Caffeinated, and make sure the widget is visible in OBS.</h2>
        <br />
        <div class="loading-spinner">
            <LoadingSpinner />
        </div>
        <br />
        <br />
        <p>(Waiting for Caffeinated)</p>
    {:else if display == "ERROR_WEBRTC_NOT_SUPPORTED"}
        <h1 class="title is-4">Your browser does not support WebRTC.</h1>
        <h2 class="subtitle is-6">
            {#if deviceMeta.platform == "iPhone"}
                You may need to switch to Safari for this to work.
            {/if}
        </h2>
    {/if}
</section>

<svelte:head>
    <script src="https://unpkg.com/peerjs@1.3.1/dist/peerjs.js"></script>
    <link rel="stylesheet" href="https://fonts.googleapis.com/css2?family=Manrope:wght@400;600&display=swap" />
    <link rel="stylesheet" href="/css/bulma.min.css" />
</svelte:head>

<style>
    #stream-contents {
        width: 100%;
        max-width: 640px;
        margin: auto;
        height: 100%;
        display: flex;
        flex-direction: column;
        flex-wrap: nowrap;
        justify-content: flex-start;
        align-content: stretch;
        align-items: stretch;
    }

    #chat-container {
        position: relative;
        order: 0;
        flex: 1 1 auto;
        align-self: auto;
        text-align: left;
    }

    #background-cover {
        position: fixed;
        top: 0;
        left: 0;
        width: 100%;
        height: 100%;
        background-color: rgba(0, 0, 0, 0.5);
        z-index: 199;
    }

    #settings {
        position: fixed;
        top: 50%;
        left: 50%;
        transform: translate(-50%, -50%);
        width: 90%;
        max-width: 400px;
        padding-bottom: 2em;
        z-index: 210;
    }

    #settings-opener {
        position: absolute;
        right: 10px;
        bottom: 10px;
        width: 40px;
        height: 40px;
        padding: 0;
    }

    #settings-close {
        position: absolute;
        top: 0;
        right: 0;
        padding: 10px;
        color: black;
        z-index: 250;
    }

    .video-preview-container {
        margin: 15px;
        margin-bottom: 0;
        padding: 0;
        overflow: hidden;
        order: 0;
        flex: 0 0 auto;
        align-self: auto;
    }

    #video-preview {
        width: 100%;
        height: 100%;
        object-fit: cover;
        display: block;
        background-color: black;
    }

    .cam-share {
        position: absolute;
        top: 0;
        bottom: 0;
        left: 0;
        width: 100%;
    }

    .casterlabs-wordmark img {
        width: 240px;
    }

    .loading-spinner {
        margin: auto;
        margin-top: 25px;
        width: 50px;
        height: 50px;
    }
</style>
