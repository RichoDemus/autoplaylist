import {connect} from "react-redux";
import {ListPlaylists} from "./ListPlaylists";
import {editPlaylist} from "./Actions";

const mapStateToProps = (state, ownProps) => {
    const playlists = Array.from(state.playlists.values());
    return {
        playlists
    }
};

const mapDispatchToProps = (dispatch, ownProps) => {
    return {
        editPlaylist: (event) => {
            const playlistId = event.target.id;
            dispatch(editPlaylist(playlistId))
        }
    }
};

const ListPlaylistsContainer = connect(
    mapStateToProps,
    mapDispatchToProps
)(ListPlaylists);

export default ListPlaylistsContainer
