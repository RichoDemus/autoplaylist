import {connect} from "react-redux";
import {addExclusion, removeExclusion} from "./Actions";
import {Exclusions} from "./Exclusions";
import {v4} from 'uuid'
import {IState} from "../../App";
import {Dispatch} from "redux";

const mapStateToProps = (state: IState, ownProps: any) => {
    const playlist = state.playlists.get(state.currentlyEditedPlaylist);
    if (playlist === undefined) {
        console.warn("No such playlist:", state.currentlyEditedPlaylist);
        throw new Error("No such playlist:" + state.currentlyEditedPlaylist);
    }
    return {
        playlistId: playlist.id,
        exclusions: playlist.rules.exclusions
    }
};

const mapDispatchToProps = (dispatch: Dispatch, ownProps: any) => {
    return {
        removeExclusion: (event: any) => {
            const playlistId = event.currentTarget.dataset.playlistid;
            const exclusionId = event.target.id;
            dispatch(removeExclusion(playlistId, exclusionId))
        },
        addExclusionClick: (event: any) => {
            const playlistId = event.target.id;
            const keyword = event.target.previousElementSibling.value;
            dispatch(addExclusion(playlistId, v4(), keyword));
        },
        addExclusionKeyPress: (event: any) => {
            if (event.key === "Enter") {
                const playlistId = event.target.id;
                const keyword = event.target.value;
                dispatch(addExclusion(playlistId, v4(), keyword));
            }
        }
    }
};

const ListPlaylistsContainer = connect(
    mapStateToProps,
    mapDispatchToProps
)(Exclusions);

export default ListPlaylistsContainer
