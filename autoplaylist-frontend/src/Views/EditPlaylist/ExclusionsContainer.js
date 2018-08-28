import {connect} from "react-redux";
import {addExclusion, removeExclusion} from "./Actions";
import {Exclusions} from "./Exclusions";
import uuidv4 from 'uuid/v4'

const mapStateToProps = (state, ownProps) => {
    let playlist = state.playlists.get(state.currentlyEditedPlaylist);
    return {
        playlistId: playlist.id,
        exclusions: playlist.rules.exclusions
    }
};

const mapDispatchToProps = (dispatch, ownProps) => {
    return {
        removeExclusion: (event) => {
            const playlistId = event.currentTarget.dataset.playlistid;
            const exclusionId = event.target.id;
            dispatch(removeExclusion(playlistId, exclusionId))
        },
        addExclusionClick: event => {
            const playlistId = event.target.id;
            const keyword = event.target.previousElementSibling.value;
            dispatch(addExclusion(playlistId, uuidv4(), keyword));
        },
        addExclusionKeyPress: event => {
            if (event.key === "Enter") {
                const playlistId = event.target.id;
                const keyword = event.target.value;
                dispatch(addExclusion(playlistId, uuidv4(), keyword));
            }
        }
    }
};

const ListPlaylistsContainer = connect(
    mapStateToProps,
    mapDispatchToProps
)(Exclusions);

export default ListPlaylistsContainer
