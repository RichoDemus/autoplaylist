import React from "react";
import {ARTIST_SEARCH_VIEW, EDIT_PLAYLIST_VIEW, LIST_PLAYLISTS_VIEW, LOADING_VIEW, LOGIN_VIEW, WELCOME_VIEW} from "./Views";
import LoginView from "../Views/Login/LoginView";
import ErrorView from "../Views/Error/ErrorView";
import WelcomeView from "../Welcome/WelcomeView";
import LoadingView from "../Views/Loading/LoadingView";
import ListPlaylistsView from "../Views/ListPlaylists/ListPlaylistsView";
import EditPlaylistView from "../Views/EditPlaylist/EditPlaylistView";
import ArtistSearchView from "../Views/ArtistSearch/ArtistSearchView";

export const SelectView = ({view}) => (
    <div>
        {(() => {
            switch (view) {
                case LOGIN_VIEW:
                    return <LoginView/>;
                case WELCOME_VIEW:
                    return <WelcomeView/>;
                case LOADING_VIEW:
                    return <LoadingView/>;
                case LIST_PLAYLISTS_VIEW:
                    return <ListPlaylistsView/>;
                case EDIT_PLAYLIST_VIEW:
                    return <EditPlaylistView/>;
                case ARTIST_SEARCH_VIEW:
                    return <ArtistSearchView/>;
                default:
                    return <ErrorView/>;
            }
        })()}
    </div>
);
