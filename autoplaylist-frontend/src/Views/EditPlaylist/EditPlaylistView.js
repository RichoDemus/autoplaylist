import React from "react";
import CloseEditPlaylistButtonContainer from "./CloseEditPlaylistButtonContainer";
import EditedPlaylistNameContainer from "./EditedPlaylistNameContainer";
import ExclusionsContainer from "./ExclusionsContainer";
import ArtistsContainer from "./ArtistsContainer";
import SyncContainer from "./SyncContainer";

const EditPlaylistView = () => (
    <div className="EditPlaylistView">
        <h2>Editing playlist <EditedPlaylistNameContainer/></h2>
        <ArtistsContainer/>
        <ExclusionsContainer/>
        <SyncContainer/>
        <CloseEditPlaylistButtonContainer/>
    </div>
);

export default EditPlaylistView
