
function clickNav(name) {
    const navigation = document.querySelector(`#router-navigate-${name}`);

    navigation.click();
}

const Router = {

    navigateSignin() {
        clickNav("signin");
    },

    navigateHome() {
        if (getUrlVars().homeGoBack) {
            history.back();
        } else {
            clickNav("home");
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
