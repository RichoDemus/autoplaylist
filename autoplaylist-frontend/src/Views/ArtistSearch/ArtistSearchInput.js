import React from "react";

export const ArtistSearchInput = ({onChange}) => (
    <div>
        <input type="text" placeholder="Artist Name" onChange={onChange}/>
    </div>
);
