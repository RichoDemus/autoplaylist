import {connect} from "react-redux";
import {disableSync, enableSync, syncNow} from "./Actions";
import {Sync} from "./Sync";

const mapStateToProps = (state, ownProps) => {
    let playlist = state.playlists.get(state.currentlyEditedPlaylist);
    return {
        playlistId: playlist.id,
        enabled: playlist.sync,
        lastSynced: playlist.lastSynced
    }
};

const mapDispatchToProps = (dispatch, ownProps) => {
    return {
        disableSync: event => {
            const playlistId = event.target.id;
            dispatch(disableSync(playlistId));
        },
        enableSync: event => {
            const playlistId = event.target.id;
            dispatch(enableSync(playlistId));
        },
        syncNow: event => {
            const playlistId = event.target.id;
            dispatch(syncNow(playlistId));
        }
    }
};

const SyncContainer = connect(
    mapStateToProps,
    mapDispatchToProps
)(Sync);

export default SyncContainer
