import {Action} from "redux";
import Playlist from "../Domain/Playlist";
import Artist from "../Domain/Artist";

export const INIT = "INIT";
export const SET_USER_ID = "SET_USER_ID";
export const SET_PLAYLISTS = "SET_PLAYLISTS";
export const PLAYLIST_CREATED = "PLAYLIST_CREATED";
export const ARTIST_SEARCH_RESULTS = "ARTIST_SEARCH_RESULTS";
export const ARTIST_INFORMATION_UPDATED = "ARTIST_INFORMATION_UPDATED";

export const init: () => Action<string> = () => {
    return {
        type: INIT
    }
};

export const setUserId: (userId: string) => SetUserIdAction = (userId) => {
    return {
        type: SET_USER_ID,
        userId
    }
};

// todo check how to actually do these things
export interface SetUserIdAction extends Action<string> {
    userId: string
}

export const setPlaylists = (playlists: Playlist[]) => {
    return {
        type: SET_PLAYLISTS,
        playlists
    }
};

export const playlistCreated = (playlist: Playlist) => {
    return {
        type: PLAYLIST_CREATED,
        playlist
    }
};

export const artistSearchResults = (results: any) => {
    return {
        type: ARTIST_SEARCH_RESULTS,
        results
    }
};

export const artistInformationUpdated = (artists: Artist[]) => {
    return {
        type: ARTIST_INFORMATION_UPDATED,
        artists
    }
};
