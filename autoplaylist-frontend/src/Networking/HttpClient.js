import {authenticate} from "./Oauth2";

const getBackendBaseUrl = () => {
    if (window.location.hostname === "localhost") {
        return "http://localhost:8080/v1"
    }
    return "https://api.autoplaylists.richodemus.com"
};

export const checkForValidSession = () => {
    return fetch(getBackendBaseUrl() + '/sessions', {credentials: 'include'})
        .then(response => {
            console.log("Check if logged in", response);
            return response.status === 200;
        });
};

export const createSession = code => {
    return fetch(getBackendBaseUrl() + '/sessions', {
        credentials: 'include',
        method: 'POST',
        headers: {
            'Accept': 'application/json',
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({code})
    })
        .then(response => {
            console.log("Create session response: ", response.json());
            return response.status === 200;
        });
};

export const login = () => {
    authenticate();
};

export const logout = () => {
    fetch(getBackendBaseUrl() + '/sessions', {
        credentials: 'include',
        method: 'DELETE',
    }).then(response => {
        console.log("logged out", response)
    });
    return Promise.resolve();
};

export const getUserId = () => {
    return fetch(getBackendBaseUrl() + '/users/me', {
        credentials: 'include'
    })
        .then(response => response.json())
        .then(response => {
            if (response.error) {
                console.log("error:", response.error);
                throw new Error(response.error);
            } else {
                return response.userId;
            }
        });
};

export const getPlaylists = () => {
    return fetch(getBackendBaseUrl() + '/playlists', {
        credentials: 'include'
    })
        .then(response => response.json())
        .then(playlists => {
            if (playlists.error) {
                console.log("error:", playlists.error);
                throw new Error(playlists.error);
            } else {
                return playlists;
            }
        });
};

export const createPlaylist = name => {
    return fetch(getBackendBaseUrl() + '/playlists', {
        credentials: 'include',
        method: 'POST',
        headers: {
            'Accept': 'application/json',
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({name})
    })
        .then(response => response.json())
        .then(response => {
            console.log("Create playlist response:", response);
            return response.playlist;
        });
};

export const updateRules = (playlistId, rules) => {
    console.log("Set rules for playlist", playlistId, "to", rules);
    return fetch(getBackendBaseUrl() + '/playlists/' + playlistId + '/rules', {
        credentials: 'include',
        method: 'POST',
        headers: {
            'Accept': 'application/json',
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(rules)
    })
        .then(response => response.json())
        .then(response => {
            console.log("Update rules response:", response);
            return response.playlist;
        });
};

export const findArtist = query => {
    if (query === "") {
        return Promise.resolve({
            query,
            artists: []
        })
    }
    return fetch(getBackendBaseUrl() + '/artists?name=' + query, {
        credentials: 'include',
        method: 'GET',
        headers: {
            'Accept': 'application/json',
            'Content-Type': 'application/json'
        }
    }).then(response => response.json())
        .then(response => {
            console.log("Search results:", response);
            return response
        }).then(artists => {
            return {
                query,
                artists
            }
        });
};

export const getArtist = id => {
    return fetch(getBackendBaseUrl() + '/artists/' + id, {
        credentials: 'include',
        method: 'GET',
        headers: {
            'Accept': 'application/json',
            'Content-Type': 'application/json'
        }
    }).then(response => response.json());
};

export const enableSync = playlistId => {
    console.log("Enable sync for", playlistId)
};

export const disableSync = playlistId => {
    console.log("Disable sync for", playlistId)
};

export const syncNow = playlistId => {
    return fetch(getBackendBaseUrl() + '/playlists/' + playlistId + '/syncOnce', {
        credentials: 'include',
        method: 'POST',
        headers: {
            'Accept': 'application/json',
            'Content-Type': 'application/json'
        }
    })
        .then(response => response.json())
        .then(response => {
            console.log("Sync once response:", response);
            return response;
        });
};
