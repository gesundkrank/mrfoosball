import { Component } from '@angular/core';

import { StartPage } from '../pages/start/start';
//import { StatsPage } from '../pages/stats/stats';


@Component({
  templateUrl: 'app.html'
})
export class MyApp {
  rootPage = StartPage;
//  rootPage = StatsPage;
}
