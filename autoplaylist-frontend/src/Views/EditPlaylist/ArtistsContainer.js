import {connect} from "react-redux";
import {Artists} from "./Artists";
import {removeArtist} from "./Actions";
import {gotoArtistSearchView} from "../../ViewSelection/Actions";

const mapStateToProps = (state, ownProps) => {
    const playlist = enrichPlaylistWithArtists(state.playlists.get(state.currentlyEditedPlaylist), state.artists);
    return {
        playlistId: playlist.id,
        // todo just do a map of artists and create object with name
        // instead of modifying whoe playlist
        artists: playlist.rules.artists
    }
};

const mapDispatchToProps = (dispatch, ownProps) => {
    return {
        addArtist: event => {
            const playlistId = event.target.id;
            console.log("Add artist to playlist", playlistId);
            dispatch(gotoArtistSearchView());
        },
        removeArtist: (event) => {
            const playlistId = event.currentTarget.dataset.playlistid;
            const artistId = event.target.id;
            dispatch(removeArtist(playlistId, artistId))
        }
    }
};

export const enrichPlaylistWithArtists = (playlist, artists) => {

    const newArtists = playlist.rules.artists.map(id => ({id: id, name: getArtistName(artists, id)}));
    const newRules = Object.assign({}, playlist.rules, {artists: newArtists});

    return Object.assign({}, playlist, {rules: newRules});
};

const getArtistName = (artists, id) => {
    // todo this can be empty
    const matches = artists.filter(artist => artist.id === id);
    if (matches.length < 1) {
        return "Unknown artist"
    }
    return matches[0].name
};

const ArtistsContainer = connect(
    mapStateToProps,
    mapDispatchToProps
)(Artists);

export default ArtistsContainer
