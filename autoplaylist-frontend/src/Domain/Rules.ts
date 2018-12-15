import Exclusion from "./Exclusion";
import ArtistId from "./ArtistId";

export default class Rules {
    public readonly artists: ArtistId[];
    public readonly exclusions: Exclusion[];

    constructor(
        artists: ArtistId[] = [],
        exclusions: Exclusion[] = []
    ) {
        this.artists = artists;
        this.exclusions = exclusions;
    }

    public addArtist(artist: ArtistId): Rules {
        return new Rules([...this.artists, artist], this.exclusions);
    }

    public removeArtist(artistId: string) {
        return new Rules(this.artists.filter(artist => artist.value !== artistId));
    }

    public addExclusion(exclusion: Exclusion) {
        return new Rules(this.artists, [...this.exclusions, exclusion]);
    }

    public removeExclusion(exclusionId: string) {
        return new Rules(this.artists, this.exclusions.filter(exclusion => exclusion.id !== exclusionId));
    }
}
