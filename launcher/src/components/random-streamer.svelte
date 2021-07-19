<style>
    .streamer-link {
        position: absolute;
        left: 8px;
        right: 0;
        bottom: 3px;
        text-align: left;
        -webkit-app-region: no-drag !important;
    }

    .streamer-link img {
        vertical-align: middle;
    }

    .streamer-link a {
        color: white;
        text-decoration: none;
    }

    .streamer-link * {
        -webkit-app-region: no-drag !important;
    }
</style>

<div class="random-streamer-container" style="opacity: 0;">
    <img class="portrait" alt="Random Streamer" src="#" />
    <div class="streamer-link">
        <a href="#">
            <img alt="Platform" src="#" />
            <span>
            </span>
        </a>
    </div>
    <script>
        window.addEventListener("load", () => {
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

            console.debug(`${chosenStreamer.name}! I choose you!`, chosenStreamer);

            const container = document.querySelector(".random-streamer-container");
            const streamerLink = container.querySelector(".streamer-link a");
            const streamerName = streamerLink.querySelector("span");
            const streamerPlatform = streamerLink.querySelector("img");
            const streamerImage = container.querySelector(".portrait");

            streamerName.innerText = chosenStreamer.name;

            streamerPlatform.src = chosenStreamer.platformIcon;
            streamerPlatform.alt = chosenStreamer.platform;

            streamerImage.src = chosenStreamer.image;
            streamerImage.alt = chosenStreamer.name;

            streamerLink.addEventListener("click", () => {
                openLink(chosenStreamer.link);
            });

            container.style.opacity = 1;
        });
    </script>
</div>