import {combineReducers, Reducer} from "redux";
import {view} from "./ViewSelection/Reducers";
import {userId} from "./Components/UserId/Reducers";
import {playlists} from "./Views/ListPlaylists/Reducers";
import {currentlyEditedPlaylist} from "./Views/EditPlaylist/Reducers";
import {artistSearchResults} from "./Views/ArtistSearch/Reducers";
import {artists} from "./artistsReducer";
import {IState} from "./App";

const BaseReducer: Reducer<IState> = combineReducers({
    view,
    userId,
    playlists,
    currentlyEditedPlaylist,
    artistSearchResults,
    artists
});

export default BaseReducer
