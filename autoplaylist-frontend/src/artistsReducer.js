import {ARTIST_INFORMATION_UPDATED} from "./Networking/Actions";

// this is just a list of "known artists"
// used so that we can map artistId to an artist object with more data, like name
export const artists = (state = [], action) => {
    switch (action.type) {
        case ARTIST_INFORMATION_UPDATED:
            // todo probably or merge or something, maybe this should be a map...
            return [...state, ...action.artists];
        default:
            return state;
    }
};
