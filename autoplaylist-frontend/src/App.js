import React, {Component} from 'react';
import logo from './logo.svg';
import './App.css';
import {authenticate, getBackendBaseUrl} from './Networking/Oauth2'
import getParameterByName from './Util/QueryString'


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
                        .then(userId => {
                            if (userId.error) {
                                console.log("error:", userId.error);
                                this.setState({
                                    error: userId.error
                                });
                            } else {
                                this.setState({
                                    userId: userId
                                });
                            }
                        });
                });

            loginPromise
                .then(() => {
                    this.refreshPlaylist();
                });
        }
    }

    render() {
        return (
            <div className="App">
                <header className="App-header">
                    <img src={logo} className="App-logo" alt="logo"/>
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
        if (window.confirm('Create playlist "' + playlistName + '" from artist "' + artistName + '"')) {
            fetch(getBackendBaseUrl() + '/playlists', {
                credentials: 'include',
                method: 'POST',
                headers: {
                    'Accept': 'application/json',
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({
                    artist: artistName,
                    name: playlistName
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

    logout() {
        this.setState({
            loggedIn: false,
            userId: null,
            playLists: [],
            error: null,
            inputArtist: "",
            inputPlaylist: ""
        });
    }
}

export default App;
