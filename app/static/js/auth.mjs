import Koi from "./koi.mjs";
import { KinokoV1 } from "./util/kinoko.mjs";
import KoiConn from "./util/koiconn.mjs";
import { generateUnsafePassword, getRandomItemInArray } from "./util/misc.mjs";
import { authStore } from "./caffeinated.mjs";
import Router from "./router.mjs";

const BRIME_CLIENT_ID = "605fadfe563212359ce4eb8b";

class AuthCallback {

    constructor(type = "unknown") {
        this.id = `auth_redirect:${generateUnsafePassword(128)}:${type}`;
        this.cancelled = false;
    }

    cancel() {
        this.cancelled = true;
        this.kinoko?.disconnect();
        this.kinoko = null;
    }

    awaitAuthMessage(timeout = -1) {
        return new Promise((resolve, reject) => {
            this.kinoko = new KinokoV1();

            let fufilled = false;
            const id = (timeout > 0) ? setTimeout(() => {
                if (!fufilled) {
                    fufilled = true;
                    this.cancel();
                    reject("TOKEN_TIMEOUT");
                }
            }, timeout) : -1;

            this.kinoko.connect(this.id, "parent");

            this.kinoko.on("close", () => {
                if (!fufilled && !this.cancelled) {
                    reject("CONNECTION_CLOSED");
                }

                clearTimeout(id);
            });

            this.kinoko.on("message", (data) => {
                const message = data.message;

                fufilled = true;

                this.kinoko.disconnect();
                this.kinoko = null;

                if (message === "NONE") {
                    reject("NO_TOKEN_PROVIDED");
                } else if (message.startsWith("token:")) {
                    const token = message.substring(6);

                    resolve(token);
                } else {
                    reject("TOKEN_MESSAGE_INVALID");
                }
            });
        });
    }

    getStateString() {
        return this.id;
    }

}

function prettifyString(str) {
    return str.substring(0, 1).toUpperCase() +
        str.substring(1).toLowerCase();
}

// Append the user's platform if required.
function transformUserData(userData) {
    if ((Auth.countSignedInPlatforms() > 1) && (userData.platform != "CASTERLABS_SYSTEM")) {
        const platformPretty = prettifyString(userData.platform);

        userData.displayname = `${userData.displayname} (${platformPretty})`;
    }
}

// ^
function transformEvent(event) {
    switch (event.event_type) {
        case "CATCHUP": {
            for (const cEvent of event.events) {
                transformEvent(cEvent);
            }
            break;
        }

        case "FOLLOW": {
            transformUserData(event.sender);
            break;
        }

        case "RAID": {
            transformUserData(event.host);
            break;
        }

        case "SUBSCRIPTION": {
            transformUserData(event.subscriber);
            break;
        }

        case "VIEWER_LIST": {
            for (const viewer of event.viewers) {
                transformUserData(viewer);
            }
            break;
        }

        case "VIEWER_JOIN":
        case "VIEWER_LEAVE": {
            transformUserData(event.viewer);
            break;
        }

        case "CHAT":
        case "DONATION":
        case "CHANNEL_POINTS": {
            transformUserData(event.sender);
            break;
        }
    }
}

Koi.on("x_koi_upvotechat", (e) => {
    const splitMessageId = e.messageId.split(":"); // Split by colon
    const platform = splitMessageId.shift(); // Remove the first element, that will be the platform.
    const messageId = splitMessageId.join(":"); // Join it back together

    const koiconn = koiconns[platform];

    if (koiconn) {
        koiconn.upvoteMessage(messageId);
    }
});

Koi.on("x_koi_deletechat", (e) => {
    const splitMessageId = e.messageId.split(":"); // Split by colon
    const platform = splitMessageId.shift(); // Remove the first element, that will be the platform.
    const messageId = splitMessageId.join(":"); // Join it back together

    const koiconn = koiconns[platform];

    if (koiconn) {
        koiconn.deleteMessage(messageId);
    }
});

Koi.on("x_koi_sendchat", (e) => {
    const { message, platform, chatter } = e;

    const koiconn = koiconns[platform];

    if (koiconn) {
        koiconn.sendMessage(message, chatter);
    }
});

Koi.on("x_koi_test", (e) => {
    const { eventType } = e;

    // This code basically randomizes which platform the test
    // will be requested from, just to keep everything fresh.
    const signedIn = Object.keys(Auth.getSignedInPlatforms());
    const randomPlatform = getRandomItemInArray(signedIn);

    const koiconn = koiconns[randomPlatform];

    koiconn.test(eventType);
});

const OAUTH_LINKS = {
    twitch: {
        type: "caffeinated_twitch",
        link: `https://id.twitch.tv/oauth2/authorize` +
            `?client_id=ekv4a842grsldmwrmsuhrw8an1duxt` +
            `&force_verify=true` +
            `&response_type=code` +
            `&redirect_uri=https%3A%2F%2Fcasterlabs.co/auth?type=caffeinated_twitch` +
            `&scope=user:read:email%20chat:read%20chat:edit%20bits:read%20channel:read:subscriptions%20channel_subscriptions%20channel:read:redemptions` +
            `&state=`
    },
    trovo: {
        type: "caffeinated_trovo",
        link: `https://open.trovo.live/page/login.html` +
            `?client_id=BGUnwUJUSJS2wf5xJpa2QrJRU4ZVcMgS` +
            `&response_type=token` +
            `&redirect_uri=https%3A%2F%2Fcasterlabs.co/auth/trovo` +
            `&scope=channel_details_self+chat_send_self+send_to_my_channel+user_details_self+chat_connect` +
            `&state=`
    },
    glimesh: {
        type: "caffeinated_glimesh",
        link: `https://glimesh.tv/oauth/authorize` +
            `?client_id=3c60c5b45bbae0eadfeeb35d1ee0c77e580b31fd42a5fbc8ae965ca7106c5139` +
            `&force_verify=true` +
            `&response_type=code` +
            `&redirect_uri=https%3A%2F%2Fcasterlabs.co%2Fauth%2Fglimesh` +
            `&scope=public+email+chat` +
            `&state=`
    }
};

let koiconns = {};
let oauthCallback = null;

const Auth = {

    /* ------------ */
    /* User Auth    */
    /* ------------ */

    addUserAuth(platform, token) {
        return new Promise((resolve, reject) => {
            platform = platform.toUpperCase();

            if (this.getSupportedPlatforms().includes(platform)) {
                // ¯\_(ツ)_/¯
                if (this.getSignedInPlatforms()[platform]) {
                    this.signOutUser(platform);
                }

                authStore.set(platform, token);

                const conn = new KoiConn();
                let loggedIn = false;

                function reconnect() {
                    loggedIn = false;

                    // Check to make sure we aren't
                    // intending for it to be closed.
                    if (koiconns[platform]) {
                        conn.connect(token);
                    }
                }

                conn.on("close", () => {
                    setTimeout(reconnect, 5000);
                });

                conn.on("error", (event) => {
                    const error = event.error;

                    switch (error) {
                        // case "PUPPET_AUTH_INVALID": {
                        //     break;
                        // }

                        case "USER_AUTH_INVALID": {
                            reject();

                            loggedIn = false;
                            this.signOutUser(platform);
                            Koi.broadcast("account_signout", {
                                platform: platform
                            });

                            if (!this.isSignedIn()) {
                                Koi.broadcast("no_account", {});
                            }
                            break;
                        }
                    }
                });

                conn.on("event", (event) => {
                    if (event.id) {
                        event.id = `${platform}:${event.id}`;
                    }

                    transformEvent(event);

                    if ((event.event_type == "USER_UPDATE") && !loggedIn) {
                        loggedIn = true;
                        Koi.broadcast("account_signin", event.streamer);
                        resolve();
                    }

                    Koi.broadcast(event.event_type, event);
                });

                koiconns[platform] = conn;

                reconnect();
            }
        });
    },

    signOutUser(platform) {
        const conn = koiconns[platform];

        if (conn) {
            authStore.set(platform, null);
            koiconns[platform] = null;
            conn.close();

            Koi.broadcast("account_signout", {
                platform: platform
            });

            if (!this.isSignedIn()) {
                Koi.broadcast("no_account", {});
            }
        }
    },

    getSignedInPlatforms() {
        const platforms = {};

        for (const [platform, koiconn] of Object.entries(koiconns)) {
            if (koiconn) {
                platforms[platform] = {
                    viewerList: koiconn.viewerList,
                    userData: koiconn.userData,
                    streamData: koiconn.streamData
                };
            }
        }

        return platforms;
    },

    countSignedInPlatforms() {
        let platforms = 0;

        for (const [platform, koiconn] of Object.entries(koiconns)) {
            if (koiconn) {
                platforms++;
            }
        }

        return platforms;
    },

    isSignedIn() {
        for (const koiconn of Object.values(koiconns)) {
            if (koiconn) {
                return true;
            }
        }

        return false;
    },

    /* ------------ */
    /* Misc         */
    /* ------------ */

    getSupportedPlatforms() {
        return [
            "CAFFEINE",
            "TWITCH",
            "TROVO",
            "GLIMESH",
            "BRIME"
        ];
    },

    /* ------------ */
    /* Signins      */
    /* ------------ */

    signinCaffeine(username, password, mfa) {
        return new Promise((resolve, reject) => {
            const loginPayload = {
                account: {
                    username: username,
                    password: password
                },
                mfa: {
                    otp: mfa
                }
            }

            fetch("https://api.caffeine.tv/v1/account/signin", {
                method: "POST",
                body: JSON.stringify(loginPayload),
                headers: new Headers({
                    "Content-Type": "application/json"
                })
            })
                .then((result) => result.json())
                .then((response) => {
                    if (response.hasOwnProperty("next")) {
                        reject("CAFFEINE_MFA");
                    } else if (response.errors) {
                        reject(response.errors);
                    } else {
                        const refreshToken = response.refresh_token;

                        fetch(`https://api.casterlabs.co/v2/natsukashii/create?platform=CAFFEINE&token=${refreshToken}`)
                            .then((nResult) => nResult.json())
                            .then((nResponse) => {
                                if (nResponse.data) {
                                    resolve(nResponse.data.token);
                                } else {
                                    reject(response.errors);
                                }
                            });
                    }
                })
                .catch(reject);
        });
    },

    signinBrime(email, password) {
        return new Promise((resolve, reject) => {
            const loginPayload = {
                email: email,
                password: password
            }

            fetch(`https://api-staging.brimelive.com/internal/auth/login?client_id=${BRIME_CLIENT_ID}`, {
                method: "POST",
                body: JSON.stringify(loginPayload),
                headers: new Headers({
                    "Content-Type": "application/json"
                })
            })
                .then((result) => result.json())
                .then((response) => {
                    if (response.data) {
                        const token = response.data.refreshToken;

                        fetch(`https://api.casterlabs.co/v2/natsukashii/create?platform=BRIME&token=${token}`)
                            .then((nResult) => nResult.json())
                            .then((nResponse) => {
                                if (nResponse.data) {
                                    resolve(nResponse.data.token);
                                } else {
                                    reject(nResponse.errors);
                                }
                            });
                    } else {
                        reject(response.errors);
                    }
                })
                .catch(reject);
        });
    },

    signinOAuth(platform) {
        oauthCallback?.cancel();

        return new Promise((resolve, reject) => {
            const { type, link } = OAUTH_LINKS[platform.toLowerCase()];

            oauthCallback = new AuthCallback(type);

            // 15min timeout
            oauthCallback.awaitAuthMessage((15 * 60) * 1000)
                .then((clToken) => {
                    resolve(clToken);
                })
                .catch((reason) => {
                    console.error("Could not await for token: ", reason);
                    reject(reason);
                });

            openLink(link + oauthCallback.getStateString());
        });
    },

    cancelOAuthSignin() {
        oauthCallback?.cancel();
    }

};

Object.freeze(Auth);

// Add listeners for forcing navigation to the signin screen when logged out.
{
    let signedOutEntirely = true;

    Koi.on("no_account", () => {
        console.debug("[Auth]", "User has not signed into an account yet, sending them to the signin screen.");
        signedOutEntirely = true;
        Router.navigateSignin();
    });

    Koi.on("account_signin", () => {
        if (signedOutEntirely) {
            console.debug("[Auth]", "User is now logged in, sending them to Home.");
            signedOutEntirely = false;
            Router.navigateHome();
        }
    });
}

// Check for accounts, otherwise broadcast that we're not signed in.
{
    let attemptedSignIn = false;

    for (const [platform, token] of Object.entries(authStore.store)) {
        if (typeof token == "string") {
            attemptedSignIn = true;
            Auth.addUserAuth(platform, token);
        } else {
            authStore.set(platform, null);
        }
    }

    if (!attemptedSignIn) {
        Koi.broadcast("no_account", {});
    }
}

export default Auth;
