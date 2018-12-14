import {toPlaylist} from "./PlaylistConverter";
import Exclusion from "./Exclusion";
import Artist from "./Artist";
import Playlist from "./Playlist";

it("Should convert a js object to a playlist", () => {
    const result = toPlaylist({
        id: "1",
        name: "Powerwolf",
        rules: {
            artists: ["5HFkc3t0HYETL4JeEbDB1v"],
            exclusions: [{id: "a", keyword: "live"}]
        },
        sync: false
    });

    expect(result.id).toEqual("1");
    expect(result.name).toEqual("Powerwolf");
    expect(result.rules.artists).toContainEqual(new Artist("5HFkc3t0HYETL4JeEbDB1v"));
    expect(result.rules.exclusions).toContainEqual(new Exclusion("a", "live"));
});

it("should create a playlist", () => {
    const playlist = new Playlist("id", "name");

    expect(playlist.id).toEqual("id");
    expect(playlist.name).toEqual("name");
    expect(playlist.rules.artists.length).toEqual(0);
    expect(playlist.rules.exclusions.length).toEqual(0);
    expect(playlist.sync).toEqual(false);
});

it("should add artist", () => {
    const playlist = new Playlist("id", "name")
        .addArtist(new Artist("artistId"));

    expect(playlist.rules.artists).toContainEqual(new Artist("artistId"))
});

it("should remove artist", () => {
    const playlist = new Playlist("id", "name")
        .addArtist(new Artist("artistId"))
        .removeArtist("artistId");

    expect(playlist.rules.artists.length).toEqual(0);
});

it("should add exclusion", () => {
    const playlist = new Playlist("id", "name")
        .addExclusion(new Exclusion("exclId", "keyword"));

    expect(playlist.rules.exclusions).toContainEqual(new Exclusion("exclId", "keyword"))
});

it("should remove exclusion", () => {
    const playlist = new Playlist("id", "name")
        .addExclusion(new Exclusion("exclId", "keyword"))
        .removeExclusion("exclId");

    expect(playlist.rules.exclusions.length).toEqual(0);
});

it("should enable sync", () => {
    const playlist = new Playlist("id", "name")
        .enableSync();
    expect(playlist.sync).toEqual(true);
});

it("should not modify ordinal instance", function () {
    const original = new Playlist("id", "name");
    const modified = original.enableSync();

    expect(original.sync).toEqual(false);
    expect(modified.sync).toEqual(true);
});