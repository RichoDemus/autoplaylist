import {connect} from "react-redux";
import {ArtistSearchResults} from "./ArtistSearchResults";
import {addArtistToPlaylist} from "../EditPlaylist/Actions";
import {gotoEditPlaylistView} from "../../ViewSelection/Actions";
import {IState} from "../../App";
import {Dispatch} from "redux";
import Playlist from "../../Domain/Playlist";
import Artist from "../../Domain/Artist";

const mapStateToProps = (state: IState, ownProps: any) => {
    // todo no elvis here
    const currentlyEditedPlaylist = state.playlists.get(state.currentlyEditedPlaylist);
    if (currentlyEditedPlaylist === undefined) {
        console.warn("No such playlist:", state.currentlyEditedPlaylist);
        throw new Error("No such playlist:" + state.currentlyEditedPlaylist);
    }
    const playlistId = currentlyEditedPlaylist.id;
    return {
        playlistId,
        artists: removeArtistsInPlaylist(state.artistSearchResults.artists, currentlyEditedPlaylist)
    }
};

const mapDispatchToProps = (dispatch: Dispatch, ownProps: any) => {
    return {
        selectArtist: (event: any) => {
            const artistId = event.target.id;
            const playlistId = event.target.dataset.playlistid;
            dispatch(addArtistToPlaylist(playlistId, artistId));
            dispatch(gotoEditPlaylistView());
        },
        back: (event: any) => {
            dispatch(gotoEditPlaylistView());
        }
    }
};

const removeArtistsInPlaylist = (artists: Artist[], playlist: Playlist) => {
    console.log("artists in playlist:", playlist.rules.artists);
    console.log("search results:", artists);
    return artists.filter(artist => {
        // todo this is ugly
        return playlist.rules.artists.map(a => a.value).indexOf(artist.id.value) === -1
    });
};

const ArtistSearchResultsContainer = connect(
    mapStateToProps,
    mapDispatchToProps
)(ArtistSearchResults);

export default ArtistSearchResultsContainer;
