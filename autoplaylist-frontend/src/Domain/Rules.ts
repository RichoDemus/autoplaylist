import Exclusion from "./Exclusion";
import Artist from "./Artist";

export default class Rules {
    public readonly artists: Artist[];
    public readonly exclusions: Exclusion[];

    constructor(
        artists: Artist[] = [],
        exclusions: Exclusion[] = []
    ) {
        this.artists = artists;
        this.exclusions = exclusions;
    }

    public addArtist(artist:Artist): Rules {
        return new Rules([...this.artists, artist], this.exclusions);
    }

    public removeArtist(artistId: string) {
        return new Rules(this.artists.filter(artist => artist.id !== artistId));
    }

    public addExclusion(exclusion: Exclusion) {
        return new Rules(this.artists, [...this.exclusions, exclusion]);
    }

    public removeExclusion(exclusionId: string) {
        return new Rules(this.exclusions.filter(exclusion => exclusion.id !== exclusionId));
    }
}
