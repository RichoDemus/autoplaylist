import React from "react";

export const NewPlaylistButton = ({newPlaylistClick, onKeyEnter}) => (
    <div>
        <input type="text" placeholder="Playlist Name" onKeyPress={onKeyEnter}/>
        <button onClick={newPlaylistClick}>New Playlist</button>
    </div>
);
