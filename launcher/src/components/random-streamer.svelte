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
        function showRandomStreamer() {
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
        }

        showRandomStreamer();
        setTimeout(showRandomStreamer, 500); // I HATE THIS.
    </script>
</div>