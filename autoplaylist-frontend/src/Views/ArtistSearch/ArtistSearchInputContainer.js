import {connect} from "react-redux";
import {ArtistSearchInput} from "./ArtistSearchInput";
import {debounce} from "debounce";
import {artistSearchQueryUpdated} from "./Actions";

const mapStateToProps = (state, ownProps) => {
    return {}
};

const mapDispatchToProps = (dispatch, ownProps) => {
    const debounceAndDispatch = debounce(string => {
        dispatch(artistSearchQueryUpdated(string));
    }, 500);

    return {
        onChange: event => {
            const searchQuery = event.target.value;
            debounceAndDispatch(searchQuery);
        }
    }
};

const ArtistSearchInputContainer = connect(
    mapStateToProps,
    mapDispatchToProps
)(ArtistSearchInput);

export default ArtistSearchInputContainer;
