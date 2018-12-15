// Credits to https://stackoverflow.com/a/901144
const getParameterByName: (name: string, url?: string) => string = (name: string, url?: string) => {
    if (!url) {
        url = window.location.href;
    }
    name = name.replace(/[[]]/g, "\\$&");
    const regex = new RegExp("[?&]" + name + "(=([^&#]*)|&|#|$)");
    const results = regex.exec(url);
    if (!results) {
        return "";
    }
    if (!results[2]) {
        return '';
    }
    return decodeURIComponent(results[2].replace(/\+/g, " ")) || "";
};

export default getParameterByName
