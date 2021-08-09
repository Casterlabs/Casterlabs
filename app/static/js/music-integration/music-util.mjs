import * as TypeValidation from "../type-validation.mjs";
import Koi from "../koi.mjs";

const MusicStates = {
    PLAYING: 0,
    PAUSED: 1,
    INACTIVE: 3,

    isValid(val) {
        for (const state of Object.values(this)) {
            if ((typeof state == "number") &&
                (state === val)) {
                return true;
            }
        }
        return false;
    }
};
Object.freeze(MusicStates);

class MusicIntegration {
    playbackState = MusicStates.INACTIVE;
    currentTrack = null;
    serviceName = null;
    serviceId = null;

    constructor(name, id) {
        this.serviceName = name;
        this.serviceId = id;
    }

    set playbackState(val) {
        if (MusicStates.isValid(val)) {
            this.playbackState = val;

            if (this.playbackState == MusicStates.INACTIVE) {
                this.currentTrack = null;
            }

            Koi.broadcast("music", this);
        } else {
            throw "Invalid music state: " + val;
        }
    }

    set currentTrack(val) {
        if (val instanceof MusicTrack) {
            this.currentTrack = val;
        }
    }

    set serviceName(val) { } // Ignored

    set serviceId(val) { } // Ignored

}

class MusicTrack {
    title = "Unknown";
    artists = ["Various Artists"];
    album = null;
    albumArtUrl = "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mN86e39HwAGJAKAjQAVpAAAAABJRU5ErkJggg==";
    link = "https://casterlabs.co#music-integration";

    constructor(title, artists, album, albumArtUrl, link) {
        // Validate title
        TypeValidation.validate(title, {
            allowedTypes: ["string"],
            nullable: false,
            name: "title"
        });
        // Validate artists
        TypeValidation.validate(artists, {
            allowedTypes: [Array],
            allowedArrayTypes: ["string"],
            nullable: false,
            name: "artists"
        });
        // Validate album
        TypeValidation.validate(album, {
            allowedTypes: ["string"],
            nullable: true,
            name: "album"
        });
        // Validate albumArtUrl
        TypeValidation.validate(albumArtUrl, {
            allowedTypes: ["string"],
            nullable: false,
            name: "albumArtUrl"
        });
        // Validate link
        TypeValidation.validate(link, {
            allowedTypes: ["string"],
            nullable: false,
            name: "link"
        });

        this.title = title;
        this.artists = artists;
        this.album = album;
        this.albumArtUrl = albumArtUrl;
        this.link = link;

        Object.freeze(this);
        Object.freeze(this.artists);
    }

}

export {
    MusicIntegration,
    MusicTrack,
    MusicStates
};