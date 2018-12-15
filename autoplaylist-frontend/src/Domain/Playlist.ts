import Rules from "./Rules";
import ArtistId from "./ArtistId";
import Exclusion from "./Exclusion";

export default class Playlist {
    public readonly id: string;
    public readonly name: string;
    public readonly rules: Rules;
    public readonly sync: boolean;

    constructor(
        id: string,
        name: string,
        rules: Rules = new Rules(),
        sync: boolean = false
    ) {
        this.id = id;
        this.name = name;
        this.rules = rules;
        this.sync = sync;
    }

    public addArtist(artist: ArtistId): Playlist {
        return this.copy({rules: this.rules.addArtist(artist)});
    }

    public removeArtist(artistId: string): Playlist {
        return this.copy({rules: this.rules.removeArtist(artistId)});
    }

    public addExclusion(exclusion: Exclusion): Playlist {
        return this.copy({rules: this.rules.addExclusion(exclusion)});
    }

    public removeExclusion(exclusionId: string): Playlist {
        return this.copy({rules: this.rules.removeExclusion(exclusionId)});
    }

    public enableSync(): Playlist {
        return this.copy({sync: true});
    }

    private copy(copy: ICopy): Playlist {
        return new Playlist(
            copy.id || this.id,
            copy.name || this.name,
            copy.rules || this.rules,
            copy.sync || this.sync
        );
    }
}

// todo rename I...
interface ICopy {
    id?: string,
    name?: string,
    rules?: Rules,
    sync?: boolean
}
