import { Component } from '@angular/core';

import { StartPage } from '../pages/start/start';


@Component({
  templateUrl: 'app.html'
})
export class MyApp {
  rootPage = StartPage;
}
