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

const HttpNetworkingMiddleware = store => next => action => {
    switch (action.type) {
        case INIT:
            console.log("iniit!!!:", window.location.pathname);
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
                    const artists = playlists.flatMap(playlist => playlist.rules.artists);
                    const distinctArtists = new Set(artists);
                    console.log("all artists", distinctArtists);
                    distinctArtists.forEach(artistId => {
                        getArtist(artistId)
                            .then(artist => store.dispatch(artistInformationUpdated([artist])))
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
            const currentRules = store.getState().playlists.get(action.playlistId).rules;


            updateRules(action.playlistId, addArtistToRules(currentRules, action.artistId));
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
            const currentRules1 = store.getState().playlists.get(action.playlistId).rules;

            updateRules(playlistId1, removeArtistFromRules(currentRules1, artistId1));
            break;
        case ADD_EXCLUSION:
            // todo maybe turn ADD_ARTIST, REMOVE_ARTIST, etc into CHANGE_RULES
            const playlistId = action.playlistId;
            const exclusionId = action.exclusionId;
            const keyword = action.keyword;

            const currentRules2 = store.getState().playlists.get(playlistId).rules;

            updateRules(action.playlistId, addExclusionToRules(currentRules2, {id: exclusionId, keyword}));
            break;
        case REMOVE_EXCLUSION:
            const playlistId3 = action.playlistId;
            const exclusionId3 = action.exclusionId;
            const currentRules3 = store.getState().playlists.get(playlistId3).rules;

            updateRules(playlistId3, removeExclusionFromRules(currentRules3, exclusionId3));
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

const addArtistToRules = (rules, artist) => {
    const newArtists = [...rules.artists, artist];
    return Object.assign({}, rules, {artists: newArtists});
};

const removeArtistFromRules = (rules, artistId) => {
    const newArtists = rules.artists.filter(artist => artist !== artistId);
    return Object.assign(rules, {artists: newArtists});
};

const addExclusionToRules = (rules, keyword) => {
    const newExclusions = [...rules.exclusions, keyword];
    return Object.assign({}, rules, {exclusions: newExclusions});
};

const removeExclusionFromRules = (rules, exclusionId) => {
    const newExclusions = rules.exclusions.filter(excl => excl.id !== exclusionId);
    return Object.assign(rules, {exclusions: newExclusions});
};

export default HttpNetworkingMiddleware
