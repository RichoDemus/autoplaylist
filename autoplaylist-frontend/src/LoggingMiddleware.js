// Node doesn't support these so tests fail...
if (!console["group"]) console["group"] = () => {
};
if (!console["groupCollapsed"]) console["groupCollapsed"] = () => {
};
if (!console["groupEnd"]) console["groupEnd"] = () => {
};

export const logger = renameToStoreAgain => next => action => {
    console.group("Action:", action.type);
    console.info('dispatching', action);
    const result = next(action);
    console.log('next state', renameToStoreAgain.getState());
    console.groupEnd();
    return result
};
