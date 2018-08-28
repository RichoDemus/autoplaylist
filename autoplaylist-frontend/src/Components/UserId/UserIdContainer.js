import {connect} from "react-redux";
import {userId} from "./UserId";

const mapStateToProps = (state, ownProps) => {
    return {
        userId: state.userId
    }
};

const mapDispatchToProps = (dispatch, ownProps) => {
    return {}
};

const UserIdContainer = connect(
    mapStateToProps,
    mapDispatchToProps
)(userId);

export default UserIdContainer
