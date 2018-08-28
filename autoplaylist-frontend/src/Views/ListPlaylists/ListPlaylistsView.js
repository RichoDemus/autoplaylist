import React from "react";
import UserIdContainer from "../../Components/UserId/UserIdContainer";
import ListPlaylistsContainer from "./ListPlaylistsContainer";
import NewPlaylistButtonContainer from "./NewPlaylistButtonContainer";
import LogoutButtonContainer from "../../Components/LogoutButton/LogoutButtonContainer";

const ListPlaylistsView = () => (
    <div className="ListPlaylistsView">
        <UserIdContainer/>
        <ListPlaylistsContainer/>
        <NewPlaylistButtonContainer/>
        <LogoutButtonContainer/>
    </div>
);

export default ListPlaylistsView
