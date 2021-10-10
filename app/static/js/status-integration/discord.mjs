const RPC = require("discord-rpc");

import { appStore } from "../caffeinated.mjs";
import Auth from "../auth.mjs";
import Koi from "../koi.mjs";
import { prettifyString } from "../util/misc.mjs";

const DISCORD_CLIENT_ID = "829844402548768768";
const DISCORD_CLIENT_SECRET = "3d829b1a030e39dcf354edda0e0777e6a15a0993d0dfb4b2193f6cc2b19481e0";

const discordRPCClient = new RPC.Client({
    transport: "ipc"
});

function isEnabled() {
    return appStore.get("status_integration.discord.enabled");
}

function getStatusIcon() {
    return appStore.get("status_integration.discord.icon");
}

function setStatusIcon(icon) {
    if (![
        "casterlabs-logo",
        "casterlabs-pride",
        "casterlabs-moon",
        "caffeine-logo",
        "twitch-logo",
        "trovo-logo",
        "glimesh-logo",
        "brime-logo"
    ].includes(icon)) {
        icon = "casterlabs-logo";
    }

    appStore.set("status_integration.discord.icon", icon);

    updateDiscord();
}

async function updateDiscord() {
    const isLive = [];

    for (const [plat, data] of Object.entries(Auth.getSignedInPlatforms())) {
        if (data.streamData?.is_live) {
            isLive.push(data.streamData);
        }
    }

    if (isEnabled() && (isLive.length > 0)) {
        console.debug("[Discord RPC]", "Setting state.")
        const icon = getStatusIcon();
        const title = "I'm live.";

        const buttons = [];

        for (const liveData of isLive) {
            buttons.push({
                label: `Watch on ${prettifyString(liveData.streamer.platform)}`,
                url: liveData.streamer.link
            });
        }

        discordRPCClient.setActivity({
            state: title,
            // timestamps: {
            //     // Funky normalization below...
            //     start: Date.now()
            //     // Told ya.
            // },
            largeImageKey: icon,
            largeImageText: title,
            buttons: buttons
        });

        // discordRPCClient.request("SET_ACTIVITY", {
        //     pid: process.pid,
        //     activity: {
        //         state: title,
        //         timestamps: {
        //             // Funky normalization below...
        //             start: start.getTime() + 2
        //             // Told ya.
        //         },
        //         assets: {
        //             large_image: image,
        //             large_text: liveMessage
        //         },
        //         buttons: [
        //             { label: "Watch Now", url: link }
        //         ]
        //     }
        // });
    } else {
        discordRPCClient.clearActivity();
    }
}

function login() {
    discordRPCClient
        .login({
            clientId: DISCORD_CLIENT_ID,
            clientSecret: DISCORD_CLIENT_SECRET
        })
        .then(() => {
            console.debug("[Discord RPC]", "Connected to Discord.");
            updateDiscord();
        })
        .catch((e) => {
            // console.error("[Discord RPC]", "Cannot connect to Discord:", e);
            setTimeout(login, 25000);
        });
}

login();

Koi.on("stream_status", () => {
    updateDiscord();
});

export {
    getStatusIcon,
    setStatusIcon,

    updateDiscord
};
