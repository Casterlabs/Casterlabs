import enUS from "./lang/en-US.mjs";
import enGB from "./lang/en-GB.mjs";
import enAU from "./lang/en-AU.mjs";

import fr from "./lang/fr.mjs";

const languages = {
    "en": enUS, // English (United States) is the default English locale
    "en-GB": enGB,
    "en-AU": enAU,

    "fr": fr,

};

let supportedLanguages = [];

for (const lang of Object.values(languages)) {
    supportedLanguages.push({
        "name": lang["meta.name"],
        "code": lang["meta.code"],
        "flag": lang["meta.flag"],
        "direction": lang["meta.direction"],
    });
}

export { supportedLanguages };

export default function translate(locale, key, opts = {}) {
    let result = key;

    if (languages[locale] && languages[locale][key]) {
        result = languages[locale][key];
    }

    const [languageCode] = locale.split("-");
    if (result == key && languages[languageCode]) {
        result = languages[languageCode][key];
    }

    if (result) {
        (result.match(/{\w+}/g) || [])
            .forEach((match) => {
                const item = match.substring(1, match.length - 1);

                if (opts[item]) {
                    result = result.replace(match, opts[item]);
                } else {
                    console.warn("[Lang]", "Could not find missing option for", item, "in", opts, "for", key);
                }
            });
    } else {
        result = key;
    }

    return result;
}