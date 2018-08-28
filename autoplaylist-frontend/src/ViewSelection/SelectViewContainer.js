import {connect} from "react-redux";
import {SelectView} from "./SelectView";

const mapStateToProps = (state, ownProps) => {
    return {
        view: state.view
    }
};

const mapDispatchToProps = (dispatch, ownProps) => {
    return {}
};

const SelectViewContainer = connect(
    mapStateToProps,
    mapDispatchToProps
)(SelectView);

export default SelectViewContainer
