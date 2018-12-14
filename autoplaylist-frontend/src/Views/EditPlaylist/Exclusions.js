import React from "react";

export const Exclusions = ({playlistId, exclusions, removeExclusion, addExclusionClick, addExclusionKeyPress}) => (
    <div>
        <strong>Exclusions:</strong>
        <ul>
            {exclusions.map(exclusion => (<li id={exclusion.id} key={exclusion.id}>{exclusion.name}
                <button id={exclusion.id} data-playlistid={playlistId} onClick={removeExclusion}>remove</button>
            </li>))}
        </ul>
        <div>
            <label>Add exclusion</label>
            <input id={playlistId} type="text" placeholder="keyword" onKeyPress={addExclusionKeyPress}/>
            <button id={playlistId} onClick={addExclusionClick}>Add</button>
        </div>
    </div>
);
