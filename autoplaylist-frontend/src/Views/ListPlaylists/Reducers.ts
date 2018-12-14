import {PLAYLIST_CREATED, SET_PLAYLISTS} from "../../Networking/Actions";
import {ADD_ARTIST, ADD_EXCLUSION, REMOVE_ARTIST, REMOVE_EXCLUSION} from "../EditPlaylist/Actions";
import Playlist from "../../Domain/Playlist";
import {AnyAction} from "redux";
import Artist from "../../Domain/Artist";
import Exclusion from "../../Domain/Exclusion";

export const playlists = (state = new Map<String, Playlist>(), action: AnyAction) => {
    switch (action.type) {
        case SET_PLAYLISTS:
            return new Map(action.playlists.map((i:Playlist) => [i.id, i]));
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

const addToMap = (map:Map<String, Playlist>, item:Playlist) => {
    const newMap = new Map(map);
    newMap.set(item.id, item);
    return newMap;
};

// todo take an arist instead
const addArtist = (playlists:Map<String, Playlist>, targetPlaylistId:String, artistId:String) => {
    const target = playlists.get(targetPlaylistId);
    if (target === undefined) {
        console.warn("Attempted to add artist to non-existing playlist:", targetPlaylistId);
        return playlists;
    }

    const newArtists = [...target.rules.artists, artistId];
    const newRules = Object.assign(target.rules, {artists: newArtists});
    const newPlaylist = Object.assign(target, {rules: newRules});

    const newMap = new Map(playlists);
    newMap.set(target.id, newPlaylist);

    return newMap;
};

// todo take an arist instead
const removeArtist = (playlists:Map<String, Playlist>, targetPlaylistId:String, artistId:String) => {
    const target = playlists.get(targetPlaylistId);
    if (target === undefined) {
        console.warn("Attempted to remove artist to non-existing playlist:", targetPlaylistId);
        return playlists;
    }

    const newArtists = target.rules.artists.filter((artist:Artist) => artist.id !== artistId);
    const newRules = Object.assign(target.rules, {artists: newArtists});
    const newPlaylist = Object.assign(target, {rules: newRules});

    const newMap = new Map(playlists);
    newMap.set(target.id, newPlaylist);

    return newMap;
};

// todo take an exclusion instead
const addExclusion = (playlists:Map<String, Playlist>, targetPlaylistId:String, exclusionId:String, keyword:String) => {
    const target = playlists.get(targetPlaylistId);
    if (target === undefined) {
        console.warn("Attempted to add exclusion to non-existing playlist:", targetPlaylistId);
        return playlists;
    }

    const newExclusions = [...target.rules.exclusions, new Exclusion(exclusionId, keyword)];

    const newRules = Object.assign(target.rules, {exclusions: newExclusions});
    const newPlaylist = Object.assign(target, {rules: newRules});

    const newMap = new Map(playlists);
    newMap.set(target.id, newPlaylist);

    return newMap;
};

// finds the playlist that has the given exclusion and removes that exclusion from it
const removeExclusion = (playlists:Map<String, Playlist>, targetPlaylistId:String, exclusionId:String) => {
    const target = playlists.get(targetPlaylistId);
    if (target === undefined) {
        console.warn("Attempted to remove exclusion from non-existing playlist:", targetPlaylistId);
        return playlists;
    }

    const newExclusions = target.rules.exclusions.filter((excl:Exclusion) => excl.id !== exclusionId);
    const newRules = Object.assign(target.rules, {exclusions: newExclusions});
    const newPlaylist = Object.assign(target, {rules: newRules});

    const newMap = new Map(playlists);
    newMap.set(target.id, newPlaylist);

    return newMap;
};

