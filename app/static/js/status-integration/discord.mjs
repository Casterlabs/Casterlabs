const RPC = require("discord-rpc");

import { appStore } from "../caffeinated.mjs";

const DISCORD_CLIENT_ID = "829844402548768768";
const DISCORD_CLIENT_SECRET = "3d829b1a030e39dcf354edda0e0777e6a15a0993d0dfb4b2193f6cc2b19481e0";

const discordRPCClient = new RPC.Client({
    transport: "ipc"
});

function isEnabled() {
    return appStore.get("status_integration.discord.enabled");
}

function getStatusIcon() {
    const icon = appStore.get("status_integration.discord.icon");

    if (icon == "streaming-platform") {
        return "brime-logo"; // TEMP
    } else {
        return icon;
    }
}

function setStatusIcon(icon) {
    if (![
        "casterlabs-logo",
        "casterlabs-pride",
        "casterlabs-moon",
        "streaming-platform"
    ].includes(icon)) {
        icon = "casterlabs-logo";
    }

    appStore.set("status_integration.discord.icon", icon);

    updateDiscord();
}

async function updateDiscord() {
    if (isEnabled()) {
        const icon = getStatusIcon();
        const title = "test, pls ignore <3";

        discordRPCClient.setActivity({
            state: title,
            // timestamps: {
            //     // Funky normalization below...
            //     start: Date.now()
            //     // Told ya.
            // },
            largeImageKey: icon,
            largeImageText: title,
            buttons: [
                // TEMP
                {
                    label: "Casterlabs",
                    url: "https://casterlabs.co"
                }
            ]
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

export {
    getStatusIcon,
    setStatusIcon,

    updateDiscord
};
