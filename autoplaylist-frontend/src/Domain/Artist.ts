import ArtistId from "./ArtistId";

export default class Artist {
    public readonly id: ArtistId;
    public readonly name: string;

    constructor(id: ArtistId, name: string) {
        this.id = id;
        this.name = name;
    }
}
