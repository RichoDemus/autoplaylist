import React from 'react';
import ReactDOM from 'react-dom';
import App from './App';

it('renders without crashing', () => {
    const div = document.createElement('div');
    ReactDOM.render(<App/>, div);
    ReactDOM.unmountComponentAtNode(div);
});

it('asd', () => {


    const a = [
        {id: 0, name: "first"},
        {id: 1, name: "second"}
    ];

    console.log(a)

});
