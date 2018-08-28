import {SET_USER_ID} from "../../Networking/Actions";

export const userId = (state = "", action) => {
    switch (action.type) {
        case SET_USER_ID:
            return action.userId;
        default:
            return state;
    }
};
