import {artistInformationUpdated, artistSearchResults, INIT, playlistCreated, setPlaylists, setUserId} from "./Actions";
import {
    checkForValidSession,
    createPlaylist,
    createSession,
    disableSync,
    enableSync,
    findArtist,
    getArtist,
    getPlaylists,
    getUserId,
    login,
    logout,
    syncNow,
    updateRules
} from "./HttpClient";
import {LOGGED_IN, loggedIn, loggedOut} from "../Views/Login/Actions";
import {CREATE_NEW_PLAYLIST} from "../Views/ListPlaylists/Actions";
import {LOGOUT} from "../Components/LogoutButton/Actions";
import {LOGIN} from "../Welcome/Actions";
import {
    ADD_ARTIST,
    ADD_EXCLUSION,
    DISABLE_SYNC,
    ENABLE_SYNC,
    REMOVE_ARTIST,
    REMOVE_EXCLUSION,
    SYNC_NOW
} from "../Views/EditPlaylist/Actions";
import {ARTIST_SEARCH_QUERY_UPDATED} from "../Views/ArtistSearch/Actions";
import getParameterByName from "../Util/QueryString";
import {error} from "../Views/Error/Actions";
import {AnyAction, Dispatch, Store} from "redux";
import Rules from "../Domain/Rules";
import Exclusion from "../Domain/Exclusion";
import Artist from "../Domain/Artist";

const HttpNetworkingMiddleware = (store: Store) => (next: Dispatch<AnyAction>) => (action: AnyAction) => {
    switch (action.type) {
        case INIT:
            if (window.location.pathname === "/callback") {
                const state = getParameterByName("state");
                const code = getParameterByName("code");
                const err = getParameterByName("error");
                console.log("State:", state);
                console.log("Error:", err);
                // todo handle error
                window.history.pushState('Main', 'Title', '/');
                createSession(code)
                    .then(sessionCreated => {
                        if (sessionCreated) {
                            store.dispatch(loggedIn());
                        } else {
                            store.dispatch(error("Failed to create session"));
                        }
                    })
            } else {
                checkForValidSession()
                    .then(hasSession => {
                        if (hasSession) {
                            store.dispatch(loggedIn())
                        } else {
                            store.dispatch(loggedOut())
                        }
                    });
            }
            break;
        case LOGIN:
            login();
            break;
        case LOGOUT:
            logout()
                .then(() => store.dispatch(loggedOut()));
            break;
        case LOGGED_IN:
            getUserId()
                .then(userId => store.dispatch(setUserId(userId)));
            getPlaylists()
                .then(playlists => {
                    store.dispatch(setPlaylists(playlists));
                    const artists = playlists.map(playlist => playlist.rules.artists)
                        .reduce((left, right) => [...left, ...right]);
                    const distinctArtists = new Set(artists);
                    console.log("all artists", distinctArtists);
                    distinctArtists.forEach(artist => {
                        getArtist(artist.id)
                            .then(artistWithName => store.dispatch(artistInformationUpdated([artistWithName])))
                    })
                });
            break;
        case CREATE_NEW_PLAYLIST:
            createPlaylist(action.name)
                .then(playlist => {
                    console.log("playlist:", playlist);
                    store.dispatch(playlistCreated(playlist))
                });
            break;
        case ADD_ARTIST:
            // todo maybe turn ADD_ARTIST, REMOVE_ARTIST, etc into CHANGE_RULES
            const currentRules: Rules = store.getState().playlists.get(action.playlistId).rules;


            updateRules(action.playlistId, currentRules.addArtist(new Artist(action.artistId)));
            break;
        case ARTIST_SEARCH_QUERY_UPDATED:
            findArtist(action.query)
                .then(result => {
                    store.dispatch(artistSearchResults(result));
                    store.dispatch(artistInformationUpdated(result.artists))
                });
            break;
        case REMOVE_ARTIST:
            const playlistId1 = action.playlistId;
            const artistId1 = action.artistId;
            const currentRules1: Rules = store.getState().playlists.get(action.playlistId).rules;

            updateRules(playlistId1, currentRules1.removeArtist(artistId1));
            break;
        case ADD_EXCLUSION:
            // todo maybe turn ADD_ARTIST, REMOVE_ARTIST, etc into CHANGE_RULES
            const playlistId = action.playlistId;
            const exclusionId = action.exclusionId;
            const keyword = action.name;

            const currentRules2: Rules = store.getState().playlists.get(playlistId).rules;

            updateRules(action.playlistId, currentRules2.addExclusion(new Exclusion(exclusionId, keyword)));
            break;
        case REMOVE_EXCLUSION:
            const playlistId3 = action.playlistId;
            const exclusionId3 = action.exclusionId;
            const currentRules3: Rules = store.getState().playlists.get(playlistId3).rules;

            updateRules(playlistId3, currentRules3.removeExclusion(exclusionId3));
            break;
        case ENABLE_SYNC:
            enableSync(action.playlistId);
            break;
        case DISABLE_SYNC:
            disableSync(action.playlistId);
            break;
        case SYNC_NOW:
            syncNow(action.playlistId);
            break;
        default:
    }
    return next(action);
};

export default HttpNetworkingMiddleware
