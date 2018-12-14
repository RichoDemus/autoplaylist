import Rules from "./Rules";
import Artist from "./Artist";
import Exclusion from "./Exclusion";

export default class Playlist {
    readonly id: String;
    readonly name: String;
    readonly rules: Rules;
    readonly sync: boolean;

    constructor(
        id: String,
        name: String,
        rules: Rules = new Rules(),
        sync: boolean = false
    ) {
        this.id = id;
        this.name = name;
        this.rules = rules;
        this.sync = sync;
    }

    addArtist(artist: Artist): Playlist {
        return this.copy({rules: this.rules.addArtist(artist)});
    }

    removeArtist(artistId: string): Playlist {
        return this.copy({rules: this.rules.removeArtist(artistId)});
    }

    addExclusion(exclusion: Exclusion): Playlist {
        return this.copy({rules: this.rules.addExclusion(exclusion)});
    }

    removeExclusion(exclusionId: string): Playlist {
        return this.copy({rules: this.rules.removeExclusion(exclusionId)});
    }

    enableSync(): Playlist {
        return this.copy({sync: true});
    }

    private copy(copy: Copy): Playlist {
        return new Playlist(
            copy.id || this.id,
            copy.name || this.name,
            copy.rules || this.rules,
            copy.sync || this.sync
        );
    }
}

interface Copy {
    id?: String,
    name?: String,
    rules?: Rules,
    sync?: boolean
}
