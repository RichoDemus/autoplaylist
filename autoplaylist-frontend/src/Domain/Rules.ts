import Exclusion from "./Exclusion";
import Artist from "./Artist";

export default class Rules {
    readonly artists: Artist[];
    readonly exclusions: Exclusion[];

    constructor(
        artists: Artist[] = [],
        exclusions: Exclusion[] = []
    ) {
        this.artists = artists;
        this.exclusions = exclusions;
    }

    addArtist(artist:Artist): Rules {
        return new Rules([...this.artists, artist], this.exclusions);
    }

    removeArtist(artistId: string) {
        return new Rules(this.artists.filter(artist => artist.id !== artistId));
    }

    addExclusion(exclusion: Exclusion) {
        return new Rules(this.artists, [...this.exclusions, exclusion]);
    }

    removeExclusion(exclusionId: string) {
        return new Rules(this.exclusions.filter(exclusion => exclusion.id !== exclusionId));
    }
}
