
let previousPage = null;

function clickNav(name) {
    previousPage = name;

    const navigation = document.querySelector(`#router-navigate-${name}`);

    navigation.click();
}

const Router = {

    navigateLogin() {
        clickNav("login");
    },

    navigateHome() {
        clickNav("home");
    },

    navigateBackOrHome() {
        if (
            (previousPage == "login") ||
            (history.length == 1)
        ) {
            this.navigateHome();
        } else {
            history.back();
        }
    }

};

Object.freeze(Router);

export default Router;
