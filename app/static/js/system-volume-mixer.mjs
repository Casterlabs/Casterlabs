const SoundMixer = require("native-sound-mixer").default;
const { DeviceType } = require("native-sound-mixer");

function getSystemOutputDevices() {
    return SoundMixer.devices
        .filter((d) => d.type == DeviceType.RENDER);
}

function getSystemOutputDeviceByName(name) {
    getSystemOutputDevices()
        .filter((d) => d.name == name)[0];
}


function getAudioOutputSessionsByName(name) {
    const devices = getSystemOutputDevices();

    const sessions = [];

    for (const device of devices) {
        for (const session of device.sessions) {
            if (session.name == name) {
                sessions.push(session);
            }
        }
    }

    return sessions;
}

function getSystemInputDevices() {
    return SoundMixer.devices
        .filter((d) => d.type == DeviceType.CAPTURE);
}

function getSystemInputDeviceByName(name) {
    getSystemInputDevices()
        .filter((d) => d.name == name)[0];
}

function getAudioInputSessionsByName(name) {
    const devices = getSystemInputDevices();

    const sessions = [];

    for (const device of devices) {
        for (const session of device.sessions) {
            if (session.name == name) {
                sessions.push(session);
            }
        }
    }

    return sessions;
}

export {
    getSystemOutputDevices,
    getSystemOutputDeviceByName,
    getAudioOutputSessionsByName,

    getSystemInputDevices,
    getSystemInputDeviceByName,
    getAudioInputSessionsByName,

}