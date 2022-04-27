import enUS from "./lang/en-US.mjs";
import enGB from "./lang/en-GB.mjs";
import enAU from "./lang/en-AU.mjs";

import fr from "./lang/fr.mjs";

import createConsole from "./console-helper.mjs";

const console = createConsole("translate");

const languages = {
    en: enUS, // English (United States) is the default English locale
    "en-GB": enGB,
    "en-AU": enAU,

    fr: fr
};

let externalLocalization = {};

export function defineExternalLocale(id, data) {
    externalLocalization[id] = data;
}

let supportedLanguages = [];

for (const lang of Object.values(languages)) {
    supportedLanguages.push({
        name: lang["meta.name"],
        code: lang["meta.code"],
        flag: lang["meta.flag"],
        direction: lang["meta.direction"]
    });
}

export { supportedLanguages };

export default function translate(locale, key, opts = {}, simpleResponse = true) {
    let result = key;
    let usedFallback = false;

    if (languages[locale] && languages[locale][key]) {
        result = languages[locale][key];
    }

    const [languageCode] = locale.split("-");
    if (result == key && languages[languageCode] && languages[languageCode][key]) {
        result = languages[languageCode][key];
    }

    if (result == key) {
        result = languages["en"][key] || key;

        if (result != key) {
            console.error(`Missing translation for key: ${key} in locale ${locale}, defaulting to English.`);
            usedFallback = true;
        }
    }

    if (!result || result == key) {
        for (const ext of Object.values(externalLocalization)) {
            if (result != key) {
                break;
            } else if (ext) {
                if (ext[locale] && ext[locale][key]) {
                    result = ext[locale][key];
                }

                const [languageCode] = locale.split("-");
                if (result == key && ext[languageCode] && ext[languageCode][key]) {
                    result = ext[languageCode][key];
                }

                if (result == key) {
                    result = ext["en"][key] || key;

                    if (result != key) {
                        console.error(`Missing translation for key: ${key} in locale ${locale} (external localization), defaulting to English.`);
                        usedFallback = true;
                    }
                }
            }
        }
    }

    if (result) {
        // Replace placeholders
        (result.match(/{\w+}/g) || []).forEach((match) => {
            const item = match.slice(1, -1);

            if (opts[item] != undefined) {
                result = result.replace(match, opts[item]);
            } else {
                console.warn("Could not find missing option for", item, "in", opts, "for", key);
            }
        });

        // Replace localized placeholders
        (result.match(/\[[\w\.]+\]/g) || []).forEach((match) => {
            const item = match.slice(1, -1);
            const tItem = translate(locale, item, {}, true);

            result = result.replace(match, tItem);
        });
    } else {
        result = key;
    }

    if (simpleResponse) {
        return result;
    } else {
        return {
            result: result,
            usedFallback: usedFallback
        };
    }
}
