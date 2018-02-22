import {Component} from "@angular/core";

import {IndexPage} from "../pages/index";

@Component({
             templateUrl: 'app.html'
           })
export class MyApp {
  rootPage = IndexPage;
}
