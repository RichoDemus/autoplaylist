import React from "react";

export const Sync = ({playlistId, enabled, lastSynced, disableSync, enableSync, syncNow}) => (
    <div>
        <strong>Sync:</strong>
        {enabled && <div>Automatic updates enabled
            <button id={playlistId} onClick={disableSync}>Disable</button>
        </div>}
        {!enabled && <div>Automatic updates disabled
            <button id={playlistId} onClick={enableSync}>Enable</button>
        </div>}
        {lastSynced && <span>Last synced: {lastSynced}</span>}
        {!lastSynced && <span>Last synced: Never</span>}
        <button id={playlistId} onClick={syncNow}>Sync now</button>
    </div>
);
