import {Component} from "@angular/core";

import {StatsPage} from "../pages/stats/stats";

@Component({
  templateUrl: 'app.html'
})
export class MyApp {
  rootPage = StatsPage;
}
