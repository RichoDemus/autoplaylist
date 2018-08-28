import {PLAYLIST_CREATED, SET_PLAYLISTS} from "../../Networking/Actions";
import {ADD_ARTIST, ADD_EXCLUSION, REMOVE_ARTIST, REMOVE_EXCLUSION} from "../EditPlaylist/Actions";

export const playlists = (state = new Map(), action) => {
    switch (action.type) {
        case SET_PLAYLISTS:
            return new Map(action.playlists.map(i => [i.id, i]));
        case PLAYLIST_CREATED:
            return addToMap(state, action.playlist);
        case ADD_EXCLUSION:
            return addExclusion(state, action.playlistId, action.exclusionId, action.keyword);
        case REMOVE_EXCLUSION:
            return removeExclusion(state, action.playlistId, action.exclusionId);
        case ADD_ARTIST:
            return addArtist(state, action.playlistId, action.artistId);
        case REMOVE_ARTIST:
            return removeArtist(state, action.playlistId, action.artistId);
        default:
            return state;
    }
};

const addToMap = (map, item) => {
    const newMap = new Map(map);
    newMap.set(item.id, item);
    return newMap;
};

const addArtist = (playlists, targetPlaylistId, artistId) => {
    const target = playlists.get(targetPlaylistId);

    const newArtists = [...target.rules.artists, artistId];
    const newRules = Object.assign(target.rules, {artists: newArtists});
    const newPlaylist = Object.assign(target, {rules: newRules});

    const newMap = new Map(playlists);
    newMap.set(target.id, newPlaylist);

    return newMap;
};

const removeArtist = (playlists, targetPlaylistId, artistId) => {
    const target = playlists.get(targetPlaylistId);

    const newArtists = target.rules.artists.filter(artist => artist !== artistId);
    const newRules = Object.assign(target.rules, {artists: newArtists});
    const newPlaylist = Object.assign(target, {rules: newRules});

    const newMap = new Map(playlists);
    newMap.set(target.id, newPlaylist);

    return newMap;
};

const addExclusion = (playlists, targetPlaylistId, exclusionId, keyword) => {
    const target = playlists.get(targetPlaylistId);

    const newExclusions = [...target.rules.exclusions, {id: exclusionId, keyword}];

    const newRules = Object.assign(target.rules, {exclusions: newExclusions});
    const newPlaylist = Object.assign(target, {rules: newRules});

    const newMap = new Map(playlists);
    newMap.set(target.id, newPlaylist);

    return newMap;
};

// finds the playlist that has the given exclusion and removes that exclusion from it
const removeExclusion = (playlists, targetPlaylistId, exclusionId) => {
    const target = playlists.get(targetPlaylistId);

    const newExclusions = target.rules.exclusions.filter(excl => excl.id !== exclusionId);
    const newRules = Object.assign(target.rules, {exclusions: newExclusions});
    const newPlaylist = Object.assign(target, {rules: newRules});

    const newMap = new Map(playlists);
    newMap.set(target.id, newPlaylist);

    return newMap;
};

