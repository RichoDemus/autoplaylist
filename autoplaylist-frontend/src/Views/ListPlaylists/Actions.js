export const CREATE_NEW_PLAYLIST = "CREATE_NEW_PLAYLIST";
export const EDIT_PLAYLIST = "EDIT_PLAYLIST";

export const newPlaylist = name => {
    return {
        type: CREATE_NEW_PLAYLIST,
        name
    }
};

export const editPlaylist = id => {
    return {
        type: EDIT_PLAYLIST,
        id
    }
};
