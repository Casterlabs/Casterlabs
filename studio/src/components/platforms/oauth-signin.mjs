import { AuthCallback } from "$lib/kinoko.mjs";

const CLIENT_ID = "LmHG2ux992BxqQ7w9RJrfhkW";

const OAUTH_LINKS = {
    twitch: {
        type: "caffeinated_twitch",
        link: "https://casterlabs.co/auth/redirect"
    },
    trovo: {
        type: "caffeinated_trovo",
        link: "https://casterlabs.co/auth/redirect"
    },
    glimesh: {
        type: "caffeinated_glimesh",
        link: "https://casterlabs.co/auth/redirect"
    },
    brime: {
        type: "caffeinated_brime",
        link: "https://casterlabs.co/auth/redirect"
    },
    youtube: {
        type: "caffeinated_youtube",
        link: "https://casterlabs.co/auth/redirect"
    }
};

class OAuthSignin {
    constructor() {
        this.oauthCallback = null;
    }

    start(platform) {
        return new Promise((resolve, reject) => {
            const { type, link } = OAUTH_LINKS[platform.toLowerCase()];

            this.oauthCallback = new AuthCallback(type, CLIENT_ID);

            const tab = window.open(`${link}?state=${this.oauthCallback.getStateString()}`, "_blank");

            this.oauthCallback
                // 15min timeout
                .awaitAuthMessage(15 * 60 * 1000)
                .then((clToken) => {
                    resolve(clToken);
                })
                .catch((reason) => {
                    console.error("Could not await for token: ", reason);
                    reject(reason);
                })
                .finally(() => {
                    tab.close();
                });
        });
    }

    cancel() {
        this.oauthCallback && this.oauthCallback.cancel();
    }
}

export default OAuthSignin;