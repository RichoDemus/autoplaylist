import {playlists} from "./Reducers";
import {addExclusion, removeExclusion} from "../EditPlaylist/Actions";

it('Adds exclusion to playlist', () => {

    const initialState = new Map();
    initialState.set("1", {
        id: "1",
        name: "Powerwolf",
        rules: {
            artists: ["5HFkc3t0HYETL4JeEbDB1v"],
            exclusions: [{id: "a", name: "live"}]
        },
        sync: false
    });

    initialState.set("2", {
        id: "2",
        name: "deadmau5",
        rules: {
            artists: ["asd"],
            exclusions: [{id: "b", name: "excl1"}, {id: "c", name: "excl2"}]
        },
        sync: false
    });

    const result = playlists(initialState, addExclusion("2", "new-id", "new-exclusion"));

    const expected = new Map();
    expected.set("1", {
        id: "1",
        name: "Powerwolf",
        rules: {
            artists: ["5HFkc3t0HYETL4JeEbDB1v"],
            exclusions: [{id: "a", name: "live"}]
        },
        sync: false
    });

    expected.set("2", {
        id: "2",
        name: "deadmau5",
        rules: {
            artists: ["asd"],
            exclusions: [{id: "b", name: "excl1"}, {id: "c", name: "excl2"}, {id: "new-id", name: "new-exclusion"}]
        },
        sync: false
    });

    expect(result).toEqual(expected);

});

it('Removes exclusion from playlist', () => {

    const initialState = new Map();
    initialState.set("1", {
        id: "1",
        name: "Powerwolf",
        rules: {
            artists: ["5HFkc3t0HYETL4JeEbDB1v"],
            exclusions: [{id: "a", name: "live"}]
        },
        sync: false
    });

    initialState.set("2", {
        id: "2",
        name: "deadmau5",
        rules: {
            artists: ["asd"],
            exclusions: [{id: "b", name: "excl1"}, {id: "c", name: "excl2"}]
        },
        sync: false
    });

    const result = playlists(initialState, removeExclusion("2", "b"));

    const expected = new Map();
    expected.set("1", {
        id: "1",
        name: "Powerwolf",
        rules: {
            artists: ["5HFkc3t0HYETL4JeEbDB1v"],
            exclusions: [{id: "a", name: "live"}]
        },
        sync: false
    });

    expected.set("2", {
        id: "2",
        name: "deadmau5",
        rules: {
            artists: ["asd"],
            exclusions: [{id: "c", name: "excl2"}]
        },
        sync: false
    });

    expect(result).toEqual(expected);

});
