import {store} from "./App"
import {EDIT_PLAYLIST_VIEW, LIST_PLAYLISTS_VIEW} from "./ViewSelection/Views";
import {newPlaylist} from "./Views/ListPlaylists/Actions";
import Playlist from "./Domain/Playlist";

const mockPlaylists: any[] = [
    {
        id: "1",
        name: "Powerwolf",
        rules: {
            artists: ["5HFkc3t0HYETL4JeEbDB1v"],
            exclusions: [{id: "a", keyword: "live"}]
        },
        sync: false
    },
    {
        id: "2",
        name: "deadmau5",
        rules: {
            artists: ["asd"],
            exclusions: [{id: "b", keyword: "excl1"}, {id: "c", keyword: "excl2"}]
        },
        sync: false
    }
];

jest.mock("./Networking/HttpClient", () => ({
    checkForValidSession: () => {
        console.log("checkForValidSession called");
        return Promise.resolve(true)
    },
    createSession: () => {
        console.log("createSession called");
        return Promise.reject("not implemented");
    },
    login: () => {
        console.log("login called");
        return Promise.reject("not implemented");
    },
    logout: () => {
        console.log("logout called");
        return Promise.reject("not implemented");
    },
    getUserId: () => {
        console.log("getUserId called");
        return Promise.resolve("richodemus")
    },
    getPlaylists: () => {
        console.log("getPlaylists called");
        return Promise.resolve(mockPlaylists);
    },
    createPlaylist: (name: string) => {
        console.log("createPlaylist called");
        return Promise.resolve(new Playlist("3", name))
    },
    updateRules: () => {
        console.log("updateRules called");
        return Promise.reject("not implemented");
    },
    findArtist: () => {
        console.log("findArtist called");
        return Promise.reject("not implemented");
    },
    getArtist: () => {
        console.log("getArtist called");
        return Promise.reject("not implemented");
    },
    enableSync: () => {
        console.log("enableSync called");
        return Promise.reject("not implemented");
    },
    disableSync: () => {
        console.log("disableSync called");
        return Promise.reject("not implemented");
    },
    syncNow: () => {
        console.log("syncNow called");
        return Promise.reject("not implemented");
    }
}));

it('Test that does way to many things', () => {
    const id = "3";
    const changedViewToEditPlaylistView = new Promise(resolve => {
        const unsub = store.subscribe(() => {
            if (store.getState().view === EDIT_PLAYLIST_VIEW) {
                resolve();
                unsub();
            }
        });
    });

    console.log("Initial state:", store.getState());
    expect(store.getState().view).toEqual(LIST_PLAYLISTS_VIEW);

    store.dispatch(newPlaylist("new playlist"));
    const done = changedViewToEditPlaylistView.then(() => {
        const playlists: Map<string, Playlist> = store.getState().playlists;
        const playlist = playlists.get(id);
        expect(playlist).toBeDefined();
        // @ts-ignore
        expect(playlist.name).toEqual("new playlist");
        // @ts-ignore
        expect(playlist.rules.artists.length).toBe(0);
        // @ts-ignore
        expect(playlist.rules.exclusions.length).toBe(0);
    });

    return Promise.all([
        changedViewToEditPlaylistView,
        done
    ]);
});


