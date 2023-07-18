import * as _ from 'lodash';

import { ApiServer } from './apiserver'


const start = async() => {
    console.log('Started Node.js App')

    const apisvr = new ApiServer("9988");
	apisvr.create();
	apisvr.listen();
}

start()
