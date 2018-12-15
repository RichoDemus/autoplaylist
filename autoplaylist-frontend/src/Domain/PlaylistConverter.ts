import Playlist from "./Playlist";
import Rules from "./Rules";
import ArtistId from "./ArtistId";
import Exclusion from "./Exclusion";

export const toPlaylist: (js: any) => Playlist = (js: any) => {
    return new Playlist(js.id, js.name, toRules(js.rules), js.sync);
};

const toRules: (js: any) => Rules = (js: any) => {
    return new Rules(
        js.artists.map((artist: any) => new ArtistId(artist)),
        js.exclusions.map((exclusion: any) => new Exclusion(exclusion.id, exclusion.keyword))
    );
};
