import React from 'react';
import './App.css';
import {applyMiddleware, compose, createStore} from "redux";
import {Provider} from "react-redux";
import HttpNetworkingMiddleware from "./Networking/HttpNetworkingMiddleware";
import BaseReducer from "./BaseReducer";
import {init} from "./Networking/Actions";
import SelectViewContainer from "./ViewSelection/SelectViewContainer";

// Node doesn't support these so tests fail...
if (!console["group"]) console["group"] = () => {
};
if (!console["groupCollapsed"]) console["groupCollapsed"] = () => {
};
if (!console["groupEnd"]) console["groupEnd"] = () => {
};

const logger = store => next => action => {
    console.group("Action:", action.type);
    console.info('dispatching', action);
    let result = next(action);
    console.log('next state', store.getState());
    console.groupEnd(action.type);
    return result
};

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

/*
class App extends Component {
    constructor(props) {
        super(props);

        this.logout = this.logout.bind(this);
        this.createPlaylist = this.createPlaylist.bind(this);

        this.state = {
            loggedIn: false,
            userId: null,
            playLists: [],
            error: null,
            inputArtist: "",
            inputPlaylist: ""
        };

        if (window.location.pathname === "/callback") {
            const state = getParameterByName("state");
            const code = getParameterByName("code");
            const error = getParameterByName("error");
            console.log("State:", state);
            console.log("Error:", error);
            // todo handle error
            window.history.pushState('Main', 'Title', '/');

            let loginPromise = fetch(getBackendBaseUrl() + '/sessions', {
                credentials: 'include',
                method: 'POST',
                headers: {
                    'Accept': 'application/json',
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({code})
            })
                .then(response => response.json())
                .then(response => {
                    console.log("Response: ", response);
                    this.setState({
                        loggedIn: true
                    });
                });

            loginPromise
                .then(() => {
                    fetch(getBackendBaseUrl() + '/users/me', {
                        credentials: 'include'
                    })
                        .then(response => response.json())
                        .then(response => {
                            if (response.error) {
                                console.log("error:", response.error);
                                this.setState({
                                    error: response.error
                                });
                            } else {
                                this.setState({
                                    userId: response.userId
                                });
                            }
                        });
                });

            loginPromise
                .then(() => {
                    this.refreshPlaylist();
                });
        } else {
            fetch(getBackendBaseUrl() + '/sessions', {credentials: 'include'})
                .then(response => {
                    console.log("Check if logged in", response);
                    if(response.status !== 200) {
                        return;
                    }
                    this.setState({
                        loggedIn: true
                    });
                    fetch(getBackendBaseUrl() + '/users/me', {
                        credentials: 'include'
                    })
                        .then(response => response.json())
                        .then(response => {
                            if (response.error) {
                                console.log("error:", response.error);
                                this.setState({
                                    error: response.error
                                });
                            } else {
                                this.setState({
                                    userId: response.userId
                                });
                            }
                        });
                    this.refreshPlaylist();
                })
        }
    }

    render() {
        return (
            <div className="App">
                <header className="App-header">
                    <h1 className="App-title">Autoplaylist</h1>
                </header>
                <div>
                    {this.state.error && (<div>{this.state.error}</div>)}
                    {(!this.state.loggedIn && !this.state.error) && (<button onClick={authenticate}>
                        Login with your Spotify Account
                    </button>)}
                    {this.state.userId && (<div>Logged in as {this.state.userId}</div>)}
                </div>
                <div>
                    <ul>
                        {this.state.playLists.map(item => (
                            <li key={item.id}>{item.name}</li>
                        ))}
                    </ul>
                </div>
                {this.state.loggedIn && (
                    <div>
                        <input
                            type="text"
                            name="artist"
                            placeholder="Artist name"
                            value={this.state.inputArtist}
                            onChange={evt => this.updateInputArtist(evt)}
                        />
                        <input
                            type="text"
                            name="playlist"
                            placeholder="Playlist name"
                            value={this.state.inputPlaylist}
                            onChange={evt => this.updateInputPlaylist(evt)}
                        />
                        <input
                            type="text"
                            name="exclusions"
                            placeholder="Exclusions"
                            value={this.state.inputExclusions}
                            onChange={evt => this.updateExclusions(evt)}
                        />
                        <button onClick={this.createPlaylist}>Create playlist
                        </button>
                    </div>
                )}
                {(this.state.loggedIn || this.state.error) && (
                    <button onClick={this.logout}>
                        Log out
                    </button>
                )}
            </div>
        );
    }

    createPlaylist() {
        const artistName = this.state.inputArtist;
        const playlistName = this.state.inputPlaylist;
        const exclusions = (this.state.inputExclusions || "").split(",");
        let message = 'Create playlist "' + playlistName + '"' +
            ' from artist "' + artistName + '"' +
            ' except tracks with "' + exclusions + '" in the name';
        if (window.confirm(message)) {
            fetch(getBackendBaseUrl() + '/playlists', {
                credentials: 'include',
                method: 'POST',
                headers: {
                    'Accept': 'application/json',
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({
                    artist: artistName,
                    name: playlistName,
                    exclusions: exclusions
                })
            })
                .then(response => response.json())
                .then(result => {
                    console.log("Created playlist:", result);
                    this.refreshPlaylist()
                }).catch(error => alert(error));
        }
    }


    refreshPlaylist() {
        fetch(getBackendBaseUrl() + '/playlists', {
            credentials: 'include'
        })
            .then(response => response.json())
            .then(playlists => {
                if (playlists.error) {
                    console.log("error:", playlists.error);
                    this.setState({
                        error: playlists.error
                    });
                } else {
                    this.setState({
                        playLists: playlists
                    });
                }
            });
    }

    updateInputArtist(evt) {
        this.setState({
            inputArtist: evt.target.value
        });
    }

    updateInputPlaylist(evt) {
        this.setState({
            inputPlaylist: evt.target.value
        });
    }

    updateExclusions(evt) {
        this.setState({
            inputExclusions: evt.target.value
        });
    }

    logout() {
        this.setState({
            loggedIn: false,
            userId: null,
            playLists: [],
            error: null,
            inputArtist: "",
            inputPlaylist: "",
            inputExclusions: ""
        });
        fetch(getBackendBaseUrl() + '/sessions', {
            credentials: 'include',
            method: 'DELETE',
        }).then(response => {
            console.log("logged out", response)
        })
    }
}
*/
export default App;
