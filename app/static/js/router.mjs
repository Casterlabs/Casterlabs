
const Router = {

    navigateSignin() {
        goto("/signin");
    },

    navigateHome() {
        if (getUrlVars().homeGoBack) {
            history.back();
        } else {
            goto("/home");
        }
    },

    tryHomeGoBack() {
        if (getUrlVars().homeGoBack) {
            history.back();
        }
    }

};

Object.freeze(Router);

export default Router;
