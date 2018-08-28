export const ARTIST_SEARCH_QUERY_UPDATED = "ARTIST_SEARCH_QUERY_UPDATED";

export const artistSearchQueryUpdated = query => {
    return {
        type: ARTIST_SEARCH_QUERY_UPDATED,
        query
    }
};
