import {connect} from "react-redux";
import {ArtistSearchResults} from "./ArtistSearchResults";
import {addArtistToPlaylist} from "../EditPlaylist/Actions";
import {gotoEditPlaylistView} from "../../ViewSelection/Actions";

const mapStateToProps = (state, ownProps) => {
    const currentlyEditedPlaylist = state.playlists.get(state.currentlyEditedPlaylist);
    const playlistId = currentlyEditedPlaylist.id;
    return {
        playlistId: playlistId,
        artists: removeArtistsInPlaylist(state.artistSearchResults.artists, currentlyEditedPlaylist)
    }
};

const mapDispatchToProps = (dispatch, ownProps) => {
    return {
        selectArtist: event => {
            const artistId = event.target.id;
            const playlistId = event.target.dataset.playlistid;
            dispatch(addArtistToPlaylist(playlistId, artistId));
            dispatch(gotoEditPlaylistView());
        },
        back: event => {
            dispatch(gotoEditPlaylistView());
        }
    }
};

const removeArtistsInPlaylist = (artists, playlist) => {
    return artists.filter(artist => !playlist.rules.artists.includes(artist.id))
};

const ArtistSearchResultsContainer = connect(
    mapStateToProps,
    mapDispatchToProps
)(ArtistSearchResults);

export default ArtistSearchResultsContainer;
