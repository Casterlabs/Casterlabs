<script>
    const streamers = [
        {
            name: "Glitch3dPenguin",
            image: "/streamers/Glitch3dPenguin.png",
            link: "https://caff.tv/Glitch3dPenguin",
            platform: "Caffeine",
            platformIcon: "/platforms/caffeine.png"
        },
        {
            name: "DivideAConquer",
            image: "/streamers/DivideAConquer.png",
            link: "https://caff.tv/DivideAConquer",
            platform: "Caffeine",
            platformIcon: "/platforms/caffeine.png"
        },
        {
            name: "GeenoTV",
            image: "/streamers/GeenoTV.png",
            link: "https://caff.tv/GeenoTV",
            platform: "Caffeine",
            platformIcon: "/platforms/caffeine.png"
        },
        {
            name: "FallenWolf",
            image: "/streamers/FallenWolf.png",
            link: "https://caff.tv/FallenWolf",
            platform: "Caffeine",
            platformIcon: "/platforms/caffeine.png"
        }
    ];

    const chosenStreamer = streamers[Math.floor(Math.random() * streamers.length)];

</script>

<style>
    .streamer-link {
        position: absolute;
        left: 8px;
        right: 0;
        bottom: 3px;
        text-align: left;
    }

    .streamer-link img {
        vertical-align: middle;
    }

    .streamer-link a {
        color: white;
        text-decoration: none;
        -webkit-app-region: no-drag !important;
    }
</style>

<div>
    <img alt="{chosenStreamer.name}" src="{chosenStreamer.image}" />
    <div class="streamer-link">
        <a href="#" onclick="openLink('{chosenStreamer.link}');">
            <img alt="{chosenStreamer.platform}" src="{chosenStreamer.platformIcon}" />
            {chosenStreamer.name}
        </a>
    </div>
</div>