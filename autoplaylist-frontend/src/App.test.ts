import {store} from "./App"
import {checkForValidSession} from "./Networking/HttpClient"

jest.mock("./Networking/HttpClient", () => ({
    checkForValidSession: Promise.resolve("")
}));

fit('asd', () => {

    // checkForValidSession.
    // checkForValidSession.mockResolvedValue(Promise.resolve(""));

    console.log("Initial state:", store.getState());
});
