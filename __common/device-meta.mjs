function getDeviceMeta() {
    let isMobile = true;
    let platform;
    let browser;

    const userAgent = navigator.userAgent;

    // Mobile devices.
    if (userAgent.match(/Android/i)) {
        platform = "Android";
    } else if (userAgent.match(/iPhone/i)) {
        platform = "iPhone";
    } else if (userAgent.match(/iPad/i)) {
        platform = "iPad";
    } else if (userAgent.match(/iPod/i)) {
        platform = "iPod";
    } else if (userAgent.match(/IEMobile/i)) {
        // <3
        platform = "Windows Phone";
    } else {
        // We're out of the mobile range.
        isMobile = false;

        // Misc
        if (
            userAgent.match(/Vizio/i) ||
            userAgent.match(/SMART-TV/i) ||
            userAgent.match(/SmartTV/i) ||
            userAgent.match(/Roku/i) ||
            userAgent.match(/tvOS/i) ||
            userAgent.match(/WebTV/i)
        ) {
            browser = "Smart TV";
        } else {
            // Desktop devices.
            if (userAgent.match(/Windows/i)) {
                platform = "Windows";
            } else if (userAgent.match(/CrOS/i)) {
                platform = "Chrome OS";
            } else if (userAgent.match(/Macintosh/i) || userAgent.match(/Darwin/i)) {
                platform = "macOS";
            } else if (userAgent.match(/Linux/i)) {
                platform = "Linux";
            }

            if (platform) {
                if (userAgent.match(/Edge/i)) {
                    browser = "Microsoft Edge";
                } else if (userAgent.match(/Chrome/i)) {
                    browser = "Google Chrome";
                } else if (userAgent.match(/Chromium/i)) {
                    browser = "Chromium";
                } else if (userAgent.match(/Firefox/i)) {
                    browser = "Firefox";
                } else if (userAgent.match(/Safari/i)) {
                    browser = "Safari";
                } else if (userAgent.match(/Opera/i)) {
                    browser = "Opera";
                }
            } else {
                platform = "Unknown";
            }
        }
    }

    const deviceName = browser ? `${browser} (${platform})` : platform;

    let vars = {};

    window.location.href.replace(/[?&]+([^=&]+)=([^&]*)/gi, (m, key, value) => {
        vars[key] = value;
    });

    const result = {
        deviceName: deviceName,
        isMobile: isMobile,
        platform: platform,
        browser: browser,
        queryParameters: vars
    };

    Object.freeze(result);

    console.debug("[DeviceMeta]", JSON.stringify(result, null, 2));

    return result;
}

export default getDeviceMeta;
