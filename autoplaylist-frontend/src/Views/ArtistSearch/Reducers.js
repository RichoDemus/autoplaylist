import {ARTIST_SEARCH_RESULTS} from "../../Networking/Actions";

const defaultState = {query: "", artists: []};

export const artistSearchResults = (state = defaultState, action) => {
    switch (action.type) {
        case ARTIST_SEARCH_RESULTS:
            return action.results;
        // this was here to clear the search result once the page was left
        // not sure how I want it to behave
        // case ADD_ARTIST:
        //     return defaultState;
        default:
            return state;
    }
};
