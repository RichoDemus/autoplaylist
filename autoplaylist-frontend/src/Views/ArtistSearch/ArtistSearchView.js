import React from "react";
import ArtistSearchInputContainer from "./ArtistSearchInputContainer";
import ArtistSearchResultsContainer from "./ArtistSearchResultsContainer";

const ArtistSearchView = () => (
    <div className="ArtistSearchView">
        <span>Search artist</span>
        <ArtistSearchInputContainer/>
        <ArtistSearchResultsContainer/>
    </div>
);

export default ArtistSearchView
