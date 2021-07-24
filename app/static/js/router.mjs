
function clickNav(name) {
    const navigation = document.querySelector(`#router-navigate-${name}`);

    navigation.click();
}

const Router = {

    navigateSignin() {
        clickNav("signin");
    },

    navigateHome() {
        clickNav("home");
    }

};

Object.freeze(Router);

export default Router;
