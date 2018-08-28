import {PLAYLIST_CREATED} from "../../Networking/Actions";
import {STOP_EDITING_PLAYLIST} from "./Actions";
import {EDIT_PLAYLIST} from "../ListPlaylists/Actions";

export const currentlyEditedPlaylist = (state = null, action) => {
    switch (action.type) {
        case PLAYLIST_CREATED:
            return action.playlist.id;
        case EDIT_PLAYLIST:
            return action.id;
        case STOP_EDITING_PLAYLIST:
            return null;
        default:
            return state;
    }
};
