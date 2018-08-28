import {SET_VIEW} from "./Actions";
import {EDIT_PLAYLIST_VIEW, LIST_PLAYLISTS_VIEW, LOADING_VIEW, WELCOME_VIEW} from "./Views";
import {LOGGED_IN, LOGGED_OUT} from "../Views/Login/Actions";
import {PLAYLIST_CREATED} from "../Networking/Actions";
import {EDIT_PLAYLIST} from "../Views/ListPlaylists/Actions";

export const view = (state = LOADING_VIEW, action) => {
    switch (action.type) {
        case SET_VIEW:
            return action.view;
        case LOGGED_OUT:
            return WELCOME_VIEW;
        case LOGGED_IN:
            return LIST_PLAYLISTS_VIEW;
        case PLAYLIST_CREATED:
            return EDIT_PLAYLIST_VIEW;
        case EDIT_PLAYLIST:
            return EDIT_PLAYLIST_VIEW;
        default:
            return state;
    }
};
