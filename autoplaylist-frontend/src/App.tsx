import * as React from 'react';
import './App.css';
import {AnyAction, applyMiddleware, compose, createStore, Store} from "redux";
import HttpNetworkingMiddleware from './Networking/HttpNetworkingMiddleware';
import SelectViewContainer from './ViewSelection/SelectViewContainer';
import {init} from './Networking/Actions';
import BaseReducer from './BaseReducer';
import {Provider} from "react-redux";
import {logger} from './LoggingMiddleware';
import Playlist from "./Domain/Playlist";

const enhancer = compose(
    applyMiddleware(logger, HttpNetworkingMiddleware)
);

export const store: Store<IState> = createStore<IState, AnyAction, any, any>(
    BaseReducer,
    enhancer
);

// todo figure out how to not need the I...
export interface IState {
    view: string,
    userId: string,
    playlists: Map<string, Playlist>,
    currentlyEditedPlaylist: string,
    artistSearchResults: any,
    artists: any
}

export const App = () => (
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
