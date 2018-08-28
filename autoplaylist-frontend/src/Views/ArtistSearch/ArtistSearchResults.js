import React from "react";

export const ArtistSearchResults = ({playlistId, artists, selectArtist, back}) => (
    <div>
        <div>
            <ul>
                {artists.map(artist => (<li id={artist.id}
                                            key={artist.id}
                                            data-playlistid={playlistId}
                                            onClick={selectArtist}>{artist.name}</li>))}
            </ul>
        </div>
        <button onClick={back}>Back</button>
    </div>
);
