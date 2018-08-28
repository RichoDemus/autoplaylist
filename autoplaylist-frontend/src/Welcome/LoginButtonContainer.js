import {connect} from "react-redux";
import {LoginButton} from "./LoginButton";
import {login} from "./Actions";
import {gotoLoadingView} from "../ViewSelection/Actions";

const mapStateToProps = (state, ownProps) => {
    return {}
};

const mapDispatchToProps = (dispatch, ownProps) => {
    return {
        login: (event) => {
            dispatch(gotoLoadingView());
            dispatch(login());
        }
    }
};

const CloseEditPlaylistButtonContainer = connect(
    mapStateToProps,
    mapDispatchToProps
)(LoginButton);

export default CloseEditPlaylistButtonContainer;
