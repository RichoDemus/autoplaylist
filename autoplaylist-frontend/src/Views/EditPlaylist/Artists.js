import React from "react";

export const Artists = ({playlistId, artists, addArtist, removeArtist}) => (
    <div>
        <strong>Artists:</strong>
        <ul>
            {artists.map(artist => (<li id={artist.id} key={artist.id}>{artist.name}
                <button id={artist.id} data-playlistid={playlistId} onClick={removeArtist}>remove</button>
            </li>))}
        </ul>
        <button id={playlistId} onClick={addArtist}>Add artist</button>
    </div>
);
