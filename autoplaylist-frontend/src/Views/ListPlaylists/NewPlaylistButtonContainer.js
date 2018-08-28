import {connect} from "react-redux";
import {NewPlaylistButton} from "./NewPlaylistButton";
import {newPlaylist} from "./Actions";
import {gotoLoadingView} from "../../ViewSelection/Actions";

const mapStateToProps = (state, ownProps) => {
    return {}
};

const mapDispatchToProps = (dispatch, ownProps) => {
    return {
        newPlaylistClick: (event) => {
            const playlistName = event.target.previousElementSibling.value;
            dispatch(gotoLoadingView());
            dispatch(newPlaylist(playlistName));
        },
        onKeyEnter: (event) => {
            if (event.key === "Enter") {
                const playlistName = event.target.value;
                dispatch(gotoLoadingView());
                dispatch(newPlaylist(playlistName));
            }
        }
    }
};

const NewPlaylistButtonContainer = connect(
    mapStateToProps,
    mapDispatchToProps
)(NewPlaylistButton);

export default NewPlaylistButtonContainer;
