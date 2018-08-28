import {enrichPlaylistWithArtists} from "./ArtistsContainer";

it('Enriches playlist with artistnames', () => {
    const playlist = {
        id: "3",
        name: "Three artists",
        rules: {
            artists: ["0hGpZy6ws8FofByMkt0CV1", "0UW9Tl5qFmdPL4U9sTVHZo", "5HFkc3t0HYETL4JeEbDB1v"],
            exclusions: []
        },
        sync: false
    };

    const artists = [
        {id: "5HFkc3t0HYETL4JeEbDB1v", name: "Powerwolf"},
        {id: "2CIMQHirSU0MQqyYHq0eOx", name: "deadmau5"},
        {id: "0hGpZy6ws8FofByMkt0CV1", name: "Pain"},
        {id: "0UW9Tl5qFmdPL4U9sTVHZo", name: "Centhron"}
    ];

    const result = enrichPlaylistWithArtists(playlist, artists);

    const expected = {
        id: "3",
        name: "Three artists",
        rules: {
            artists: [
                {id: "0hGpZy6ws8FofByMkt0CV1", name: "Pain"},
                {id: "0UW9Tl5qFmdPL4U9sTVHZo", name: "Centhron"},
                {id: "5HFkc3t0HYETL4JeEbDB1v", name: "Powerwolf"}
            ],
            exclusions: []
        },
        sync: false
    };

    expect(result).toEqual(expected);
});
