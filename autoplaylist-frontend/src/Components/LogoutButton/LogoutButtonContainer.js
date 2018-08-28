import {connect} from "react-redux";
import {LogoutButton} from "./LogoutButton";
import {gotoLoadingView} from "../../ViewSelection/Actions";
import {logout} from "./Actions";

const mapStateToProps = (state, ownProps) => {
    return {}
};

const mapDispatchToProps = (dispatch, ownProps) => {
    return {
        logout: (event) => {
            dispatch(gotoLoadingView());
            dispatch(logout());
        }
    }
};

const CloseEditPlaylistButtonContainer = connect(
    mapStateToProps,
    mapDispatchToProps
)(LogoutButton);

export default CloseEditPlaylistButtonContainer;
