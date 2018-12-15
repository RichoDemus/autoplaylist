import {SET_USER_ID} from "../../Networking/Actions";
import {AnyAction} from "redux";

export const userId = (state = "", action: AnyAction) => {
    switch (action.type) {
        case SET_USER_ID:
            return action.userId;
        default:
            return state;
    }
};
