import {connect} from "react-redux";
import {CloseEditPlaylistButton} from "./CloseEditPlaylistButton";
import {gotoListPlaylistsView} from "../../ViewSelection/Actions";
import {stopEditingPlaylist} from "./Actions";

const mapStateToProps = (state, ownProps) => {
    return {}
};

const mapDispatchToProps = (dispatch, ownProps) => {
    return {
        closeEditPlaylist: (event) => {
            dispatch(stopEditingPlaylist());
            dispatch(gotoListPlaylistsView());
        }
    }
};

const CloseEditPlaylistButtonContainer = connect(
    mapStateToProps,
    mapDispatchToProps
)(CloseEditPlaylistButton);

export default CloseEditPlaylistButtonContainer;
