import {Component} from "@angular/core";
import {NavController} from "ionic-angular";
import {StatsPage} from "../stats/stats";

@Component({
             selector: 'page-index',
             templateUrl: 'index.html'
           })
export class IndexPage {
  constructor(private readonly navCtrl: NavController){
    // userParams is an object we have in our nav-parameters

    const splits = location.search.substring(1).split("=");
    if (splits && splits.length == 2 && splits[0] == 'id') {
      this.navCtrl.push(StatsPage, {"id": splits[1]});
    }
  }

  ionViewDidEnter() {
  }
}
