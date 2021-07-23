import Koi from "./koi.mjs";
import { KinokoV1 } from "./util/kinoko.mjs";
import { generateUnsafePassword } from "./util/util.mjs";

class AuthCallback {

    constructor(type = "unknown") {
        this.id = `auth_redirect:${generateUnsafePassword(128)}:${type}`;
    }

    disconnect() {
        if (this.kinoko) {
            this.kinoko.disconnect();
        }

        this.kinoko = new KinokoV1();
    }

    awaitAuthMessage(timeout = -1) {
        return new Promise((resolve, reject) => {
            this.disconnect();

            let fufilled = false;
            const id = (timeout > 0) ? setTimeout(() => {
                if (!fufilled) {
                    fufilled = true;
                    this.disconnect();
                    reject("TOKEN_TIMEOUT");
                }
            }, timeout) : -1;

            this.kinoko.connect(this.id, "parent");

            this.kinoko.on("close", () => {
                if (!fufilled) {
                    reject("CONNECTION_CLOSED");
                }

                clearTimeout(id);
            });

            this.kinoko.on("message", (message) => {
                fufilled = true;

                this.disconnect();

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

Koi.on("x_koi_upvotechat", (e) => {
    const { messageId } = e;

});

Koi.on("x_koi_deletechat", (e) => {
    const { messageId } = e;

});

Koi.on("x_koi_sendchat", (e) => {
    const { message, platform, chatter } = e;

});

Koi.on("x_koi_test", (e) => {
    const { eventType } = e;

});

const OAUTH_LINKS = {
    twitch: {
        type: "caffeinated_twitch",
        link: `https://id.twitch.tv/oauth2/authorize?client_id=ekv4a842grsldmwrmsuhrw8an1duxt&force_verify=true&redirect_uri=https%3A%2F%2Fcasterlabs.co/auth?type=caffeinated_twitch&response_type=code&scope=user:read:email%20chat:read%20chat:edit%20bits:read%20channel:read:subscriptions%20channel_subscriptions%20channel:read:redemptions&state=`
    },
    trovo: {
        type: "caffeinated_trovo",
        link: `https://open.trovo.live/page/login.html?client_id=BGUnwUJUSJS2wf5xJpa2QrJRU4ZVcMgS&response_type=token&scope=channel_details_self+chat_send_self+send_to_my_channel+user_details_self+chat_connect&redirect_uri=https%3A%2F%2Fcasterlabs.co/auth/trovo&state=`
    },
    glimesh: {
        type: "caffeinated_glimesh",
        link: `https://glimesh.tv/oauth/authorize?force_verify=true&client_id=3c60c5b45bbae0eadfeeb35d1ee0c77e580b31fd42a5fbc8ae965ca7106c5139&redirect_uri=https%3A%2F%2Fcasterlabs.co%2Fauth%2Fglimesh&response_type=code&scope=public+email+chat&state=`
    }
};

const Auth = {

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

            fetch("https://api-staging.brimelive.com/internal/auth/login?client_id=" + BRIME_CLIENT_ID, {
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

                        fetch(`https://${CAFFEINATED.store.get("server_domain")}/v2/natsukashii/create?platform=BRIME&token=${token}`)
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
        return new Promise((resolve, reject) => {
            const { type, link } = OAUTH_LINKS[platform];

            const auth = new AuthCallback(type);

            // 15min timeout
            auth.awaitAuthMessage((15 * 1000) * 60).then((clToken) => {
                resolve(clToken);
            }).catch((reason) => {
                console.error("Could not await for token: ", reason);
                reject(reason);
            });

            openLink(link + auth.getStateString());
        });
    },

    signout(platform) {

    }

};

Object.freeze(Auth);

export default Auth;
