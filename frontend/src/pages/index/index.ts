import {Component} from "@angular/core";
import {NavController, ToastController} from "ionic-angular";
import {StatsPage} from "../stats/stats";

@Component({
             selector: 'page-index',
             templateUrl: 'index.html'
           })
export class IndexPage {
  error: string;

  constructor(private readonly navCtrl: NavController,
              private readonly toastCtrl: ToastController) {
  }

  ionViewDidEnter() {
    const splits = location.search.substring(1).split("=");
    if (splits && splits.length == 2 && splits[0] == 'id') {
      this.navCtrl.push(StatsPage, {"id": splits[1]});
    } else {
      window.history.pushState(null, null, '/');

      this.error = this.navCtrl.getActive().getNavParams().get('error');
      if (this.error) {
        this.presentToast()
      }
    }
  }

  presentToast() {
    const toast = this.toastCtrl
      .create({
                message: this.error,
                duration: 10000,
                position: 'top'
              });
    toast.present();
  }
}
