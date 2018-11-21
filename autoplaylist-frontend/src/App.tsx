import * as React from 'react';
import './App.css';
import {applyMiddleware, compose, createStore} from "redux";
import HttpNetworkingMiddleware from './Networking/HttpNetworkingMiddleware';
import SelectViewContainer from './ViewSelection/SelectViewContainer';
import {init} from './Networking/Actions';
import BaseReducer from './BaseReducer';
import {Provider} from "react-redux";
import {logger} from './LoggingMiddleware';

const enhancer = compose(
    applyMiddleware(logger, HttpNetworkingMiddleware)
);

const store = createStore(
    BaseReducer,
    enhancer
);

const App = () => (
    <Provider store={store}>
        <div className="App">
            <SelectViewContainer/>
        </div>
    </Provider>
);

// This is here to actually trigger the init stuff
// I have no idea where to put this :(
const initApp = () => {
    store.dispatch(init());
};
initApp();

export default App;
