import {playlists} from "./Reducers";
import {addExclusion, removeExclusion} from "../EditPlaylist/Actions";
import Playlist from "../../Domain/Playlist";
import Artist from "../../Domain/Artist";
import Rules from "../../Domain/Rules";
import Exclusion from "../../Domain/Exclusion";

it('Adds exclusion to playlist', () => {

    const initialState = new Map<string, Playlist>();
    initialState.set("1",
        new Playlist(
            "1",
            "Powerwolf",
            new Rules(
                [new Artist("5HFkc3t0HYETL4JeEbDB1v")],
                [new Exclusion("a", "live")]
            )
        )
    );

    initialState.set("2",
        new Playlist(
            "2",
            "deadmau5",
            new Rules(
                [new Artist("asd")],
                [new Exclusion("b", "excl1"), new Exclusion("c", "excl2")]
            )
        )
    );

    const result = playlists(initialState, addExclusion("2", "new-id", "new-exclusion"));

    const expected = new Map<string, Playlist>();
    expected.set("1", new Playlist(
        "1",
        "Powerwolf",
        new Rules(
            [new Artist("5HFkc3t0HYETL4JeEbDB1v")],
            [new Exclusion("a", "live")]
        )
    ));

    expected.set("2",
        new Playlist(
            "2",
            "deadmau5",
            new Rules(
                [new Artist("asd")],
                [
                    new Exclusion("b", "excl1"),
                    new Exclusion("c", "excl2"),
                    new Exclusion("new-id", "new-exclusion")
                ]
            )
        ));

    expect(result).toEqual(expected);

})
;

it('Removes exclusion from playlist', () => {

    const initialState = new Map<string, Playlist>();
    initialState.set("1",
        new Playlist(
            "1",
            "Powerwolf",
            new Rules(
                [new Artist("5HFkc3t0HYETL4JeEbDB1v")],
                [new Exclusion("a", "live")]
            )
        )
    );

    initialState.set("2",
        new Playlist(
            "2",
            "deadmau5",
            new Rules(
                [new Artist("asd")],
                [new Exclusion("b", "excl1"), new Exclusion("c", "excl2")]
            )
        )
    );

    const result = playlists(initialState, removeExclusion("2", "b"));

    const expected = new Map<string, Playlist>();
    expected.set("1",
        new Playlist(
            "1",
            "Powerwolf",
            new Rules(
                [new Artist("5HFkc3t0HYETL4JeEbDB1v")],
                [new Exclusion("a", "live")]
            )
        )
    );

    expected.set("2",
        new Playlist(
            "2",
            "deadmau5",
            new Rules(
                [new Artist("asd")],
                [new Exclusion("c", "excl2")]
            )
        )
    );

    expect(result).toEqual(expected);

});
