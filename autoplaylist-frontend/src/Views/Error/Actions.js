export const ERROR = "ERROR";

export const error = msg => {
    return {
        type: ERROR,
        msg
    }
};
