export const INIT = "INIT";
export const SET_USER_ID = "SET_USER_ID";
export const SET_PLAYLISTS = "SET_PLAYLISTS";
export const PLAYLIST_CREATED = "PLAYLIST_CREATED";
export const ARTIST_SEARCH_RESULTS = "ARTIST_SEARCH_RESULTS";
export const ARTIST_INFORMATION_UPDATED = "ARTIST_INFORMATION_UPDATED";

export const init = () => {
    return {
        type: INIT
    }
};

export const setUserId = userId => {
    return {
        type: SET_USER_ID,
        userId
    }
};

export const setPlaylists = playlists => {
    return {
        type: SET_PLAYLISTS,
        playlists
    }
};

export const playlistCreated = playlist => {
    return {
        type: PLAYLIST_CREATED,
        playlist
    }
};

export const artistSearchResults = results => {
    return {
        type: ARTIST_SEARCH_RESULTS,
        results
    }
};

export const artistInformationUpdated = artists => {
    return {
        type: ARTIST_INFORMATION_UPDATED,
        artists
    }
};
