import {connect} from "react-redux";
import {EditedPlaylistName} from "./EditedPlaylistName";

const mapStateToProps = (state, ownProps) => {
    return {
        name: state.playlists.get(state.currentlyEditedPlaylist).name
    }
};

const mapDispatchToProps = (dispatch, ownProps) => {
    return {}
};

const EditedPlaylistNameContainer = connect(
    mapStateToProps,
    mapDispatchToProps
)(EditedPlaylistName);

export default EditedPlaylistNameContainer
