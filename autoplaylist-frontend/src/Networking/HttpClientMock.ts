import {authenticate} from "./Oauth2";
import Playlist from "../Domain/Playlist";
import Rules from "../Domain/Rules";
import ArtistId from "../Domain/ArtistId";
import Exclusion from "../Domain/Exclusion";
import Artist from "../Domain/Artist";

const getBackendBaseUrl = () => {
    if (window.location.hostname === "localhost") {
        return "http://localhost:8080/v1"
    }
    return "https://api.autoplaylists.richodemus.com"
};

export const checkForValidSession = () => {
    return Promise.resolve(true);
};

export const createSession = (code: string) => {
    return fetch(getBackendBaseUrl() + '/sessions', {
        credentials: 'include',
        method: 'POST',
        headers: {
            'Accept': 'application/json',
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({code})
    })
        .then(response => {
            console.log("Create session response: ", response.json());
            return response.status === 200;
        });
};

export const login = () => {
    authenticate();
};

export const logout = () => {
    fetch(getBackendBaseUrl() + '/sessions', {
        credentials: 'include',
        method: 'DELETE',
    }).then(response => {
        console.log("logged out", response)
    });
    return Promise.resolve();
};

export const getUserId = () => {
    return Promise.resolve("richodemus");
};

const playlists = new Map<string, Playlist>();
playlists.set("1", new Playlist(
    "1",
    "Powerwolf",
    new Rules(
        [new ArtistId("5HFkc3t0HYETL4JeEbDB1v")],
        [new Exclusion("a", "live")]
    )
));

playlists.set("2",
    new Playlist(
        "2",
        "deadmau5",
        new Rules(
            [new ArtistId("asd")],
            [
                new Exclusion("b", "excl1"),
                new Exclusion("c", "excl2"),
                new Exclusion("new-value", "new-exclusion")
            ]
        )
    ));
let nextPlaylistId = 3;

export const getPlaylists: () => Promise<Playlist[]> = () => {
    return Promise.resolve(Array.from(playlists.values()));
};

export const createPlaylist: (name: string) => Promise<Playlist> = (name) => {
    return Promise.resolve(new Playlist("" + nextPlaylistId++, name));
};

export const updateRules = (playlistId: string, rules: any) => {
    console.log("Set rules for playlist", playlistId, "to", rules);
    return Promise.resolve(null);
};

export const findArtist = (query: string) => {
    if (query === "") {
        return Promise.resolve({
            query,
            artists: []
        })
    }
    return Promise.resolve({
        query,
        artists: [
            new Artist(new ArtistId("5HFkc3t0HYETL4JeEbDB1v"), "Powerwolf"),
            new Artist(new ArtistId("asd"), "deadmau5")
        ]
    })
};

export const getArtist = (id: ArtistId) => {
    if (id.value === "5HFkc3t0HYETL4JeEbDB1v") {
        return Promise.resolve(new Artist(id, "Powerwolf"));
    }
    return Promise.resolve(new Artist(new ArtistId("asd"), "deadmau5"));
};

export const enableSync = (playlistId: string) => {
    console.log("Enable sync for", playlistId)
};

export const disableSync = (playlistId: string) => {
    console.log("Disable sync for", playlistId)
};

export const syncNow = (playlistId: string) => {
    console.log("sync playlist", playlistId);
    return Promise.resolve(null);
};
