import {Component} from "@angular/core";
import {AlertController, ModalController, NavController, ToastController} from "ionic-angular";
import {StatsPage} from "../stats/stats";
import {QRScannerPage} from "../qr-scanner/qr-scanner";

@Component({
             selector: 'page-index',
             templateUrl: 'index.html'
           })
export class IndexPage {
  error: string;

  constructor(private readonly navCtrl: NavController,
              private readonly alertCtrl: AlertController,
              private readonly modalCtrl: ModalController,
              private readonly toastCtrl: ToastController) {
  }

  ionViewDidEnter() {
    const splits = location.search.substring(1).split("=");
    if (splits && splits.length == 2 && splits[0] == 'id') {
      this.goToStatsPage(splits[1]);
    } else {
      window.history.pushState(null, null, '/');

      this.error = this.navCtrl.getActive().getNavParams().get('error');
      if (this.error) {
        this.presentToast()
      }
    }
  }

  goToStatsPage(channelId: string) {
    this.navCtrl.push(StatsPage, {"id": channelId});
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

  presentEnterChannelIdAlert() {
    let alert = this.alertCtrl
      .create({
                title: 'Enter Channel Id',
                inputs: [
                  {
                    name: 'channelId',
                    placeholder: 'Channel Id'
                  }
                ],
                buttons: [
                  {
                    text: 'Cancel',
                    role: 'cancel',
                    handler: () => {
                      console.log('Cancel clicked');
                    }
                  },
                  {
                    text: 'Submit',
                    handler: data => {
                      this.goToStatsPage(data.channelId);
                    }
                  }
                ]
              });
    alert.present();
  }

  presentQRCodeScannerModal() {
    let qrCodeScannerModal = this.modalCtrl.create(QRScannerPage);
    qrCodeScannerModal.present();
  }
}
