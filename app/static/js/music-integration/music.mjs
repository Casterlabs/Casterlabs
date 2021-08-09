import { MusicStates } from "./music-util.mjs";

import PretzelIntegration from "./impl/pretzel-rocks.mjs";
import SpotifyIntegration from "./impl/spotify.mjs";

// In order of priority
const integrations = [
    SpotifyIntegration,
    PretzelIntegration
];

const musicServices = {

    getCurrentPlayback() {
        let state = MusicStates.INACTIVE;
        let service = null;

        for (const integration of integrations) {
            if (integration.playbackState < state) {
                state = integration.playbackState;
                service = integration;
            }
        }

        if (service) {
            return {
                playbackState: service.playbackState,
                currentTrack: service.currentTrack,
                serviceName: service.serviceName,
                serviceId: service.serviceId
            };
        } else {
            return null;
        }
    }

};

// Register the services.
for (const integration of integrations) {
    musicServices[integration.serviceId] = integration;
}

Object.freeze(musicServices);

export default musicServices;