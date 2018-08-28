import React from "react";

export const ListPlaylists = ({editPlaylist, playlists}) => (
    <div>
        <strong>Playlists:</strong>
        <ul>
            {playlists.map(playlist => (
                <li id={playlist.id}
                    key={playlist.id}
                    onClick={editPlaylist}>{playlist.name}</li>))}
        </ul>
    </div>
);
