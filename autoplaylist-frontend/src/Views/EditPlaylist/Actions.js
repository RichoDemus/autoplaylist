export const STOP_EDITING_PLAYLIST = "STOP_EDITING_PLAYLIST";
export const ADD_ARTIST = "ADD_ARTIST";
export const REMOVE_ARTIST = "REMOVE_ARTIST";
export const ADD_EXCLUSION = "ADD_EXCLUSION";
export const REMOVE_EXCLUSION = "REMOVE_EXCLUSION";
export const ENABLE_SYNC = "ENABLE_SYNC";
export const DISABLE_SYNC = "DISABLE_SYNC";
export const SYNC_NOW = "SYNC_NOW";

export const stopEditingPlaylist = () => {
    return {
        type: STOP_EDITING_PLAYLIST
    }
};

export const addArtistToPlaylist = (playlistId, artistId) => {
    return {
        type: ADD_ARTIST,
        playlistId,
        artistId
    }
};

export const removeArtist = (playlistId, artistId) => {
    return {
        type: REMOVE_ARTIST,
        playlistId,
        artistId
    }
};

export const addExclusion = (playlistId, exclusionId, keyword) => {
    return {
        type: ADD_EXCLUSION,
        playlistId,
        exclusionId,
        keyword
    }
};

export const removeExclusion = (playlistId, exclusionId) => {
    return {
        type: REMOVE_EXCLUSION,
        playlistId,
        exclusionId
    }
};

export const enableSync = playlistId => {
    return {
        type: ENABLE_SYNC,
        playlistId
    }
};

export const disableSync = playlistId => {
    return {
        type: DISABLE_SYNC,
        playlistId
    }
};

export const syncNow = playlistId => {
    return {
        type: SYNC_NOW,
        playlistId
    }
};
